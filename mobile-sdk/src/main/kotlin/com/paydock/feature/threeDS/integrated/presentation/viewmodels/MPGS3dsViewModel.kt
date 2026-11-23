package com.paydock.feature.threeDS.integrated.presentation.viewmodels

import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.core.extensions.safeCastAs
import com.paydock.core.presentation.viewmodels.BaseViewModel
import com.paydock.core.telemetry.SdkWidget
import com.paydock.core.telemetry.failPayment
import com.paydock.core.telemetry.finishPayment
import com.paydock.core.telemetry.reportSdkError
import com.paydock.core.telemetry.startPayment
import com.paydock.feature.threeDS.integrated.domain.mapper.asEntity
import com.paydock.feature.threeDS.integrated.domain.model.ui.MPGS3dsEvent
import com.paydock.feature.threeDS.integrated.presentation.state.MPGS3dsUIState
import com.paydock.telemetry.Telemetry
import com.paydock.telemetry.TelemetryEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ViewModel for managing the UI state and logic related to MPGS 3DS (3-D Secure) authentication.
 *
 * This ViewModel handles the lifecycle of MPGS 3DS events, including processing authentication
 * results, updating the UI state, and managing errors. It exposes a [SharedFlow] to allow UI
 * components to observe and react to changes in the 3DS process.
 *
 * @param dispatchers A [DispatchersProvider] for managing coroutine dispatchers.
 * @param telemetry The telemetry facade the view model reports its handled errors to.
 *
 * @see BaseViewModel
 * @see MPGS3dsUIState
 * @see MPGS3dsEvent
 * @see DispatchersProvider
 */
internal class MPGS3dsViewModel(
    dispatchers: DispatchersProvider,
    private val telemetry: Telemetry,
) : BaseViewModel(dispatchers) {

    /**
     * A [MutableSharedFlow] used to emit events related to the MPGS 3DS UI state.
     *
     * This flow is used internally to communicate changes in the 3DS UI state to any
     * components that are observing it.  It's designed for one-way communication,
     * pushing updates outwards from the system managing the 3DS UI state.
     */
    private val _eventFlow = MutableSharedFlow<MPGS3dsUIState>(replay = 0)

    /**
     * Whether a web view failure was reported: the web view's child windows can fire one error per failed resource,
     * so only the first failure is reported per attempt (MP3).
     */
    private val widgetFailureReported = AtomicBoolean(false)

    /**
     * A shared flow of [MPGS3dsUIState] events emitted by the MPGS 3DS UI.
     *
     * This flow allows multiple collectors to receive updates about the state changes of the 3DS UI.
     * It is typically used to observe events like the start of the challenge,
     * the completion of the challenge, or any errors that may occur during the process.
     *
     * The flow is "hot", meaning it starts emitting events as soon as they are produced,
     * regardless of whether any collectors are actively listening.
     * New collectors will receive only the events that occur after they start collecting.
     *
     * The underlying implementation is a [SharedFlow], ensuring that multiple collectors
     * receive the same sequence of events without the need for complex sharing logic.
     *
     * @see MPGS3dsUIState
     * @see SharedFlow
     */
    val eventFlow: SharedFlow<MPGS3dsUIState> = _eventFlow.asSharedFlow()

    /** The token of the last attempt started, so the same token starts nothing. Main thread only. */
    private var attemptToken: String? = null

    /**
     * The open payment attempt's ID, or null when none is open or telemetry is inactive. Set on the main thread and
     * read on the JS bridge's thread.
     */
    @Volatile
    private var paymentId: String? = null

    /** The attempt's 3DS charge ID once known (D25): the token's at start, else the first event's; reset per attempt. */
    @Volatile
    private var chargeId: String? = null

    init {
        updateState(MPGS3dsUIState.Idle)
    }

    /**
     * Resets the current state to idle.
     *
     * This method is used to clear the current UI state, preparing the ViewModel
     * for a fresh flow or to indicate that no action is currently taking place.
     */
    fun resetResultState() {
        updateState(MPGS3dsUIState.Idle)
    }

    /**
     * Starts a payment attempt (§ 5.9) for each new 3DS token: the widget calls it from its launch effect. The same
     * token starts nothing, so a rotation or recomposition sends no second start. A new token, such as a retry on the
     * same screen where Koin hands back this view model, starts a new attempt; one still open counts as abandoned.
     *
     * @param token The widget's 3DS token; the backend issues a new one for every 3DS charge.
     * @param charge3dsId The 3DS charge ID decoded from the widget's token, or null when it has none (D25).
     */
    fun startAttempt(token: String, charge3dsId: String? = null) {
        if (token == attemptToken) return
        attemptToken = token
        chargeId = null
        widgetFailureReported.set(false)
        rememberChargeId(charge3dsId)
        paymentId = telemetry.startPayment(SdkWidget.MPGS_3DS, chargeId)
    }

    /** Keeps [id] as the attempt's charge ID when none is known yet and [id] is not empty. */
    private fun rememberChargeId(id: String?) {
        if (chargeId == null && !id.isNullOrEmpty()) chargeId = id
    }

    /**
     * Ends the open payment attempt as an error with the code of [error], the web view failure the widget hands the
     * merchant itself.
     *
     * @param error The error the merchant gets.
     */
    fun failAttempt(error: Throwable) {
        if (paymentId == null) return
        finishAttempt(TelemetryEvent.PaymentOutcome.ERROR, error)
    }

    /**
     * Updates the internal UI state to the given value.
     *
     * @param newState The new state to set in the ViewModel.
     */
    private fun updateState(newState: MPGS3dsUIState) {
        launchOnIO {
            _eventFlow.emit(newState)
        }
    }

    /**
     * Processes an MPGS 3DS event and updates the UI state accordingly.
     *
     * This method handles various MPGS 3DS events, such as authentication success,
     * rejection, or errors, and updates the UI state to reflect the corresponding
     * result. Each event type maps to a specific [MPGS3dsUIState].
     *
     * @param event The MPGS 3DS event to be processed.
     */
    private fun updateThreeDSEvent(event: MPGS3dsEvent) {
        rememberChargeId(event.data.charge3dsId)
        val result = event.asEntity()
        updateState(MPGS3dsUIState.Success(result))
        finishPaymentOn(event)
    }

    /**
     * Ends the open payment attempt on a final event: success, a reject or an additional data reject as declined, a
     * cancellation as cancelled. The charge auth and additional data success events end nothing.
     */
    private fun finishPaymentOn(event: MPGS3dsEvent) {
        if (paymentId == null) return
        when (event) {
            is MPGS3dsEvent.ChargeAuthSuccessEvent -> finishAttempt(TelemetryEvent.PaymentOutcome.SUCCESS)
            is MPGS3dsEvent.ChargeAuthRejectEvent,
            is MPGS3dsEvent.AdditionalDataCollectRejectEvent -> finishAttempt(TelemetryEvent.PaymentOutcome.DECLINED)
            is MPGS3dsEvent.ChargeAuthCancelledEvent -> finishAttempt(TelemetryEvent.PaymentOutcome.CANCELLED)
            is MPGS3dsEvent.ChargeAuthEvent,
            is MPGS3dsEvent.AdditionalDataCollectSuccessEvent -> Unit
        }
    }

    /**
     * Ends the open payment attempt with [outcome]: an error carries the code of [error], the exception the merchant
     * receives (none when it is not an SdkException). Every finish carries the attempt's charge ID when known, and
     * never a flow. Then no attempt is open.
     */
    private fun finishAttempt(outcome: TelemetryEvent.PaymentOutcome, error: Throwable? = null) {
        if (outcome == TelemetryEvent.PaymentOutcome.ERROR) {
            telemetry.failPayment(SdkWidget.MPGS_3DS, paymentId, error, charge3dsId = chargeId)
        } else {
            telemetry.finishPayment(SdkWidget.MPGS_3DS, paymentId, outcome, charge3dsId = chargeId)
        }
        paymentId = null
    }

    /**
     * Handles the result of a 3DS event operation.
     *
     * This function processes the result of an operation that produces a [MPGS3dsEvent].
     * It uses the `Result` type to handle both success and failure scenarios.
     *
     * In case of success, it calls [updateThreeDSEvent] with the received [MPGS3dsEvent].
     * In case of failure, it attempts to cast the `Throwable` to an [SdkException]. If the cast is successful,
     * it updates the UI state to [MPGS3dsUIState.Error] with the `SdkException`.
     * If the cast is not successful, nothing happens.
     *
     * @param eventResult The `Result` containing either a successful [MPGS3dsEvent] or a `Throwable` in case of failure.
     *
     * @see Result
     * @see MPGS3dsEvent
     * @see SdkException
     * @see MPGS3dsUIState
     * @see updateThreeDSEvent
     * @see updateState
     * @see safeCastAs
     */
    fun handleEventResult(eventResult: Result<MPGS3dsEvent>) {
        eventResult.fold(
            onSuccess = { event ->
                updateThreeDSEvent(event)
            },
            onFailure = { throwable ->
                telemetry.reportSdkError(throwable, SdkWidget.MPGS_3DS)
                throwable.safeCastAs<SdkException>()
                    ?.let { updateState(MPGS3dsUIState.Error(it)) }
                failAttempt(throwable)
            }
        )
    }

    /**
     * Reports a failure the widget builds and hands to the merchant itself, outside this view model's state (MP3):
     * the first one per view model, since child windows can fire one error per failed resource. The merchant still
     * gets every one.
     *
     * @param error The error the merchant gets.
     * @param cause The original throwable [error] was built from, if any.
     */
    fun reportWidgetError(error: Throwable, cause: Throwable? = null) {
        if (widgetFailureReported.compareAndSet(false, true)) {
            telemetry.reportSdkError(error, SdkWidget.MPGS_3DS, cause = cause)
        }
    }
}
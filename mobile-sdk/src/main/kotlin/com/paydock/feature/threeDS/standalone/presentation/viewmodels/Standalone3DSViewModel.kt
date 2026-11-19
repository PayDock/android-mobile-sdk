package com.paydock.feature.threeDS.standalone.presentation.viewmodels

import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.core.extensions.safeCastAs
import com.paydock.core.presentation.viewmodels.BaseViewModel
import com.paydock.core.telemetry.SdkWidget
import com.paydock.core.telemetry.failPayment
import com.paydock.core.telemetry.finishPayment
import com.paydock.core.telemetry.reportSdkError
import com.paydock.core.telemetry.startPayment
import com.paydock.feature.threeDS.standalone.domain.mapper.asEntity
import com.paydock.feature.threeDS.standalone.domain.mapper.asProgress
import com.paydock.feature.threeDS.standalone.domain.mapper.withAuthenticationFlow
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSResult
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.Standalone3DSAuthenticationFlow
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.StandaloneEventType
import com.paydock.feature.threeDS.standalone.domain.model.ui.Standalone3DSEvent
import com.paydock.feature.threeDS.standalone.domain.model.ui.enums.StandaloneEvent
import com.paydock.feature.threeDS.standalone.presentation.state.Standalone3DSUIState
import com.paydock.telemetry.Telemetry
import com.paydock.telemetry.TelemetryEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ViewModel for managing the UI state and logic related to Standalone 3DS (3-D Secure) authentication.
 *
 * This ViewModel handles the lifecycle of Standalone 3DS events, including processing authentication
 * results, updating the UI state, and managing errors. It exposes a [SharedFlow] to allow UI
 * components to observe and react to changes in the 3DS process.
 *
 * @param dispatchers A [DispatchersProvider] for managing coroutine dispatchers.
 * @param telemetry The telemetry facade the view model reports its handled errors to.
 *
 * @see BaseViewModel
 * @see Standalone3DSUIState
 * @see Standalone3DSEvent
 * @see DispatchersProvider
 */
internal class Standalone3DSViewModel(
    dispatchers: DispatchersProvider,
    private val telemetry: Telemetry,
) : BaseViewModel(dispatchers) {

    /**
     * A [MutableSharedFlow] used to emit events related to the 3DS UI state.
     *
     * This flow is used internally to communicate changes in the 3DS UI state to any
     * components that are observing it.  It's designed for one-way communication,
     * pushing updates outwards from the system managing the 3DS UI state.
     *
     * States are emitted synchronously (buffered) so collectors receive them in the exact order the
     * 3DS events arrived, e.g. a loading change always precedes the result it belongs to. The buffer is
     * unbounded so `tryEmit` never drops a state (a lost final result would leave the flow hanging).
     */
    private val _eventFlow = MutableSharedFlow<Standalone3DSUIState>(
        replay = 0,
        extraBufferCapacity = Channel.UNLIMITED
    )

    /**
     * A shared flow of [Standalone3DSUIState] events emitted by the 3DS UI.
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
     * @see Standalone3DSUIState
     * @see SharedFlow
     */
    val eventFlow: SharedFlow<Standalone3DSUIState> = _eventFlow.asSharedFlow()

    /** Whether this view model's one payment attempt was started (§ 5.9): [startAttempt] runs once. */
    private val paymentStarted = AtomicBoolean(false)

    /**
     * The open payment attempt's ID, or null when none is open or telemetry is inactive. Set on the main thread and
     * read on the JS bridge's thread.
     */
    @Volatile
    private var paymentId: String? = null

    /** The flow seen so far per charge ID, until its final result. Guarded by itself. */
    private val authenticationFlows = HashMap<String?, Standalone3DSAuthenticationFlow>()

    /**
     * Starts this view model's payment attempt (§ 5.9), at most once: the widget calls it from its launch effect.
     */
    fun startAttempt() {
        if (!paymentStarted.compareAndSet(false, true)) return
        paymentId = telemetry.startPayment(SdkWidget.STANDALONE_3DS)
    }

    /**
     * Ends the open payment attempt as an error with the code of [error], the failure the widget hands the merchant
     * itself (render process gone, web view error).
     *
     * @param error The error the merchant gets.
     */
    fun failAttempt(error: Throwable) {
        if (paymentId == null) return
        finishAttempt(TelemetryEvent.PaymentOutcome.ERROR, error = error)
    }

    /**
     * Resets the current state to idle.
     *
     * This method is used to clear the current UI state, preparing the ViewModel
     * for a fresh flow or to indicate that no action is currently taking place.
     */
    fun resetResultState() {
        updateState(Standalone3DSUIState.Idle)
    }

    /**
     * Updates the internal UI state to the given value.
     *
     * @param newState The new state to set in the ViewModel.
     */
    private fun updateState(newState: Standalone3DSUIState) {
        _eventFlow.tryEmit(newState)
    }

    /**
     * Processes an Standalone 3DS event and updates the UI state accordingly.
     *
     * Each event may emit, in this order:
     * 1. a [Standalone3DSUIState.Loading] change (see [loadingChange]),
     * 2. a [Standalone3DSUIState.Progress] for progress events (challenge, challenge loaded,
     *    challenge completed, decoupled),
     * 3. a [Standalone3DSUIState.Success] for every event reported to the widget's completion
     *    (all events except challenge loaded/completed, which are progress-only).
     *
     * @param event The Standalone 3DS event to be processed.
     */
    private fun updateThreeDSEvent(event: Standalone3DSEvent) {
        event.loadingChange()?.let { updateState(Standalone3DSUIState.Loading(it)) }
        event.asProgress()?.let { updateState(Standalone3DSUIState.Progress(it)) }
        event.asEntity()?.let { updateState(Standalone3DSUIState.Success(it.withTrackedFlow())) }
        finishPaymentOn(event)
    }

    /**
     * Tracks the authentication flow per charge ID and sets it on a final result: a challenge or decoupled result
     * records its flow (the latest wins), a success or reject takes the recorded flow (see [withAuthenticationFlow])
     * and ends the tracking for its ID, as a charge error does. A null charge ID is tracked under the null key.
     */
    private fun Standalone3DSResult.withTrackedFlow(): Standalone3DSResult = synchronized(authenticationFlows) {
        when (event) {
            StandaloneEventType.CHARGE_AUTH_CHALLENGE ->
                authenticationFlows[charge3dsId] = Standalone3DSAuthenticationFlow.CHALLENGE
            StandaloneEventType.CHARGE_AUTH_DECOUPLED ->
                authenticationFlows[charge3dsId] = Standalone3DSAuthenticationFlow.DECOUPLED
            StandaloneEventType.CHARGE_ERROR -> authenticationFlows.remove(charge3dsId)
            StandaloneEventType.CHARGE_AUTH_SUCCESS,
            StandaloneEventType.CHARGE_AUTH_REJECT ->
                return@synchronized withAuthenticationFlow(authenticationFlows.remove(charge3dsId))
            StandaloneEventType.CHARGE_AUTH_INFO -> Unit
        }
        this
    }

    /**
     * Ends the open payment attempt on a final event: success, a reject as declined, a charge error as an error
     * with the `CHARGE_ERROR` code. Challenge, decoupled, info and the challenge loaded/completed events end nothing.
     */
    private fun finishPaymentOn(event: Standalone3DSEvent) {
        if (paymentId == null) return
        when (event) {
            is Standalone3DSEvent.ChargeAuthSuccessEvent -> finishAttempt(TelemetryEvent.PaymentOutcome.SUCCESS)
            is Standalone3DSEvent.ChargeAuthRejectEvent -> finishAttempt(TelemetryEvent.PaymentOutcome.DECLINED)
            is Standalone3DSEvent.ChargeErrorEvent ->
                finishAttempt(TelemetryEvent.PaymentOutcome.ERROR, errorCode = StandaloneEvent.CHARGE_ERROR.name)
            else -> Unit
        }
    }

    /**
     * Ends the open payment attempt with [outcome]: an error with [error] carries the code of the exception the
     * merchant receives (none when it is not an SdkException), else [errorCode]. Then no attempt is open.
     */
    private fun finishAttempt(
        outcome: TelemetryEvent.PaymentOutcome,
        error: Throwable? = null,
        errorCode: String? = null,
    ) {
        if (error != null) {
            telemetry.failPayment(SdkWidget.STANDALONE_3DS, paymentId, error)
        } else {
            telemetry.finishPayment(SdkWidget.STANDALONE_3DS, paymentId, outcome, errorCode)
        }
        paymentId = null
    }

    /**
     * Determines how an event affects the loading indicator.
     *
     * The loader stays up from launch through fingerprinting and the challenge start (the challenge
     * page is not visible yet), is hidden once the challenge page has loaded, is shown again while the
     * challenge result is confirmed and is hidden on the final result. Decoupled authentications hide
     * it (the shopper approves outside the widget); informational events leave it unchanged.
     *
     * @return `true` to show the loader, `false` to hide it, or `null` to leave it unchanged.
     */
    private fun Standalone3DSEvent.loadingChange(): Boolean? = when (this) {
        is Standalone3DSEvent.ChargeAuthChallengeEvent,
        is Standalone3DSEvent.ChargeAuthChallengeCompletedEvent -> true

        is Standalone3DSEvent.ChargeAuthChallengeLoadedEvent,
        is Standalone3DSEvent.ChargeAuthDecoupledEvent,
        is Standalone3DSEvent.ChargeAuthSuccessEvent,
        is Standalone3DSEvent.ChargeAuthRejectEvent,
        is Standalone3DSEvent.ChargeErrorEvent -> false

        is Standalone3DSEvent.ChargeAuthInfoEvent -> null
    }

    /**
     * Handles the result of a 3DS event operation.
     *
     * This function processes the result of an operation that produces a `ThreeDSEvent.Standalone3DSEvent`.
     * It uses the `Result` type to handle both success and failure scenarios.
     *
     * In case of success, it calls [updateThreeDSEvent] with the received [Standalone3DSEvent].
     * In case of failure, it attempts to cast the `Throwable` to an [SdkException]. If the cast is successful,
     * it hides the loader and updates the UI state to `ThreeDSUIState.Error` with the `SdkException`.
     * If the cast is not successful, nothing happens.
     *
     * @param eventResult The `Result` containing either a successful `ThreeDSEvent.Standalone3DSEvent` or a `Throwable` in case of failure.
     *
     * @see Result
     * @see Standalone3DSEvent
     * @see SdkException
     * @see Standalone3DSUIState
     * @see updateThreeDSEvent
     * @see updateState
     * @see safeCastAs
     */
    fun handleEventResult(eventResult: Result<Standalone3DSEvent>) {
        eventResult.fold(
            onSuccess = { event ->
                updateThreeDSEvent(event)
            },
            onFailure = { throwable ->
                telemetry.reportSdkError(throwable, SdkWidget.STANDALONE_3DS)
                throwable.safeCastAs<SdkException>()
                    ?.let {
                        updateState(Standalone3DSUIState.Loading(false))
                        updateState(Standalone3DSUIState.Error(it))
                    }
                failAttempt(throwable)
            }
        )
    }

    /**
     * Reports a failure the widget or its bridge holds outside this view model's state: the unknown bridge event
     * that is otherwise only logged (SD2), the render process gone and web view errors handed to the merchant (SD4,
     * SD5).
     *
     * @param error The error the merchant gets, or the one the bridge drops.
     * @param cause The original throwable [error] was built from, if any.
     */
    fun reportWidgetError(error: Throwable, cause: Throwable? = null) {
        telemetry.reportSdkError(error, SdkWidget.STANDALONE_3DS, cause = cause)
    }
}
package com.paydock.feature.src.presentation.viewmodels

import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.presentation.viewmodels.BaseViewModel
import com.paydock.core.telemetry.SdkWidget
import com.paydock.core.telemetry.reportSdkError
import com.paydock.feature.src.domain.model.ui.ClickToPayEvent
import com.paydock.feature.src.domain.model.ui.ErrorData
import com.paydock.feature.src.domain.model.ui.mapToException
import com.paydock.feature.src.presentation.state.ClickToPayViewState
import com.paydock.telemetry.Telemetry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ViewModel class for managing the state of the Click to Pay feature.
 *
 * @param dispatchers Provides dispatchers for coroutines.
 * @param telemetry The telemetry facade the view model reports its handled errors to.
 */
internal class ClickToPayViewModel(
    dispatchers: DispatchersProvider,
    private val telemetry: Telemetry,
) : BaseViewModel(dispatchers) {

    // Mutable state flow to hold the UI state
    private val _stateFlow: MutableStateFlow<ClickToPayViewState> =
        MutableStateFlow(ClickToPayViewState())

    // Expose a read-only state flow for observing the UI state changes
    val stateFlow: StateFlow<ClickToPayViewState> = _stateFlow.asStateFlow()

    /**
     * Whether a web view failure was reported: the web view's child windows can fire one error per failed resource,
     * so only the first failure is reported per view model (CT2).
     */
    private val widgetFailureReported = AtomicBoolean(false)

    /**
     * Reset the result state, clearing token and error.
     */
    fun resetResultState() {
        updateState { state ->
            state.copy(
                token = null,
                error = null
            )
        }
    }

    /**
     * Update the UI state based on the received Click to Pay event.
     *
     * @param event The Click to Pay event to be processed.
     */
    fun updateSRCEvent(event: ClickToPayEvent) {
        when (event) {
            is ClickToPayEvent.IframeLoadedEvent -> {
                updateState { state ->
                    state.copy(isLoading = true)
                }
            }

            is ClickToPayEvent.CheckoutPopupOpenEvent,
            is ClickToPayEvent.CheckoutPopupClosedEvent,
            is ClickToPayEvent.CheckoutReadyEvent -> {
                updateState { state ->
                    state.copy(isLoading = false)
                }
            }

            is ClickToPayEvent.CheckoutCompletedEvent -> {
                updateState { state ->
                    state.copy(token = event.data.data.token, error = null)
                }
            }

            is ClickToPayEvent.CheckoutErrorEvent -> {
                val failure = event.data.mapToException()
                // A user error (a wrong code, an unrecognised user) is the shopper's outcome; a critical error and a
                // bridge parse failure are reported.
                if (event.data !is ErrorData.UserErrorData) telemetry.reportSdkError(failure, SdkWidget.CLICK_TO_PAY)
                updateState { state ->
                    state.copy(token = null, error = failure)
                }
            }
        }
    }

    /**
     * Reports a failure the widget builds and hands to the merchant itself, outside this view model's state (CT2):
     * the first one per view model, since child windows can fire one error per failed resource. The merchant still
     * gets every one.
     *
     * @param error The error the merchant gets.
     * @param cause The original throwable [error] was built from, if any.
     */
    fun reportWidgetError(error: Throwable, cause: Throwable? = null) {
        if (widgetFailureReported.compareAndSet(false, true)) {
            telemetry.reportSdkError(error, SdkWidget.CLICK_TO_PAY, cause = cause)
        }
    }

    /**
     * Helper function to update the UI state.
     *
     * @param update Function to update the current state.
     */
    private fun updateState(update: (ClickToPayViewState) -> ClickToPayViewState) {
        _stateFlow.update { state ->
            update(state)
        }
    }
}
package com.paydock.feature.src.presentation.viewmodels

import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.presentation.ui.BaseViewModel
import com.paydock.feature.src.presentation.MastercardSRCViewState
import com.paydock.feature.src.presentation.model.MastercardSRCEvent
import com.paydock.feature.src.presentation.model.mapToException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel class for managing the state of the Mastercard SRC (Secure Remote Commerce) feature.
 *
 * @param dispatchers Provides dispatchers for coroutines.
 */
internal class MastercardSRCViewModel(
    dispatchers: DispatchersProvider
) : BaseViewModel(dispatchers) {

    // Mutable state flow to hold the UI state
    private val _stateFlow: MutableStateFlow<MastercardSRCViewState> =
        MutableStateFlow(MastercardSRCViewState())

    // Expose a read-only state flow for observing the UI state changes
    val stateFlow: StateFlow<MastercardSRCViewState> = _stateFlow.asStateFlow()

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
     * Update the UI state based on the received Mastercard SRC event.
     *
     * @param event The Mastercard SRC event to be processed.
     */
    fun updateSRCEvent(event: MastercardSRCEvent) {
        when (event) {
            is MastercardSRCEvent.IframeLoadedEvent -> {
                updateState { state ->
                    state.copy(isLoading = true)
                }
            }

            is MastercardSRCEvent.CheckoutPopupOpenEvent,
            is MastercardSRCEvent.CheckoutPopupClosedEvent,
            is MastercardSRCEvent.CheckoutReadyEvent -> {
                updateState { state ->
                    state.copy(isLoading = false)
                }
            }

            is MastercardSRCEvent.CheckoutCompletedEvent -> {
                updateState { state ->
                    state.copy(token = event.data.data.token, error = null)
                }
            }

            is MastercardSRCEvent.CheckoutErrorEvent -> {
                updateState { state ->
                    state.copy(token = null, error = event.data.mapToException())
                }
            }
        }
    }

    /**
     * Helper function to update the UI state.
     *
     * @param update Function to update the current state.
     */
    private fun updateState(update: (MastercardSRCViewState) -> MastercardSRCViewState) {
        _stateFlow.update { state ->
            update(state)
        }
    }
}
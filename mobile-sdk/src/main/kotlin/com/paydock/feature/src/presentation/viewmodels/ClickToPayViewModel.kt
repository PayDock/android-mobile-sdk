package com.paydock.feature.src.presentation.viewmodels

import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.presentation.viewmodels.BaseViewModel
import com.paydock.feature.src.domain.model.ui.ClickToPayEvent
import com.paydock.feature.src.domain.model.ui.mapToException
import com.paydock.feature.src.presentation.state.ClickToPayViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel class for managing the state of the Click to Pay feature.
 *
 * @param dispatchers Provides dispatchers for coroutines.
 */
internal class ClickToPayViewModel(
    dispatchers: DispatchersProvider
) : BaseViewModel(dispatchers) {

    // Mutable state flow to hold the UI state
    private val _stateFlow: MutableStateFlow<ClickToPayViewState> =
        MutableStateFlow(ClickToPayViewState())

    // Expose a read-only state flow for observing the UI state changes
    val stateFlow: StateFlow<ClickToPayViewState> = _stateFlow.asStateFlow()

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
    private fun updateState(update: (ClickToPayViewState) -> ClickToPayViewState) {
        _stateFlow.update { state ->
            update(state)
        }
    }
}
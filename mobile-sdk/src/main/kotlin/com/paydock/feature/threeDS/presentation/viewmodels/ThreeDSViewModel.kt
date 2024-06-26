package com.paydock.feature.threeDS.presentation.viewmodels

import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.ThreeDSException
import com.paydock.core.presentation.ui.BaseViewModel
import com.paydock.feature.threeDS.presentation.ThreeDSViewState
import com.paydock.feature.threeDS.presentation.model.ThreeDSEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel class responsible for managing the UI state related to 3DS authentication.
 *
 * @param dispatchers The dispatchers provider for coroutine operations.
 */
internal class ThreeDSViewModel(dispatchers: DispatchersProvider) : BaseViewModel(dispatchers) {

    // Mutable state flow to hold the UI state
    private val _stateFlow: MutableStateFlow<ThreeDSViewState> =
        MutableStateFlow(ThreeDSViewState())

    // Expose a read-only state flow for observing the UI state changes
    val stateFlow: StateFlow<ThreeDSViewState> = _stateFlow.asStateFlow()

    /**
     * Resets the result state, clearing the charge3dsId and error.
     */
    fun resetResultState() {
        updateState { state ->
            state.copy(
                charge3dsId = null,
                error = null
            )
        }
    }

    /**
     * Updates the UI state based on the received 3DS event.
     *
     * @param event The 3DS event to be processed.
     */
    fun updateThreeDSEvent(event: ThreeDSEvent) {
        when (event) {
            is ThreeDSEvent.ChargeAuthSuccessEvent -> {
                updateState { state ->
                    state.copy(
                        isLoading = false,
                        charge3dsId = event.data.charge3dsId,
                        status = event.data.status,
                        error = null
                    )
                }
            }

            is ThreeDSEvent.ChargeAuthRejectEvent -> {
                updateState { state ->
                    state.copy(
                        isLoading = false,
                        charge3dsId = event.data.charge3dsId,
                        status = event.data.status,
                        error = ThreeDSException.ChargeErrorException(
                            displayableMessage = MobileSDKConstants.Errors.THREE_DS_REJECTED_ERROR
                        )
                    )
                }
            }

            is ThreeDSEvent.ChargeAuthDecoupledEvent -> {
                updateState { state ->
                    state.copy(
                        charge3dsId = event.data.charge3dsId,
                        status = event.data.status,
                        error = null
                    )
                }
            }

            is ThreeDSEvent.ChargeAuthInfoEvent -> {
                updateState { state ->
                    state.copy(charge3dsId = event.data.charge3dsId, status = event.data.status)
                }
            }

            is ThreeDSEvent.ChargeAuthChallengeEvent -> {
                updateState { state ->
                    state.copy(charge3dsId = event.data.charge3dsId, status = event.data.status)
                }
            }

            is ThreeDSEvent.ChargeErrorEvent -> {
                updateState { state ->
                    state.copy(
                        isLoading = false,
                        charge3dsId = event.data.charge3dsId,
                        error = ThreeDSException.ChargeErrorException(
                            displayableMessage = event.data.error.message
                                ?: MobileSDKConstants.Errors.THREE_DS_ERROR
                        )
                    )
                }
            }
        }
    }

    /**
     * Helper function to update the UI state.
     *
     * @param update Function to update the current state.
     */
    private fun updateState(update: (ThreeDSViewState) -> ThreeDSViewState) {
        _stateFlow.update { state ->
            update(state)
        }
    }
}

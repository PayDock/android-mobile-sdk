package com.paydock.feature.threeDS.presentation.viewmodels

import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.presentation.viewmodels.BaseViewModel
import com.paydock.feature.threeDS.domain.model.integration.ThreeDSResult
import com.paydock.feature.threeDS.domain.model.integration.enums.EventType
import com.paydock.feature.threeDS.domain.model.ui.ThreeDSEvent
import com.paydock.feature.threeDS.presentation.state.ThreeDSUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for managing the 3D Secure (3DS) flow and UI state.
 *
 * This ViewModel tracks the current state of the 3DS process and updates it
 * based on events. It exposes a public [StateFlow] to allow observers to react to
 * changes in the UI state.
 *
 * @constructor Initializes the ViewModel with the given dispatchers.
 * @param dispatchers A provider for coroutine dispatchers to manage concurrency.
 */
internal class ThreeDSViewModel(dispatchers: DispatchersProvider) : BaseViewModel(dispatchers) {

    // Internal mutable state flow to track UI state.
    private val _stateFlow: MutableStateFlow<ThreeDSUIState> =
        MutableStateFlow(ThreeDSUIState.Idle)

    /**
     * Public [StateFlow] for observing the current 3DS UI state.
     *
     * Observers can use this state flow to react to UI state changes.
     */
    val stateFlow: StateFlow<ThreeDSUIState> = _stateFlow.asStateFlow()

    /**
     * Resets the current state to idle.
     *
     * This method is used to clear the current UI state, preparing the ViewModel
     * for a fresh flow or to indicate that no action is currently taking place.
     */
    fun resetResultState() {
        updateState(ThreeDSUIState.Idle)
    }

    /**
     * Updates the internal UI state to the given value.
     *
     * @param newState The new state to set in the ViewModel.
     */
    private fun updateState(newState: ThreeDSUIState) {
        _stateFlow.value = newState
    }

    /**
     * Processes a 3DS event and updates the UI state accordingly.
     *
     * This method handles various 3DS events, such as authentication success,
     * rejection, or errors, and updates the UI state to reflect the corresponding
     * result. Each event type maps to a specific [ThreeDSUIState].
     *
     * @param event The 3DS event to be processed.
     */
    fun updateThreeDSEvent(event: ThreeDSEvent) {
        when (event) {
            is ThreeDSEvent.ChargeAuthSuccessEvent -> {
                updateState(
                    ThreeDSUIState.Success(
                        ThreeDSResult(
                            charge3dsId = event.data.charge3dsId,
                            event = EventType.CHARGE_AUTH_SUCCESS
                        )
                    )
                )
            }

            is ThreeDSEvent.ChargeAuthRejectEvent -> {
                updateState(
                    ThreeDSUIState.Success(
                        ThreeDSResult(
                            charge3dsId = event.data.charge3dsId,
                            event = EventType.CHARGE_AUTH_REJECT
                        )
                    )
                )
            }

            is ThreeDSEvent.ChargeAuthDecoupledEvent -> {
                updateState(
                    ThreeDSUIState.Success(
                        ThreeDSResult(
                            charge3dsId = event.data.charge3dsId,
                            event = EventType.CHARGE_AUTH_DECOUPLED
                        )
                    )
                )
            }

            is ThreeDSEvent.ChargeAuthInfoEvent -> {
                updateState(
                    ThreeDSUIState.Success(
                        ThreeDSResult(
                            charge3dsId = event.data.charge3dsId,
                            event = EventType.CHARGE_AUTH_INFO
                        )
                    )
                )
            }

            is ThreeDSEvent.ChargeAuthChallengeEvent -> {
                updateState(
                    ThreeDSUIState.Success(
                        ThreeDSResult(
                            charge3dsId = event.data.charge3dsId,
                            event = EventType.CHARGE_AUTH_CHALLENGE
                        )
                    )
                )
            }

            is ThreeDSEvent.ChargeErrorEvent -> {
                updateState(
                    ThreeDSUIState.Success(
                        ThreeDSResult(
                            charge3dsId = event.data.charge3dsId,
                            event = EventType.CHARGE_ERROR
                        )
                    )
                )
            }
        }
    }
}
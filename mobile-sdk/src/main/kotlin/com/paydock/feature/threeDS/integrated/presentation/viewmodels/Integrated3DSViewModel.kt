package com.paydock.feature.threeDS.integrated.presentation.viewmodels

import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.core.extensions.safeCastAs
import com.paydock.core.presentation.viewmodels.BaseViewModel
import com.paydock.feature.threeDS.integrated.domain.mapper.asEntity
import com.paydock.feature.threeDS.integrated.domain.model.ui.Integrated3DSEvent
import com.paydock.feature.threeDS.integrated.presentation.state.Integrated3DSUIState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * ViewModel for managing the UI state and logic related to Integrated 3DS (3-D Secure) authentication.
 *
 * This ViewModel handles the lifecycle of Integrated 3DS events, including processing authentication
 * results, updating the UI state, and managing errors. It exposes a [SharedFlow] to allow UI
 * components to observe and react to changes in the 3DS process.
 *
 * @param dispatchers A [DispatchersProvider] for managing coroutine dispatchers.
 *
 * @see BaseViewModel
 * @see Integrated3DSUIState
 * @see Integrated3DSEvent
 * @see DispatchersProvider
 */
internal class Integrated3DSViewModel(dispatchers: DispatchersProvider) :
    BaseViewModel(dispatchers) {

    /**
     * A [MutableSharedFlow] used to emit events related to the Integrated 3DS UI state.
     *
     * This flow is used internally to communicate changes in the 3DS UI state to any
     * components that are observing it.  It's designed for one-way communication,
     * pushing updates outwards from the system managing the 3DS UI state.
     */
    private val _eventFlow = MutableSharedFlow<Integrated3DSUIState>(replay = 0)

    /**
     * A shared flow of [Integrated3DSUIState] events emitted by the integrated 3DS UI.
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
     * @see Integrated3DSUIState
     * @see SharedFlow
     */
    val eventFlow: SharedFlow<Integrated3DSUIState> = _eventFlow.asSharedFlow()

    init {
        updateState(Integrated3DSUIState.Idle)
    }

    /**
     * Resets the current state to idle.
     *
     * This method is used to clear the current UI state, preparing the ViewModel
     * for a fresh flow or to indicate that no action is currently taking place.
     */
    fun resetResultState() {
        updateState(Integrated3DSUIState.Idle)
    }

    /**
     * Updates the internal UI state to the given value.
     *
     * @param newState The new state to set in the ViewModel.
     */
    private fun updateState(newState: Integrated3DSUIState) {
        launchOnIO {
            _eventFlow.emit(newState)
        }
    }

    /**
     * Processes an Integrated 3DS event and updates the UI state accordingly.
     *
     * This method handles various Integrated 3DS events, such as authentication success,
     * rejection, or errors, and updates the UI state to reflect the corresponding
     * result. Each event type maps to a specific [Integrated3DSUIState].
     *
     * @param event The Integrated 3DS event to be processed.
     */
    private fun updateThreeDSEvent(event: Integrated3DSEvent) {
        val result = event.asEntity()
        updateState(Integrated3DSUIState.Success(result))
    }

    /**
     * Handles the result of a 3DS event operation.
     *
     * This function processes the result of an operation that produces a `ThreeDSEvent.Integrated3DSEvent`.
     * It uses the `Result` type to handle both success and failure scenarios.
     *
     * In case of success, it calls [updateThreeDSEvent] with the received [Integrated3DSEvent].
     * In case of failure, it attempts to cast the `Throwable` to an [SdkException]. If the cast is successful,
     * it updates the UI state to `ThreeDSUIState.Error` with the `SdkException`.
     * If the cast is not successful, nothing happens.
     *
     * @param eventResult The `Result` containing either a successful `ThreeDSEvent.Integrated3DSEvent` or a `Throwable` in case of failure.
     *
     * @see Result
     * @see Integrated3DSEvent
     * @see SdkException
     * @see Integrated3DSUIState
     * @see updateThreeDSEvent
     * @see updateState
     * @see safeCastAs
     */
    fun handleEventResult(eventResult: Result<Integrated3DSEvent>) {
        eventResult.fold(
            onSuccess = { event ->
                updateThreeDSEvent(event)
            },
            onFailure = { throwable ->
                throwable.safeCastAs<SdkException>()
                    ?.let { updateState(Integrated3DSUIState.Error(it)) }
            }
        )
    }
}
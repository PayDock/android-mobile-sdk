package com.paydock.feature.card.presentation.viewmodels

import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.core.extensions.safeCastAs
import com.paydock.core.presentation.viewmodels.BaseViewModel
import com.paydock.feature.card.data.dto.CreateCardPaymentTokenRequest
import com.paydock.feature.card.domain.usecase.CreateGiftCardPaymentTokenUseCase
import com.paydock.feature.card.presentation.state.GiftCardInputState
import com.paydock.feature.card.presentation.state.GiftCardUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for managing the state and interactions of the Gift Card feature.
 *
 * This ViewModel handles the business logic and state updates for the Gift Card flow,
 * including input state management, tokenization requests, and state transitions for
 * the associated UI.
 *
 * @property accessToken The access token used for authenticated API requests.
 * @property createCardPaymentTokenUseCase Use case for creating a gift card payment token.
 * @property dispatchers Provider for coroutine dispatchers to handle threading concerns.
 */
internal class GiftCardViewModel(
    private val accessToken: String,
    private val createCardPaymentTokenUseCase: CreateGiftCardPaymentTokenUseCase,
    dispatchers: DispatchersProvider,
) : BaseViewModel(dispatchers) {

    // Private mutable state flow to manage the gift card input state
    private val _inputStateFlow: MutableStateFlow<GiftCardInputState> =
        MutableStateFlow(GiftCardInputState())

    /**
     * StateFlow for observing the current input state of the gift card details.
     */
    val inputStateFlow: StateFlow<GiftCardInputState> = _inputStateFlow.asStateFlow()

    // Private mutable state flow to manage the UI state
    private val _stateFlow: MutableStateFlow<GiftCardUIState> =
        MutableStateFlow(GiftCardUIState.Idle)

    /**
     * StateFlow for observing the current UI state of the gift card process.
     */
    val stateFlow: StateFlow<GiftCardUIState> = _stateFlow.asStateFlow()

    /**
     * Resets the UI state to the idle state.
     *
     * This is typically called after completing an operation to ensure
     * the UI is ready for a new user action.
     */
    fun resetResultState() {
        updateState(GiftCardUIState.Idle)
    }

    /**
     * Updates the store pin preference for the gift card.
     *
     * @param storePin Boolean indicating whether the PIN should be stored for future use.
     */
    fun setStorePin(storePin: Boolean) {
        _inputStateFlow.update { state ->
            state.copy(storePin = storePin)
        }
    }

    /**
     * Updates the card number in the gift card input state.
     *
     * @param number The card number to be updated in the input state.
     */
    fun updateCardNumber(number: String) {
        _inputStateFlow.update { state ->
            state.copy(cardNumber = number)
        }
    }

    /**
     * Updates the card PIN in the gift card input state.
     *
     * @param pin The card PIN to be updated in the input state.
     */
    fun updateCardPin(pin: String) {
        _inputStateFlow.update { state ->
            state.copy(pin = pin)
        }
    }

    /**
     * Updates the current UI state.
     *
     * @param newState The new state to be applied to the UI.
     */
    private fun updateState(newState: GiftCardUIState) {
        _stateFlow.value = newState
    }

    /**
     * Initiates the tokenization process for the gift card.
     *
     * - Sets the UI state to `Loading` while the process is ongoing.
     * - Sends a tokenization request using the current input state.
     * - On success, transitions the UI state to `Success` with the generated token.
     * - On failure, transitions the UI state to `Error` with the corresponding exception.
     *
     * This method ensures the UI is updated to reflect the outcome of the operation
     * and prepares the state for any subsequent actions.
     */
    fun tokeniseCard() {
        launchOnIO {
            updateState(GiftCardUIState.Loading)
            val state = _inputStateFlow.value
            val request = CreateCardPaymentTokenRequest.TokeniseCardRequest.GiftCard(
                cardNumber = state.cardNumber,
                cardPin = state.pin,
                storePin = state.storePin
            )
            createCardPaymentTokenUseCase(accessToken, request)
                .onSuccess { details ->
                    updateState(GiftCardUIState.Success(details.token!!))
                }
                .onFailure { error ->
                    error.safeCastAs<SdkException>()
                        ?.let { updateState(GiftCardUIState.Error(it)) }
                }
        }
    }
}
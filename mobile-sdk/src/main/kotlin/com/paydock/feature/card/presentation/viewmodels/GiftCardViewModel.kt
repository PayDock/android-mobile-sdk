package com.paydock.feature.card.presentation.viewmodels

import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.GiftCardException
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.exceptions.UnknownApiException
import com.paydock.core.presentation.ui.BaseViewModel
import com.paydock.feature.card.data.api.dto.TokeniseCardRequest
import com.paydock.feature.card.domain.model.TokenisedCardDetails
import com.paydock.feature.card.domain.usecase.TokeniseGiftCardUseCase
import com.paydock.feature.card.presentation.state.GiftCardViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for handling gift card details input and tokenization.
 *
 * @param useCase The tokenization use case for performing card tokenization.
 */
internal class GiftCardViewModel(
    private val accessToken: String,
    private val useCase: TokeniseGiftCardUseCase,
    dispatchers: DispatchersProvider
) : BaseViewModel(dispatchers) {

    // Mutable state flow to hold the UI state
    private val _stateFlow: MutableStateFlow<GiftCardViewState> =
        MutableStateFlow(GiftCardViewState())

    // Expose a read-only state flow for observing the UI state changes
    val stateFlow: StateFlow<GiftCardViewState> = _stateFlow.asStateFlow()

    /**
     * Set the store pin flag for the gift card details.
     *
     * @param storePin Flag to be able to use a PIN value for the initial transaction.
     */
    fun setStorePin(storePin: Boolean) {
        _stateFlow.update { state ->
            state.copy(storePin = storePin)
        }
    }

    /**
     * Reset the result state, clearing token and error.
     */
    fun resetResultState() {
        updateState { state ->
            state.copy(token = null, error = null)
        }
    }

    /**
     * Update the card number in the UI state.
     *
     * @param number The new card number.
     */
    fun updateCardNumber(number: String) {
        updateState { state ->
            state.copy(
                cardNumber = number,
                error = null
            )
        }
    }

    /**
     * Update the card PIN in the UI state.
     *
     * @param pin The new card PIN.
     */
    fun updateCardPin(pin: String) {
        updateState { state ->
            state.copy(pin = pin, error = null)
        }
    }

    // Helper function to update the UI state
    private fun updateState(update: (GiftCardViewState) -> GiftCardViewState) {
        _stateFlow.update { state ->
            update(state)
        }
    }

    /**
     * Tokenize the card details using the provided use case.
     */
    fun tokeniseCard() {
        launchOnIO {
            resetResultState()
            updateState { state ->
                state.copy(isLoading = true)
            }
            val state = _stateFlow.value
            val request = TokeniseCardRequest.GiftCard(
                cardNumber = state.cardNumber,
                cardPin = state.pin,
                storePin = state.storePin
            )
            val result: Result<TokenisedCardDetails> = useCase(accessToken, request)
            _stateFlow.update { currentState ->
                val exception: Throwable? = result.exceptionOrNull()
                val error: GiftCardException? = exception?.let {
                    when (exception) {
                        is ApiException -> GiftCardException.TokenisingCardException(error = exception.error)
                        is UnknownApiException -> GiftCardException.UnknownException(displayableMessage = exception.errorMessage)
                        else -> GiftCardException.UnknownException(displayableMessage = exception.message ?: "An unknown error occurred")
                    }
                } ?: currentState.error
                currentState.copy(
                    error = error,
                    isLoading = false,
                    token = result.getOrNull()?.token
                )
            }
        }
    }
}
package com.paydock.feature.card.presentation.viewmodels

import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.CardDetailsException
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.exceptions.UnknownApiException
import com.paydock.core.presentation.ui.BaseViewModel
import com.paydock.feature.card.data.api.dto.TokeniseCardRequest
import com.paydock.feature.card.domain.model.TokenisedCardDetails
import com.paydock.feature.card.domain.usecase.TokeniseCreditCardUseCase
import com.paydock.feature.card.presentation.state.CardDetailsViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for handling credit card details input and tokenization.
 *
 * @param useCase The tokenization use case for performing card tokenization.
 */
internal class CardDetailsViewModel(
    private val accessToken: String,
    private val useCase: TokeniseCreditCardUseCase,
    dispatchers: DispatchersProvider
) : BaseViewModel(dispatchers) {

    // Mutable state flow to hold the UI state
    private val _stateFlow: MutableStateFlow<CardDetailsViewState> =
        MutableStateFlow(CardDetailsViewState())

    // Expose a read-only state flow for observing the UI state changes
    val stateFlow: StateFlow<CardDetailsViewState> = _stateFlow.asStateFlow()

    /**
     * Set the gateway ID for the card details.
     *
     * @param gatewayId The gateway ID to be set.
     */
    fun setGatewayId(gatewayId: String) {
        _stateFlow.update { state ->
            state.copy(gatewayId = gatewayId)
        }
    }

    /**
     * Reset the result state, clearing token and error.
     */
    fun resetResultState() {
        updateState { state ->
            state.copy(
                token = null,
                error = null,
                cardholderName = "",
                cardNumber = "",
                expiry = "",
                code = ""
            )
        }
    }

    fun updateCardholderName(name: String) {
        updateState { state ->
            state.copy(
                cardholderName = name,
                error = null
            )
        }
    }

    fun updateCardNumber(number: String) {
        updateState { state ->
            state.copy(
                cardNumber = number,
                error = null
            )
        }
    }

    fun updateExpiry(expiry: String) {
        updateState { state ->
            state.copy(expiry = expiry, error = null)
        }
    }

    fun updateSecurityCode(code: String) {
        updateState { state ->
            state.copy(code = code, error = null)
        }
    }

    fun updateSaveCard(saveCard: Boolean) {
        updateState { state ->
            state.copy(
                saveCard = saveCard
            )
        }
    }

    private fun updateState(update: (CardDetailsViewState) -> CardDetailsViewState) {
        _stateFlow.update { state ->
            update(state)
        }
    }

    /**
     * Tokenize the card details using the provided use case.
     */
    fun tokeniseCard() {
        launchOnIO {
            updateState { state ->
                state.copy(isLoading = true)
            }
            val state = _stateFlow.value
            val request = TokeniseCardRequest.CreditCard(
                cvv = state.code,
                cardholderName = state.cardholderName,
                cardNumber = state.cardNumber,
                expiryMonth = state.expiryMonth,
                expiryYear = state.expiryYear,
                gatewayId = state.gatewayId
            )
            val result: Result<TokenisedCardDetails> = useCase(accessToken, request)
            _stateFlow.update { currentState ->
                val exception: Throwable? = result.exceptionOrNull()
                val error: CardDetailsException? = when (exception) {
                    is ApiException -> CardDetailsException.TokenisingCardException(error = exception.error)
                    is UnknownApiException -> CardDetailsException.UnknownException(
                        displayableMessage = exception.errorMessage
                    )

                    else -> currentState.error
                }
                currentState.copy(
                    error = error,
                    isLoading = false,
                    token = result.getOrNull()?.token
                )
            }
        }
    }
}
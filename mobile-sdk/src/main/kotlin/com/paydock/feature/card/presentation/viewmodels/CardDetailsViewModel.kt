package com.paydock.feature.card.presentation.viewmodels

import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.core.extensions.safeCastAs
import com.paydock.core.presentation.viewmodels.BaseViewModel
import com.paydock.feature.card.data.dto.CreateCardPaymentTokenRequest
import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
import com.paydock.feature.card.domain.usecase.CreateCardPaymentTokenUseCase
import com.paydock.feature.card.domain.usecase.GetCardSchemasUseCase
import com.paydock.feature.card.presentation.state.CardDetailsInputState
import com.paydock.feature.card.presentation.state.CardDetailsUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.TreeMap

/**
 * ViewModel for managing the state and operations of card details input and tokenization.
 *
 * @param accessToken The access token required for API requests.
 * @param gatewayId Optional ID of the payment gateway for processing card payments.
 * @param schemeConfig Configuration for supported card schemes and scheme validation behavior.
 * @param getCardSchemasUseCase The use case responsible for retrieving supported card schemas.
 * @param createCardPaymentTokenUseCase The use case responsible for creating card payment tokens.
 * @param dispatchers The provider for coroutine dispatchers, used for handling asynchronous tasks.
 */
internal class CardDetailsViewModel(
    private val accessToken: String,
    private val gatewayId: String?,
    schemeConfig: SupportedSchemeConfig,
    private val getCardSchemasUseCase: GetCardSchemasUseCase,
    private val createCardPaymentTokenUseCase: CreateCardPaymentTokenUseCase,
    dispatchers: DispatchersProvider,
) : BaseViewModel(dispatchers) {

    /**
     * Holds the current input state of card details.
     */
    private val _inputStateFlow: MutableStateFlow<CardDetailsInputState> =
        MutableStateFlow(CardDetailsInputState())
    val inputStateFlow: StateFlow<CardDetailsInputState> = _inputStateFlow.asStateFlow()

    /**
     * Holds the current UI state for card details operations.
     */
    private val _stateFlow: MutableStateFlow<CardDetailsUIState> =
        MutableStateFlow(CardDetailsUIState.Idle)
    val stateFlow: StateFlow<CardDetailsUIState> = _stateFlow.asStateFlow()

    init {
        updateSchemeConfigState(schemeConfig)
        getCardSchemas()
    }

    private fun updateSchemeConfigState(schemeConfig: SupportedSchemeConfig) {
        _inputStateFlow.update { state ->
            state.copy(schemeConfig = schemeConfig)
        }
    }

    /**
     * Fetches the supported card schemas using the provided use case.
     *
     * - On success, updates the input state with the retrieved card schemas.
     * - On failure, clears the card schemas in the input state.
     */
    private fun getCardSchemas() {
        launchOnIO {
            getCardSchemasUseCase()
                .onSuccess { schemas ->
                    _inputStateFlow.update { state ->
                        state.copy(cardSchemas = schemas)
                    }
                }
                .onFailure {
                    _inputStateFlow.update { state ->
                        state.copy(cardSchemas = TreeMap())
                    }
                }
        }
    }

    /**
     * Resets the UI state to idle.
     */
    fun resetResultState() {
        updateState(CardDetailsUIState.Idle)
    }

    /**
     * Sets whether to collect the cardholder's name.
     *
     * @param collectCardHolderName Boolean indicating whether to collect the cardholder's name.
     */
    fun setCollectCardholderName(collectCardHolderName: Boolean) {
        _inputStateFlow.update { state ->
            state.copy(collectCardholderName = collectCardHolderName)
        }
    }

    /**
     * Updates the cardholder's name in the input state.
     *
     * @param name The name of the cardholder to set.
     */
    fun updateCardholderName(name: String) {
        _inputStateFlow.update { state ->
            state.copy(cardholderName = name)
        }
    }

    /**
     * Updates the card number in the input state.
     *
     * @param number The card number to set.
     */
    fun updateCardNumber(number: String) {
        _inputStateFlow.update { state ->
            state.copy(cardNumber = number)
        }
    }

    /**
     * Updates the expiry date in the input state.
     *
     * @param expiry The card's expiry date in MMYY format.
     */
    fun updateExpiry(expiry: String) {
        _inputStateFlow.update { state ->
            state.copy(expiry = expiry)
        }
    }

    /**
     * Updates the security code (CVV/CVC) in the input state.
     *
     * @param code The card's security code.
     */
    fun updateSecurityCode(code: String) {
        _inputStateFlow.update { state ->
            state.copy(code = code)
        }
    }

    /**
     * Updates the "save card" flag in the input state.
     *
     * @param saveCard Boolean indicating whether the card should be saved for future use.
     */
    fun updateSaveCard(saveCard: Boolean) {
        _inputStateFlow.update { state ->
            state.copy(saveCard = saveCard)
        }
    }

    /**
     * Updates the UI state to a new value.
     *
     * @param newState The new state to set for the UI.
     */
    private fun updateState(newState: CardDetailsUIState) {
        _stateFlow.value = newState
    }

    /**
     * Initiates the card tokenization process by making an API request.
     *
     * - Updates the UI state to `Loading` before starting the process.
     * - Creates a tokenization request using the current input state.
     * - On success, updates the UI state to `Success` with the generated token.
     * - On failure, updates the UI state to `Error` with the relevant exception.
     */
    fun tokeniseCard() {
        launchOnIO {
            updateState(CardDetailsUIState.Loading)

            val state = _inputStateFlow.value
            val request = CreateCardPaymentTokenRequest.TokeniseCardRequest.CreditCard(
                cvv = state.code,
                cardholderName = state.cardholderName,
                cardNumber = state.cardNumber,
                expiryMonth = state.expiryMonth,
                expiryYear = state.expiryYear,
                gatewayId = gatewayId
            )
            createCardPaymentTokenUseCase(accessToken, request)
                .onSuccess { details ->
                    updateState(CardDetailsUIState.Success(details.token!!))
                }
                .onFailure { error ->
                    error.safeCastAs<SdkException>()
                        ?.let { updateState(CardDetailsUIState.Error(it)) }
                }
        }
    }
}

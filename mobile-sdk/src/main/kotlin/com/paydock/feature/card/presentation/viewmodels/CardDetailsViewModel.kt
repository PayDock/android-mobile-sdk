package com.paydock.feature.card.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
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
    private val savedStateHandle: SavedStateHandle,
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
        restoreFromSavedState()
    }

    private fun restoreFromSavedState() {
        // For security/PCI reasons, do not restore PAN/CVV/expiry from disk-backed state
        val savedCardholderName: String? = savedStateHandle[KEY_CARDHOLDER_NAME]
        val savedSaveCard: Boolean? = savedStateHandle[KEY_SAVE_CARD]
        if (savedCardholderName != null || savedSaveCard != null) {
            _inputStateFlow.update { state ->
                state.copy(
                    cardholderName = savedCardholderName ?: state.cardholderName,
                    saveCard = savedSaveCard ?: state.saveCard
                )
            }
        }
    }

    private fun updateSchemeConfigState(schemeConfig: SupportedSchemeConfig) {
        _inputStateFlow.update { state ->
            state.copy(schemeConfig = schemeConfig)
        }
    }

    /**
     * Fetches the BIN data using the provided use case.
     * Always uses cache first (CloudFront download), then falls back to the bundled asset (card-schemes.json).
     * BIN refresh runs at SDK init and on app foreground via [BinDataRefreshCoordinator];
     * no BIN network request occurs during the tokenise flow.
     */
    private fun getCardSchemas() {
        launchOnIO {
            getCardSchemasUseCase()
                .onSuccess { binData ->
                    _inputStateFlow.update { state ->
                        state.copy(binData = binData)
                    }
                }
                .onFailure {
                    _inputStateFlow.update { state ->
                        state.copy(binData = null)
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
     * Triggers validation for all fields in the input state.
     * This is useful when the submit button is enabled by default and needs to show errors upon click.
     */
    fun validateAllFields() {
        _inputStateFlow.update { state ->
            state.copy(
                cardholderNameErrorOverwrite = true,
                cardNumberErrorOverwrite = true,
                cardExpiryErrorOverwrite = true,
                cardSecurityErrorOverwrite = true
            )
        }
    }

    private fun resetCardholderErrorOverwrite() {
        if (_inputStateFlow.value.cardholderNameErrorOverwrite) {
            _inputStateFlow.update { state ->
                state.copy(
                    cardholderNameErrorOverwrite = false
                )
            }
        }
    }

    private fun resetCardNumberErrorOverwrite() {
        if (_inputStateFlow.value.cardNumberErrorOverwrite) {
            _inputStateFlow.update { state ->
                state.copy(
                    cardNumberErrorOverwrite = false
                )
            }
        }
    }

    private fun resetCardExpiryErrorOverwrite() {
        if (_inputStateFlow.value.cardExpiryErrorOverwrite) {
            _inputStateFlow.update { state ->
                state.copy(
                    cardExpiryErrorOverwrite = false
                )
            }
        }
    }

    private fun resetCardSecurityErrorOverwrite() {
        if (_inputStateFlow.value.cardSecurityErrorOverwrite) {
            _inputStateFlow.update { state ->
                state.copy(
                    cardSecurityErrorOverwrite = false
                )
            }
        }
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
     * Sets whether to store the security code (CVV) when tokenizing the card.
     *
     * @param storeSecurityCode Boolean indicating whether to store the security code. If `null`,
     * the `store_ccv` parameter will not be sent in the tokenization request.
     */
    fun setStoreSecurityCode(storeSecurityCode: Boolean?) {
        _inputStateFlow.update { state ->
            state.copy(storeSecurityCode = storeSecurityCode)
        }
    }

    /**
     * Updates the cardholder's name in the input state.
     *
     * @param name The name of the cardholder to set.
     */
    fun updateCardholderName(name: String) {
        resetCardholderErrorOverwrite()
        _inputStateFlow.update { state ->
            state.copy(cardholderName = name)
        }
        savedStateHandle[KEY_CARDHOLDER_NAME] = name
    }

    /**
     * Updates the card number in the input state.
     *
     * @param number The card number to set.
     */
    fun updateCardNumber(number: String) {
        resetCardNumberErrorOverwrite()
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
        resetCardExpiryErrorOverwrite()
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
        resetCardSecurityErrorOverwrite()
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
        savedStateHandle[KEY_SAVE_CARD] = saveCard
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
        // Transition to Loading synchronously (before dispatching to IO), so a second call arriving
        // on the same thread before the coroutine has started sees Loading immediately rather than
        // racing it — this is what makes the composable's `uiState is Loading` re-entrancy guard
        // (for both the internal button and the external `state.submit()` trigger) actually durable.
        if (_stateFlow.value is CardDetailsUIState.Loading) return
        updateState(CardDetailsUIState.Loading)

        launchOnIO {
            val state = _inputStateFlow.value
            val request = CreateCardPaymentTokenRequest.TokeniseCardRequest.CreditCard(
                cvv = state.code,
                cardholderName = state.cardholderName,
                cardNumber = state.cardNumber,
                expiryMonth = state.expiryMonth,
                expiryYear = state.expiryYear,
                storeCVV = state.storeSecurityCode,
                savedCardConsentAccepted = state.saveCard,
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

    private companion object {
        const val KEY_CARDHOLDER_NAME = "card_input_cardholder_name"
        const val KEY_SAVE_CARD = "card_input_save_card"
    }
}

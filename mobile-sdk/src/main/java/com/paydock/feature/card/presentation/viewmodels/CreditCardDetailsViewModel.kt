/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 4:15 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paydock.feature.card.presentation.viewmodels

import com.paydock.core.EXPIRY_CHUNK_SIZE
import com.paydock.core.MAX_EXPIRY_LENGTH
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.ErrorModel
import com.paydock.core.domain.error.toError
import com.paydock.core.presentation.ui.BaseViewModel
import com.paydock.feature.card.data.api.dto.TokeniseCardRequest
import com.paydock.feature.card.domain.model.TokenisedCardDetails
import com.paydock.feature.card.domain.usecase.TokeniseCreditCardUseCase
import com.paydock.feature.card.presentation.model.CardIssuerType
import com.paydock.feature.card.presentation.model.SecurityCodeType
import com.paydock.feature.card.presentation.utils.CardExpiryValidator
import com.paydock.feature.card.presentation.utils.CardHolderNameValidator
import com.paydock.feature.card.presentation.utils.CardIssuerValidator
import com.paydock.feature.card.presentation.utils.CardSecurityCodeValidator
import com.paydock.feature.card.presentation.utils.CreditCardInputValidator
import com.paydock.feature.card.presentation.utils.CreditCardNumberValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for handling credit card details input and tokenization.
 *
 * @param useCase The tokenization use case for performing card tokenization.
 */
internal class CreditCardDetailsViewModel(
    private val useCase: TokeniseCreditCardUseCase,
    dispatchers: DispatchersProvider
) : BaseViewModel(dispatchers) {

    // Mutable state flow to hold the UI state
    private val _stateFlow: MutableStateFlow<CreditCardDetailsViewState> =
        MutableStateFlow(CreditCardDetailsViewState())

    // Expose a read-only state flow for observing the UI state changes
    val stateFlow: StateFlow<CreditCardDetailsViewState> = _stateFlow.asStateFlow()

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
            state.copy(token = null, error = null)
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

    private fun updateState(update: (CreditCardDetailsViewState) -> CreditCardDetailsViewState) {
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
            val request = TokeniseCardRequest.CreditCard(
                cvv = state.code,
                cardholderName = state.cardholderName,
                cardNumber = state.cardNumber,
                expiryMonth = state.expiryMonth,
                expiryYear = state.expiryYear,
                gatewayId = state.gatewayId
            )
            val result: Result<TokenisedCardDetails> = useCase(request)
            _stateFlow.update { currentState ->
                currentState.copy(
                    error = result.exceptionOrNull()?.toError(),
                    isLoading = false,
                    token = result.getOrNull()?.token
                )
            }
        }
    }
}

/**
 * UI State that represents card details input and processing state.
 */
internal data class CreditCardDetailsViewState(
    val cardholderName: String = "",
    val cardNumber: String = "",
    val expiry: String = "",
    val code: String = "",
    val gatewayId: String = "",
    val token: String? = null,
    val isLoading: Boolean = false,
    val error: ErrorModel? = null,
) {
    private val cardIssuer: CardIssuerType
        get() = CardIssuerValidator.detectCardIssuer(cardNumber)
    private val securityCodeType: SecurityCodeType
        get() = CardSecurityCodeValidator.detectSecurityCodeType(cardIssuer)

    /**
     * Get the validity status of all input data.
     */
    val isDataValid: Boolean
        get() = CardHolderNameValidator.checkHolderName(cardholderName) &&
            CreditCardNumberValidator.checkNumber(cardNumber) && CreditCardInputValidator.isLuhnValid(cardNumber) &&
            CardExpiryValidator.isExpiryValid(expiry) &&
            CardSecurityCodeValidator.isSecurityCodeValid(code, securityCodeType)

    /**
     * Extract the expiry month from the full expiry string.
     */
    internal val expiryMonth: String
        get() = if (expiry.length >= EXPIRY_CHUNK_SIZE) expiry.take(EXPIRY_CHUNK_SIZE) else expiry

    /**
     * Extract the expiry year from the full expiry string.
     */
    internal val expiryYear: String
        get() = if (expiry.length >= MAX_EXPIRY_LENGTH) {
            expiry.drop(EXPIRY_CHUNK_SIZE)
                .take(EXPIRY_CHUNK_SIZE)
        } else {
            ""
        }

}
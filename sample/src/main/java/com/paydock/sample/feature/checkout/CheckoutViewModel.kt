/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 5:58 PM
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

package com.paydock.sample.feature.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.threeDS.domain.model.ThreeDSResult
import com.paydock.sample.BuildConfig
import com.paydock.sample.core.AU_CURRENCY_CODE
import com.paydock.sample.core.CHARGE_TRANSACTION_ERROR
import com.paydock.sample.core.THREE_DS_CARD_ERROR
import com.paydock.sample.core.TOKENISE_CARD_ERROR
import com.paydock.sample.feature.card.data.api.dto.CaptureCardChargeRequest
import com.paydock.sample.feature.card.data.api.dto.VaultTokenRequest
import com.paydock.sample.feature.card.domain.usecase.CaptureCardChargeTokenUseCase
import com.paydock.sample.feature.card.domain.usecase.CreateCardSessionVaultTokenUseCase
import com.paydock.sample.feature.threeDS.data.api.dto.CreateIntegratedThreeDSTokenRequest
import com.paydock.sample.feature.threeDS.domain.model.ThreeDSToken
import com.paydock.sample.feature.threeDS.domain.usecase.CreateIntegratedThreeDSTokenUseCase
import com.paydock.sample.feature.wallet.data.api.dto.Customer
import com.paydock.sample.feature.wallet.data.api.dto.InitiateWalletRequest
import com.paydock.sample.feature.wallet.data.api.dto.PaymentSource
import com.paydock.sample.feature.wallet.domain.usecase.InitiateWalletTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val initiateWalletTransactionUseCase: InitiateWalletTransactionUseCase,
    private val createCardSessionVaultTokenUseCase: CreateCardSessionVaultTokenUseCase,
    private val createIntegratedThreeDSTokenUseCase: CreateIntegratedThreeDSTokenUseCase,
    private val captureCardChargeTokenUseCase: CaptureCardChargeTokenUseCase,
) : ViewModel() {

    private val _stateFlow: MutableStateFlow<CheckoutUIState> = MutableStateFlow(CheckoutUIState())
    val stateFlow: StateFlow<CheckoutUIState> = _stateFlow

    fun resetResultState() {
        _stateFlow.update { state ->
            state.copy(
                cardToken = null,
                vaultToken = null,
                threeDSToken = null,
                chargeResult = null,
                flyPayResult = null,
                error = null
            )
        }
    }

    fun getWalletToken(
        currencyCode: String,
        walletType: String,
        gatewayId: String
    ): (onTokenReceived: (String) -> Unit) -> Unit = { onTokenReceived ->
        resetResultState()
        initiateWalletTransaction(
            currencyCode = currencyCode,
            walletType = walletType,
            gatewayId = gatewayId,
            callback = onTokenReceived
        )
    }

    private fun initiateWalletTransaction(
        currencyCode: String,
        walletType: String,
        gatewayId: String,
        callback: (String) -> Unit
    ) {
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(isLoading = true)
            }
            val request = InitiateWalletRequest(
                currency = currencyCode,
                customer = Customer(
                    paymentSource = PaymentSource(
                        gatewayId = gatewayId,
                        walletType = walletType
                    )
                )
            )
            val result = initiateWalletTransactionUseCase(request)
            result.onSuccess { token ->
                callback(token)
                _stateFlow.update { state ->
                    state.copy(isLoading = false, error = null)
                }
            }
            result.onFailure {
                _stateFlow.update { state ->
                    state.copy(
                        isLoading = false,
                        error = it.message ?: CHARGE_TRANSACTION_ERROR
                    )
                }
            }
        }
    }

    fun handleCardResult(result: Result<String>) {
        result.onSuccess {
            _stateFlow.update { state ->
                state.copy(cardToken = it)
            }
        }.onFailure {
            _stateFlow.update { state ->
                state.copy(error = TOKENISE_CARD_ERROR)
            }
        }
    }

    fun createSessionVaultToken(cardToken: String) {
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(isLoading = true)
            }
            val request = VaultTokenRequest.CreateCardSessionVaultTokenRequest(token = cardToken)
            val result = createCardSessionVaultTokenUseCase(request)
            result.onSuccess { vaultToken ->
                _stateFlow.update { state ->
                    state.copy(vaultToken = vaultToken)
                }
                createIntegrated3dsToken(vaultToken)
            }
            result.onFailure {
                _stateFlow.update { state ->
                    state.copy(
                        isLoading = false,
                        error = it.message ?: TOKENISE_CARD_ERROR
                    )
                }
            }
        }
    }

    private fun createIntegrated3dsToken(vaultToken: String) {
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(isLoading = true)
            }
            val result =
                createIntegratedThreeDSTokenUseCase(
                    CreateIntegratedThreeDSTokenRequest(
                        customer = Customer(
                            paymentSource = PaymentSource(
                                gatewayId = BuildConfig.GATEWAY_ID,
                                vaultToken = vaultToken
                            )
                        )
                    )
                )
            result.onSuccess { threeDSResult ->
                when (threeDSResult.status) {
                    ThreeDSToken.ThreeDSStatus.NOT_SUPPORTED -> captureCardCharge(vaultToken)
                    ThreeDSToken.ThreeDSStatus.PRE_AUTH_PENDING -> {
                        _stateFlow.update { state ->
                            state.copy(
                                threeDSToken = threeDSResult,
                                isLoading = false,
                                error = null
                            )
                        }
                    }

                    else -> _stateFlow.update { state ->
                        state.copy(
                            isLoading = false,
                            error = "Invalid 3DS Status!"
                        )
                    }
                }
            }
            result.onFailure {
                _stateFlow.update { state ->
                    state.copy(
                        isLoading = false,
                        error = it.message ?: THREE_DS_CARD_ERROR
                    )
                }
            }
        }
    }

    fun handleThreeDSResult(vaultToken: String, result: Result<ThreeDSResult>) {
        result.onSuccess {
            _stateFlow.update { state -> state.copy(threeDSToken = null) }
            captureCardCharge(vaultToken)
        }.onFailure {
            _stateFlow.update { state ->
                state.copy(error = CHARGE_TRANSACTION_ERROR)
            }
        }
    }

    private fun captureCardCharge(vaultToken: String) {
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(isLoading = true, threeDSToken = null)
            }
            val request = CaptureCardChargeRequest(
                currency = AU_CURRENCY_CODE, customer = Customer(
                    paymentSource = PaymentSource(
                        vaultToken = vaultToken
                    )
                )
            )
            handleChargeResult(captureCardChargeTokenUseCase(request))
        }
    }

    fun handleChargeResult(result: Result<ChargeResponse>) {
        result.onSuccess {
            _stateFlow.update { state ->
                state.copy(
                    isLoading = false,
                    chargeResult = it,
                    threeDSToken = null,
                    vaultToken = null,
                    cardToken = null
                )
            }
        }.onFailure {
            _stateFlow.update { state ->
                state.copy(error = CHARGE_TRANSACTION_ERROR)
            }
        }
    }

    fun handleFlyPayResult(result: Result<String>) {
        result.onSuccess {
            _stateFlow.update { state ->
                state.copy(
                    isLoading = false,
                    flyPayResult = it,
                    threeDSToken = null,
                    vaultToken = null,
                    cardToken = null
                )
            }
        }.onFailure {
            _stateFlow.update { state ->
                state.copy(error = CHARGE_TRANSACTION_ERROR)
            }
        }
    }
}

data class CheckoutUIState(
    val isLoading: Boolean = false,
    val cardToken: String? = null,
    val vaultToken: String? = null,
    val threeDSToken: ThreeDSToken? = null,
    val chargeResult: ChargeResponse? = null,
    val flyPayResult: String? = null,
    val error: String? = null
) {
    override fun toString(): String {
        return "CheckoutUIState(isLoading=$isLoading, cardToken=$cardToken, vaultToken=$vaultToken, threeDSToken=$threeDSToken, chargeResult=$chargeResult, error=$error)"
    }
}
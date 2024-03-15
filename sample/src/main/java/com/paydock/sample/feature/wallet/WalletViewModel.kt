/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
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

package com.paydock.sample.feature.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paydock.sample.core.CHARGE_TRANSACTION_ERROR
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
class WalletViewModel @Inject constructor(private val initiateWalletTransactionUseCase: InitiateWalletTransactionUseCase) :
    ViewModel() {

    private val _stateFlow: MutableStateFlow<WalletTransactionUIState> =
        MutableStateFlow(WalletTransactionUIState())
    val stateFlow: StateFlow<WalletTransactionUIState> = _stateFlow

    private fun resetResultState() {
        _stateFlow.update { state ->
            state.copy(token = null, error = null)
        }
    }

    private fun initiateWalletTransaction(
        manualCapture: Boolean,
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
            val result = initiateWalletTransactionUseCase(manualCapture = manualCapture, request = request)
            result.onSuccess { charge ->
                val walletToken = charge.walletToken
                if (walletToken != null) {
                    callback(walletToken)
                }
                _stateFlow.update { state ->
                    state.copy(token = walletToken, isLoading = false, error = null)
                }
            }
            result.onFailure {
                _stateFlow.update { state ->
                    state.copy(
                        token = null,
                        isLoading = false,
                        error = it.message ?: CHARGE_TRANSACTION_ERROR
                    )
                }
            }
        }
    }

    fun getWalletToken(
        manualCapture: Boolean = false,
        currencyCode: String,
        walletType: String,
        gatewayId: String
    ): (onTokenReceived: (String) -> Unit) -> Unit = { onTokenReceived ->
        resetResultState()
        initiateWalletTransaction(
            manualCapture = manualCapture,
            currencyCode = currencyCode,
            walletType = walletType,
            gatewayId = gatewayId,
            callback = onTokenReceived
        )
    }
}

data class WalletTransactionUIState(
    val isLoading: Boolean = false,
    val token: String? = null,
    val error: String? = null
)
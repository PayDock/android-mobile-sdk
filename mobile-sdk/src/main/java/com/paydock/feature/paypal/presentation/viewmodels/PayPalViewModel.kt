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

package com.paydock.feature.paypal.presentation.viewmodels

import android.net.Uri
import com.paydock.core.PAY_PAL_REDIRECT_PARAM_VALUE
import com.paydock.core.REDIRECT_PARAM_NAME
import com.paydock.core.TYPE_CREATE_TRANSACTION
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.toError
import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.paypal.presentation.state.PayPalData
import com.paydock.feature.paypal.presentation.state.PayPalViewState
import com.paydock.feature.wallet.data.api.dto.Customer
import com.paydock.feature.wallet.data.api.dto.PaymentSource
import com.paydock.feature.wallet.data.api.dto.WalletCallbackRequest
import com.paydock.feature.wallet.data.api.dto.WalletCaptureRequest
import com.paydock.feature.wallet.domain.model.WalletCallback
import com.paydock.feature.wallet.domain.usecase.CaptureWalletTransactionUseCase
import com.paydock.feature.wallet.domain.usecase.GetWalletCallbackUseCase
import com.paydock.feature.wallet.presentation.viewmodels.WalletViewModel

/**
 * ViewModel responsible for managing PayPal payment-related data and state.
 *
 * @param captureWalletTransactionUseCase The use case responsible for capturing wallet transactions.
 * @param getWalletCallbackUseCase The use case getting wallet callback details.
 * @param dispatchers Provides the coroutine dispatchers for handling asynchronous tasks.
 */
internal class PayPalViewModel(
    captureWalletTransactionUseCase: CaptureWalletTransactionUseCase,
    getWalletCallbackUseCase: GetWalletCallbackUseCase,
    dispatchers: DispatchersProvider
) : WalletViewModel<PayPalViewState>(
    captureWalletTransactionUseCase,
    getWalletCallbackUseCase,
    dispatchers
) {
    /**
     * Creates the initial state for the PayPal UI.
     *
     * @return Initial state of the PayPal UI.
     */
    override fun createInitialState(): PayPalViewState = PayPalViewState()

    /**
     * Sets the PayPal authentication token in the UI state.
     *
     * @param token The PayPal authentication token.
     */
    override fun setWalletToken(token: String) {
        updateState { state ->
            state.copy(token = token)
        }
    }

    /**
     * Resets the result state for PayPal, clearing payment data and error information.
     */
    override fun resetResultState() {
        updateState { state ->
            state.copy(
                token = null,
                callbackData = null,
                chargeData = null,
                paymentData = null,
                error = null,
                isLoading = false
            )
        }
    }

    /**
     * Sets the PayPal UI state to indicate loading.
     */
    override fun setLoadingState() {
        updateState { state -> state.copy(isLoading = true) }
    }

    /**
     * Updates the PayPal UI state based on the result of fetching wallet callback data.
     *
     * @param result The result of fetching wallet callback data.
     */
    override fun updateCallbackUIState(result: Result<WalletCallback?>) {
        updateState { currentState ->
            currentState.copy(
                error = result.exceptionOrNull()?.toError(),
                isLoading = false,
                callbackData = result.getOrNull()
            )
        }
    }

    /**
     * Updates the PayPal UI state based on the result of capturing a wallet transaction.
     *
     * @param result The result of capturing a wallet transaction.
     */
    override fun updateChargeUIState(result: Result<ChargeResponse>) {
        updateState { currentState ->
            currentState.copy(
                error = result.exceptionOrNull()?.toError(),
                isLoading = false,
                chargeData = result.getOrNull()
            )
        }
    }

    /**
     * Initiates the process to retrieve the PayPal wallet callback data based on the provided parameters.
     *
     * @param walletToken The PayPal authentication token.
     * @param requestShipping A boolean flag indicating if shipping information is requested for the PayPal wallet callback.
     */
    fun getWalletCallback(walletToken: String, requestShipping: Boolean) {
        // Create a request object for fetching PayPal wallet callback data
        val request = WalletCallbackRequest(
            type = TYPE_CREATE_TRANSACTION,
            shipping = requestShipping,
            walletType = "paypal"
        )
        getWalletCallback(walletToken, request)
    }

    /**
     * Capture a PayPal wallet transaction using the provided PayPal token and reference token.
     *
     * @param walletToken The PayPal authentication token.
     * @param paymentMethodId The ID of the payment method to be used for the PayPal transaction.
     * @param payerId The external payer ID for the payment source, if available.
     */
    fun captureWalletTransaction(
        walletToken: String,
        paymentMethodId: String? = null,
        payerId: String? = null
    ) {
        // Create a request object for capturing PayPal wallet transaction
        val request = WalletCaptureRequest(
            paymentMethodId = paymentMethodId,
            customer = Customer(
                paymentSource = PaymentSource(
                    externalPayerId = payerId
                )
            )
        )
        captureWalletTransaction(walletToken, request)
    }

    /**
     * Creates the PayPal URL for the payment process based on the callback URL.
     *
     * @param callbackUrl The URL for the PayPal payment callback.
     * @return The composed URL with PayPal parameters.
     */
    fun createPayPalUrl(callbackUrl: String): String =
        "$callbackUrl&$REDIRECT_PARAM_NAME=$PAY_PAL_REDIRECT_PARAM_VALUE"

    /**
     * Parses the PayPal URL to extract payment data, such as the PayPal token and PayerID.
     *
     * @param requestUrl The URL returned from the PayPal payment process.
     */
    fun parsePayPalUrl(requestUrl: String) {
        val requestUri = Uri.parse(requestUrl)
        val payPalToken = requestUri.getQueryParameter("token")
        val payerId = requestUri.getQueryParameter("PayerID")
        if (payPalToken != null && payerId != null) {
            updateState { state ->
                state.copy(paymentData = PayPalData(payPalToken, payerId), isLoading = false)
            }
        }
    }
}

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

package com.paydock.feature.googlepay.presentation.viewmodels

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.toError
import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.googlepay.presentation.state.GooglePayViewState
import com.paydock.feature.wallet.data.api.dto.WalletCaptureRequest
import com.paydock.feature.wallet.domain.usecase.CaptureWalletTransactionUseCase
import com.paydock.feature.wallet.domain.usecase.GetWalletCallbackUseCase
import com.paydock.feature.wallet.presentation.viewmodels.WalletViewModel
import org.json.JSONObject

/**
 * ViewModel responsible for managing the Google Pay feature's UI state and interactions.
 *
 * @param paymentsClient An instance of PaymentsClient for handling Google Pay payments.
 * @param captureWalletTransactionUseCase The use case responsible for capturing wallet transactions.
 * @param getWalletCallbackUseCase The use case getting wallet callback details.
 * @param dispatchers The provider for coroutine dispatchers.
 */
internal class GooglePayViewModel(
    private val paymentsClient: PaymentsClient,
    captureWalletTransactionUseCase: CaptureWalletTransactionUseCase,
    getWalletCallbackUseCase: GetWalletCallbackUseCase,
    dispatchers: DispatchersProvider
) : WalletViewModel<GooglePayViewState>(captureWalletTransactionUseCase, getWalletCallbackUseCase, dispatchers) {

    /**
     * Creates the initial state for the Google Pay feature.
     */
    override fun createInitialState(): GooglePayViewState = GooglePayViewState()

    /**
     * Sets the wallet authentication token in the state.
     *
     * @param token The wallet authentication token.
     */
    override fun setWalletToken(token: String) {
        updateState { state ->
            state.copy(token = token)
        }
    }

    /**
     * Resets the result state, clearing payment data and error information.
     */
    override fun resetResultState() {
        updateState { state ->
            state.copy(paymentData = null, chargeData = null, token = null, error = null, isLoading = false)
        }
    }

    /**
     * Sets the loading state in the UI.
     */
    override fun setLoadingState() {
        updateState { state -> state.copy(isLoading = true) }
    }

    /**
     * Updates the UI state with the result of a wallet charge operation.
     *
     * @param result The result of the wallet charge operation.
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
     * Initiates a Google Pay payment with the provided transaction details.
     *
     * @param request A JSON object containing transaction details.
     * @see [loadPaymentData](https://developers.google.com/android/reference/com/google/android/gms/wallet/PaymentsClient#loadPaymentData(com.google.android.gms.wallet.PaymentDataRequest))
     */
    fun requestPayment(request: JSONObject?) {
        updateState { state -> state.copy(isLoading = true) }

        // Use PaymentsClient to load payment data asynchronously
        val task = paymentsClient.loadPaymentData(PaymentDataRequest.fromJson(request.toString()))

        // Handle the completion of the task
        task.addOnCompleteListener { completedTask ->
            if (completedTask.isSuccessful) {
                // Update UI state with successful payment data
                val paymentData = completedTask.result
                updateState { state ->
                    state.copy(paymentData = paymentData, isLoading = false)
                }
            } else {
                // Update UI state with payment failure
                updateState { state ->
                    state.copy(error = completedTask.exception?.toError(), isLoading = false)
                }
            }
        }
    }

    /**
     * Checks if the user can use Google Pay to make a payment with a supported payment method.
     *
     * @param request A JSON object containing payment readiness details.
     */
    fun fetchCanUseGooglePay(request: JSONObject?) {
        // Check if the request is not null
        if (request != null) {
            // Update UI state to indicate loading
            updateState { state -> state.copy(isLoading = true) }

            // Use PaymentsClient to check if Google Pay is available
            val task = paymentsClient.isReadyToPay(IsReadyToPayRequest.fromJson(request.toString()))

            // Handle the completion of the task
            task.addOnCompleteListener { completedTask ->
                try {
                    // Update UI state with Google Pay availability
                    updateState { state ->
                        state.copy(
                            googlePayAvailable = completedTask.getResult(ApiException::class.java),
                            isLoading = false
                        )
                    }
                } catch (exception: ApiException) {
                    // Update UI state with error if an exception occurs
                    updateState { state ->
                        state.copy(error = exception.toError(), isLoading = false)
                    }
                }
            }
        } else {
            // If the request is null, set Google Pay availability to false
            updateState { state ->
                state.copy(googlePayAvailable = false)
            }
        }
    }

    /**
     * Captures a wallet transaction using the provided wallet token and Google Pay token.
     *
     * @param walletToken The wallet authentication token.
     * @param googleToken The Google Pay token obtained during the payment process.
     */
    fun captureWalletTransaction(
        walletToken: String,
        googleToken: String
    ) {
        // Create a request object for capturing wallet transaction
        val request = WalletCaptureRequest(
            paymentMethodId = googleToken
        )

        // Call the base class method to perform the wallet transaction
        captureWalletTransaction(walletToken, request)
    }
}
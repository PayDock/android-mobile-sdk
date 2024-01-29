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

package com.paydock.feature.flypay.presentation.viewmodels

import com.paydock.core.FLY_PAY_REDIRECT_URL
import com.paydock.core.TYPE_CREATE_SESSION
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.toError
import com.paydock.feature.flypay.presentation.state.FlyPayViewState
import com.paydock.feature.wallet.data.api.dto.WalletCallbackRequest
import com.paydock.feature.wallet.domain.model.WalletCallback
import com.paydock.feature.wallet.domain.usecase.CaptureWalletTransactionUseCase
import com.paydock.feature.wallet.domain.usecase.GetWalletCallbackUseCase
import com.paydock.feature.wallet.presentation.viewmodels.WalletViewModel

/**
 * ViewModel responsible for managing the FlyPay feature's UI state and interactions.
 *
 * @param captureWalletTransactionUseCase The use case responsible for capturing wallet transactions.
 * @param getWalletCallbackUseCase The use case getting wallet callback details.
 * @param dispatchers Provides the coroutine dispatchers for handling asynchronous tasks.
 */
internal class FlyPayViewModel(
    captureWalletTransactionUseCase: CaptureWalletTransactionUseCase,
    getWalletCallbackUseCase: GetWalletCallbackUseCase,
    dispatchers: DispatchersProvider
) : WalletViewModel<FlyPayViewState>(
    captureWalletTransactionUseCase,
    getWalletCallbackUseCase,
    dispatchers
) {
    override fun createInitialState(): FlyPayViewState = FlyPayViewState()

    /**
     * Sets the wallet token in the current state.
     *
     * @param token The wallet authentication token.
     */
    override fun setWalletToken(token: String) {
        updateState { state ->
            state.copy(token = token)
        }
    }

    /**
     * Resets the result state, clearing token and error information.
     */
    override fun resetResultState() {
        updateState { state ->
            state.copy(token = null, callbackData = null, error = null, isLoading = false)
        }
    }

    /**
     * Sets the loading state in the current state.
     */
    override fun setLoadingState() {
        updateState { state -> state.copy(isLoading = true) }
    }

    /**
     * Updates the UI state with the result of the wallet callback operation.
     *
     * @param result The result of the wallet callback operation.
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
     * Initiates the process to retrieve the wallet callback data based on the provided parameters.
     *
     * @param walletToken The wallet authentication token.
     */
    fun getWalletCallback(walletToken: String) {
        // Create a request object for fetching wallet callback data
        val request = WalletCallbackRequest(type = TYPE_CREATE_SESSION, walletType = "flypay")
        getWalletCallback(walletToken, request)
    }

    /**
     * Creates the FlyPay URL for the payment process based on the callback URL.
     *
     * @param flyPayOrderId The FlyPay orderId.
     * @return The composed URL with FlyPay parameters.
     */
    fun createFlyPayUrl(flyPayOrderId: String): String =
        "https://checkout.release.cxbflypay.com.au/?orderId=$flyPayOrderId&redirectUrl=$FLY_PAY_REDIRECT_URL" // This is the sandbox URL
}

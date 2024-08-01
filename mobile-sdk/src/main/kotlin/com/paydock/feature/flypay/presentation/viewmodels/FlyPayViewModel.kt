package com.paydock.feature.flypay.presentation.viewmodels

import com.paydock.MobileSDK
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.FlyPayException
import com.paydock.core.domain.model.Environment
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.exceptions.UnknownApiException
import com.paydock.feature.flypay.presentation.state.FlyPayViewState
import com.paydock.feature.wallet.data.api.dto.WalletCallbackRequest
import com.paydock.feature.wallet.domain.model.WalletCallback
import com.paydock.feature.wallet.domain.model.WalletType
import com.paydock.feature.wallet.domain.usecase.CaptureWalletTransactionUseCase
import com.paydock.feature.wallet.domain.usecase.DeclineWalletTransactionUseCase
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
    declineWalletTransactionUseCase: DeclineWalletTransactionUseCase,
    getWalletCallbackUseCase: GetWalletCallbackUseCase,
    dispatchers: DispatchersProvider
) : WalletViewModel<FlyPayViewState>(
    captureWalletTransactionUseCase,
    declineWalletTransactionUseCase,
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
    override fun updateCallbackUIState(result: Result<WalletCallback>) {
        updateState { currentState ->
            val exception: Throwable? = result.exceptionOrNull()
            val error: FlyPayException? = when (exception) {
                is ApiException -> FlyPayException.FetchingUrlException(error = exception.error)
                is UnknownApiException -> FlyPayException.UnknownException(displayableMessage = exception.errorMessage)
                else -> currentState.error
            }
            currentState.copy(
                error = error,
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
        val request =
            WalletCallbackRequest(
                type = MobileSDKConstants.WalletCallbackType.TYPE_CREATE_SESSION,
                walletType = WalletType.FLY_PAY.type
            )
        getWalletCallback(walletToken, request)
    }

    /**
     * Creates the FlyPay URL for the payment process based on the callback URL.
     *
     * @param flyPayOrderId The FlyPay orderId.
     * @return The composed URL with FlyPay parameters.
     */
    @Suppress("MaxLineLength")
    fun createFlyPayUrl(flyPayOrderId: String): String =
        when (MobileSDK.getInstance().environment) {
            Environment.PRODUCTION -> "https://checkout.cxbflypay.com.au/?orderId=$flyPayOrderId&redirectUrl=${MobileSDKConstants.FlyPayConfig.FLY_PAY_REDIRECT_URL}"
            else -> "https://checkout.sandbox.cxbflypay.com.au/?orderId=$flyPayOrderId&redirectUrl=${MobileSDKConstants.FlyPayConfig.FLY_PAY_REDIRECT_URL}" // default to sandbox
        }
}

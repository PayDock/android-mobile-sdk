package com.paydock.feature.flypay.presentation.viewmodels

import com.paydock.api.charges.data.dto.WalletCallbackRequest
import com.paydock.api.charges.domain.model.WalletCallback
import com.paydock.api.charges.domain.model.WalletType
import com.paydock.api.charges.domain.usecase.CaptureWalletChargeUseCase
import com.paydock.api.charges.domain.usecase.DeclineWalletChargeUseCase
import com.paydock.api.charges.domain.usecase.GetWalletCallbackUseCase
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.FlyPayException
import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.core.domain.error.extensions.mapApiException
import com.paydock.feature.flypay.presentation.state.FlyPayUIState
import com.paydock.feature.wallet.presentation.viewmodels.WalletViewModel

/**
 * ViewModel responsible for managing the FlyPay feature's UI state and interactions.
 *
 * This ViewModel oversees the FlyPay wallet integration, handling tasks such as retrieving
 * wallet callback data, processing transactions, and managing UI states. It extends
 * `WalletViewModel` and leverages specific use cases for interacting with FlyPay-related services.
 *
 * @property clientId The client ID used for authenticating or identifying the FlyPay transaction.
 * @param captureWalletChargeUseCase Use case for capturing charges associated with FlyPay wallets.
 * @param declineWalletChargeUseCase Use case for declining charges associated with FlyPay wallets.
 * @param getWalletCallbackUseCase Use case for fetching callback data from the FlyPay wallet service.
 * @param dispatchers Dispatcher provider to manage coroutine contexts for background operations.
 */
internal class FlyPayViewModel(
    val clientId: String,
    captureWalletChargeUseCase: CaptureWalletChargeUseCase,
    declineWalletChargeUseCase: DeclineWalletChargeUseCase,
    getWalletCallbackUseCase: GetWalletCallbackUseCase,
    dispatchers: DispatchersProvider
) : WalletViewModel<FlyPayUIState>(
    captureWalletChargeUseCase,
    declineWalletChargeUseCase,
    getWalletCallbackUseCase,
    dispatchers
) {

    // Holds the wallet token used for FlyPay operations
    private var walletToken: String? = null

    /**
     * Provides the initial state for the FlyPay UI.
     *
     * This method is invoked when the ViewModel is initialized or reset, ensuring that the
     * FlyPay UI starts in an idle state.
     *
     * @return The initial UI state, which is `FlyPayUIState.Idle`.
     */
    override fun createInitialState(): FlyPayUIState = FlyPayUIState.Idle

    /**
     * Stores the FlyPay wallet token for subsequent API interactions.
     *
     * @param token The wallet token, typically required for authentication or session management.
     */
    override fun setWalletToken(token: String) {
        walletToken = token
    }

    /**
     * Resets the FlyPay result state, clearing any payment or error-related information.
     *
     * This method ensures the UI returns to its initial idle state, ready for new interactions.
     */
    override fun resetResultState() {
        walletToken = null
        updateState { FlyPayUIState.Idle }
    }

    /**
     * Updates the FlyPay UI state to indicate a loading state.
     *
     * This is typically invoked during operations that require user feedback, such as API calls.
     */
    override fun setLoadingState() {
        updateState { FlyPayUIState.Loading }
    }

    /**
     * Updates the FlyPay UI state based on the result of a wallet callback data fetch operation.
     *
     * - On success, the state is updated with the retrieved callback data.
     * - On failure, an error state is set, containing a mapped `SdkException`.
     *
     * @param result The result of the operation, containing either the callback data or an error.
     */
    override fun updateCallbackUIState(result: Result<WalletCallback>) {
        result.onSuccess { chargeData ->
            updateState {
                FlyPayUIState.LaunchIntent(chargeData)
            }
        }.onFailure { throwable ->
            val error: SdkException = throwable.mapApiException<FlyPayException.FetchingUrlException>()
            updateState { FlyPayUIState.Error(error) }
        }
    }

    /**
     * Initiates a fetch operation for wallet callback data using the provided wallet token.
     *
     * This operation constructs a request object and invokes the appropriate use case to
     * retrieve session or callback data required for FlyPay wallet operations.
     *
     * @param walletToken The FlyPay wallet token used to authenticate and fetch the callback data.
     */
    fun getWalletCallback(walletToken: String) {
        val request = WalletCallbackRequest(
            type = MobileSDKConstants.WalletCallbackType.TYPE_CREATE_SESSION,
            walletType = WalletType.FLY_PAY.type
        )
        getWalletCallback(walletToken, request)
    }

    /**
     * Completes the FlyPay operation by updating the UI state with a success result.
     *
     * This method is typically called when a FlyPay transaction is successfully processed,
     * and the operation is finalized with an order ID.
     *
     * @param orderId The ID of the successfully processed order.
     */
    fun completeResult(orderId: String) {
        updateState {
            FlyPayUIState.Success(orderId)
        }
    }
}
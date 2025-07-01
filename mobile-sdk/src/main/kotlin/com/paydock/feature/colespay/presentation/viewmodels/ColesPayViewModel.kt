package com.paydock.feature.colespay.presentation.viewmodels

import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.ColesPayException
import com.paydock.core.domain.error.extensions.mapApiException
import com.paydock.feature.colespay.integration.ColesPayWidgetConfig
import com.paydock.feature.colespay.presentation.state.ColesPayUIState
import com.paydock.feature.wallet.data.dto.WalletCallbackRequest
import com.paydock.feature.wallet.domain.model.integration.WalletTokenResult
import com.paydock.feature.wallet.domain.model.ui.WalletCallback
import com.paydock.feature.wallet.domain.usecase.CaptureWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.DeclineWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.GetWalletCallbackUseCase
import com.paydock.feature.wallet.presentation.viewmodels.WalletViewModel

/**
 * ViewModel responsible for managing the Coles Pay feature's UI state and interactions.
 *
 * This ViewModel oversees the Coles Pay wallet integration, handling tasks such as retrieving
 * wallet callback data, processing transactions, and managing UI states. It extends
 * `WalletViewModel` and leverages specific use cases for interacting with Coles Pay-related services.
 *
 * @property config The client ID used for authenticating or identifying the Coles Pay transaction.
 * @param captureWalletChargeUseCase Use case for capturing charges associated with Coles Pay wallets.
 * @param declineWalletChargeUseCase Use case for declining charges associated with Coles Pay wallets.
 * @param getWalletCallbackUseCase Use case for fetching callback data from the Coles Pay wallet service.
 * @param dispatchers Dispatcher provider to manage coroutine contexts for background operations.
 */
internal class ColesPayViewModel(
    val config: ColesPayWidgetConfig,
    captureWalletChargeUseCase: CaptureWalletChargeUseCase,
    declineWalletChargeUseCase: DeclineWalletChargeUseCase,
    getWalletCallbackUseCase: GetWalletCallbackUseCase,
    dispatchers: DispatchersProvider
) : WalletViewModel<ColesPayUIState>(
    captureWalletChargeUseCase,
    declineWalletChargeUseCase,
    getWalletCallbackUseCase,
    dispatchers
) {

    //region Private Properties
    /**
     * Holds the wallet token used for Coles Pay operations.
     *
     * This token is essential for authenticating and managing Coles Pay transactions.
     */
    private var walletToken: String? = null
    //endregion

    //region Overridden Methods
    /**
     * Provides the initial state for the Coles Pay UI.
     *
     * This method is invoked when the ViewModel is initialized or reset, ensuring that the
     * Coles Pay UI starts in an idle state.
     *
     * @return The initial UI state, which is `Coles PayUIState.Idle`.
     */
    override fun createInitialState(): ColesPayUIState = ColesPayUIState.Idle

    /**
     * Stores the Coles Pay wallet token for subsequent API interactions.
     *
     * @param token The wallet token, typically required for authentication or session management.
     */
    override fun setWalletToken(token: String) {
        walletToken = token
    }

    /**
     * Resets the Coles Pay result state, clearing any payment or error-related information.
     *
     * This method ensures the UI returns to its initial idle state, ready for new interactions.
     */
    override fun resetResultState() {
        walletToken = null
        updateUiState(ColesPayUIState.Idle)
    }

    /**
     * Updates the Coles Pay UI state to indicate a loading state.
     *
     * This is typically invoked during operations that require user feedback, such as API calls.
     */
    override fun setLoadingState() {
        updateUiState(ColesPayUIState.Loading)
    }

    /**
     * Updates the Coles Pay UI state based on the result of a wallet callback data fetch operation.
     *
     * - On success, the state is updated with the retrieved callback data.
     * - On failure, an error state is set, containing a mapped `SdkException`.
     *
     * @param result The result of the operation, containing either the callback data or an error.
     */
    override fun updateCallbackUIState(result: Result<WalletCallback>) {
        result.fold(
            onSuccess = { chargeData ->
                updateUiState(ColesPayUIState.LaunchIntent(chargeData))
            },
            onFailure = { throwable ->
                updateUiState(
                    ColesPayUIState.Error(
                        throwable.mapApiException(ColesPayException.FetchingUrlException::class)
                    )
                )
            }
        )
    }
    //endregion

    //region Public Methods

    /**
     * Initiates the Coles Pay flow by first obtaining a token and then fetching wallet callback data.
     *
     * This function serves as the entry point for starting a Coles Pay transaction. It sets the UI to a loading state,
     * then uses the provided `tokenProvider` to asynchronously fetch a wallet token.
     *
     * If the token fetch is successful:
     * - The received token is stored using `setWalletToken`.
     * - `getWalletCallback` is triggered with the obtained token to retrieve the necessary
     *   session or callback information for the Coles Pay wallet.
     *
     * If the token fetch fails:
     * - The UI state is updated to reflect an error, specifically `ColesPayException.InitialisationWalletTokenException`.
     *   The error message will be the message from the throwable or a default wallet token error message.
     *
     * @param tokenProvider A higher-order function that takes a callback `(Result<WalletTokenResult>) -> Unit` as an argument.
     *                      This provider is responsible for asynchronously fetching the wallet token and
     *                      invoking the `tokenResult` callback with the result of the token fetching operation.
     *                      The result will be either a `Success` containing `WalletTokenResult` or a `Failure`
     *                      containing a `Throwable`.
     */
    fun startColesPayFlow(tokenProvider: (tokenResult: (Result<WalletTokenResult>) -> Unit) -> Unit) {
        setLoadingState()
        tokenProvider.invoke { tokenResult ->
            tokenResult.onSuccess { result ->
                setWalletToken(result.token)
                getWalletCallback(walletToken = result.token)
            }.onFailure { throwable ->
                updateUiState(
                    ColesPayUIState.Error(
                        ColesPayException.InitialisationWalletTokenException(
                            throwable.message ?: MobileSDKConstants.GooglePayConfig.Errors.WALLET_TOKEN_ERROR
                        )
                    )
                )
            }
        }
    }

    /**
     * Initiates a fetch operation for wallet callback data using the provided wallet token.
     *
     * This operation constructs a request object and invokes the appropriate use case to
     * retrieve session or callback data required for Coles Pay wallet operations.
     *
     * @param walletToken The Coles Pay wallet token used to authenticate and fetch the callback data.
     */
    fun getWalletCallback(walletToken: String) {
        val request = WalletCallbackRequest(
            type = MobileSDKConstants.WalletCallbackType.TYPE_CREATE_SESSION
        )
        getWalletCallback(walletToken, request)
    }

    /**
     * Completes the Coles Pay operation by updating the UI state with a success result.
     *
     * This method is typically called when a Coles Pay transaction is successfully processed,
     * and the operation is finalized with an order ID.
     *
     * @param orderId The ID of the successfully processed order.
     */
    fun completeResult(orderId: String) {
        updateUiState(ColesPayUIState.Success(orderId))
    }
    //endregion
}
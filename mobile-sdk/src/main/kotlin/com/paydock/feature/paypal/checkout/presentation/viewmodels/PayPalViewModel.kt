package com.paydock.feature.paypal.checkout.presentation.viewmodels

import androidx.core.net.toUri
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.PayPalException
import com.paydock.core.domain.error.extensions.mapApiException
import com.paydock.feature.paypal.checkout.presentation.state.PayPalCheckoutUIState
import com.paydock.feature.wallet.data.dto.CaptureWalletChargeRequest
import com.paydock.feature.wallet.data.dto.CustomerData
import com.paydock.feature.wallet.data.dto.PaymentSourceData
import com.paydock.feature.wallet.data.dto.WalletCallbackRequest
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.model.ui.WalletCallback
import com.paydock.feature.wallet.domain.usecase.CaptureWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.DeclineWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.GetWalletCallbackUseCase
import com.paydock.feature.wallet.presentation.viewmodels.WalletViewModel

/**
 * ViewModel for managing the PayPal checkout process in the mobile SDK.
 *
 * This ViewModel handles various aspects of the PayPal flow, such as retrieving wallet callback data,
 * capturing wallet transactions, managing UI states, and parsing PayPal URLs. It extends
 * `WalletViewModel` and leverages use cases for interacting with PayPal services.
 *
 * @param captureWalletChargeUseCase Use case for capturing wallet charges.
 * @param declineWalletChargeUseCase Use case for declining wallet charges.
 * @param getWalletCallbackUseCase Use case for retrieving wallet callback data.
 * @param dispatchers Dispatcher provider for managing coroutine contexts.
 */
internal class PayPalViewModel(
    captureWalletChargeUseCase: CaptureWalletChargeUseCase,
    declineWalletChargeUseCase: DeclineWalletChargeUseCase,
    getWalletCallbackUseCase: GetWalletCallbackUseCase,
    dispatchers: DispatchersProvider,
) : WalletViewModel<PayPalCheckoutUIState>(
    captureWalletChargeUseCase,
    declineWalletChargeUseCase,
    getWalletCallbackUseCase,
    dispatchers
) {

    //region Private Properties
    /**
     * Holds the wallet token used for PayPal operations.
     *
     * This token is essential for authenticating and managing PayPal transactions.
     */
    private var walletToken: String? = null
    //endregion

    //region Overridden Methods
    /**
     * Provides the initial state for the PayPal UI.
     *
     * @return The initial UI state, which is `PayPalCheckoutUIState.Idle`.
     */
    override fun createInitialState(): PayPalCheckoutUIState = PayPalCheckoutUIState.Idle

    /**
     * Sets the PayPal wallet token used for authentication and transaction processing.
     *
     * @param token The wallet token.
     */
    override fun setWalletToken(token: String) {
        walletToken = token
    }

    /**
     * Resets the PayPal result state, clearing payment and error information.
     *
     * Updates the state to `PayPalCheckoutUIState.Idle`.
     */
    override fun resetResultState() {
        walletToken = null
        updateUiState(PayPalCheckoutUIState.Idle)
    }

    /**
     * Sets the PayPal UI to a loading state.
     *
     * Updates the state to `PayPalCheckoutUIState.Loading`.
     */
    override fun setLoadingState() {
        updateUiState(PayPalCheckoutUIState.Loading)
    }

    /**
     * Updates the PayPal UI state with the result of fetching wallet callback data.
     *
     * @param result The result containing the wallet callback data or an error.
     */
    override fun updateCallbackUIState(result: Result<WalletCallback>) {
        result.fold(
            onSuccess = { chargeData ->
                updateUiState(PayPalCheckoutUIState.LaunchIntent(chargeData))
            },
            onFailure = { throwable ->
                updateUiState(PayPalCheckoutUIState.Error(throwable.mapApiException(PayPalException.FetchingUrlException::class)))
            }
        )
    }

    /**
     * Updates the PayPal UI state with the result of capturing a wallet transaction.
     *
     * @param result The result containing the charge response or an error.
     */
    override fun updateChargeUIState(result: Result<ChargeResponse>) {
        result.fold(
            onSuccess = { chargeData ->
                updateUiState(PayPalCheckoutUIState.Success(chargeData))
            },
            onFailure = { throwable ->
                updateUiState(PayPalCheckoutUIState.Error(throwable.mapApiException(PayPalException.CapturingChargeException::class)))
            }
        )
    }
    //endregion

    //region Public Methods

    /**
     * Initiates the PayPal checkout flow by obtaining a wallet token and fetching wallet callback data.
     *
     * This function serves as the entry point for starting the PayPal checkout process.
     * It first uses the provided [tokenProvider] to asynchronously retrieve a wallet token.
     * Once the token is obtained, it's set using [setWalletToken] and then used to fetch
     * the wallet callback information via [getWalletCallback].
     *
     * @param tokenProvider A suspend function that takes a callback `(String) -> Unit` and
     *                      invokes it with the obtained wallet token. This allows for asynchronous
     *                      token retrieval.
     * @param requestShipping A boolean flag indicating whether shipping information should be
     *                        requested during the PayPal flow.
     */
    fun startPayPalFlow(
        tokenProvider: (onTokenReceived: (String) -> Unit) -> Unit,
        requestShipping: Boolean
    ) {
        tokenProvider { obtainedToken ->
            setWalletToken(obtainedToken)
            getWalletCallback(walletToken = obtainedToken, requestShipping = requestShipping)
        }
    }

    /**
     * Fetches wallet callback data using the wallet token and additional parameters.
     *
     * @param walletToken The PayPal wallet token.
     * @param requestShipping Indicates whether shipping information should be requested.
     */
    fun getWalletCallback(walletToken: String, requestShipping: Boolean) {
        val request = WalletCallbackRequest(
            type = MobileSDKConstants.WalletCallbackType.TYPE_CREATE_TRANSACTION,
            shipping = requestShipping
        )
        getWalletCallback(walletToken, request)
    }

    /**
     * Captures a PayPal wallet transaction using the provided payment method ID and payer ID.
     *
     * @param paymentMethodId The ID of the payment method to be used (optional).
     * @param payerId The external payer ID for the payment source (optional).
     */
    fun captureWalletTransaction(
        paymentMethodId: String? = null,
        payerId: String? = null,
    ) {
        val request = CaptureWalletChargeRequest(
            paymentMethodId = paymentMethodId,
            customer = CustomerData(
                paymentSource = PaymentSourceData(
                    externalPayerId = payerId
                )
            )
        )
        walletToken?.let {
            captureWalletTransaction(it, request)
        }
    }

    /**
     * Parses a PayPal URL to extract payment data such as the PayPal token and payer ID.
     *
     * Updates the state to `PayPalCheckoutUIState.Capture` if the required parameters are found.
     *
     * @param requestUrl The URL returned from the PayPal payment process.
     */
    fun parsePayPalUrl(requestUrl: String) {
        val requestUri = requestUrl.toUri()
        val payPalToken = requestUri.getQueryParameter(MobileSDKConstants.PayPalConfig.TOKEN_KEY)
        val payerId = if (requestUrl.contains(MobileSDKConstants.PayPalConfig.PAYER_ID_KEY)) {
            requestUri.getQueryParameter(MobileSDKConstants.PayPalConfig.PAYER_ID_KEY)
        } else {
            requestUri.getQueryParameter(MobileSDKConstants.PayPalConfig.FLOW_ID_KEY)
        }
        if (payPalToken != null && payerId != null) {
            updateUiState(PayPalCheckoutUIState.Capture(payPalToken, payerId))
        }
    }
    //endregion
}
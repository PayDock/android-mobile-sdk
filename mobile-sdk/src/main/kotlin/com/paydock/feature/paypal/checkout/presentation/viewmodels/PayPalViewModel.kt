package com.paydock.feature.paypal.checkout.presentation.viewmodels

import android.net.Uri
import com.paydock.api.charges.data.dto.CaptureWalletChargeRequest
import com.paydock.api.charges.data.dto.CustomerData
import com.paydock.api.charges.data.dto.PaymentSourceData
import com.paydock.api.charges.data.dto.WalletCallbackRequest
import com.paydock.api.charges.domain.model.WalletCallback
import com.paydock.api.charges.domain.model.WalletType
import com.paydock.api.charges.domain.usecase.CaptureWalletChargeUseCase
import com.paydock.api.charges.domain.usecase.DeclineWalletChargeUseCase
import com.paydock.api.charges.domain.usecase.GetWalletCallbackUseCase
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.PayPalException
import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.core.domain.error.extensions.mapApiException
import com.paydock.feature.charge.domain.model.integration.ChargeResponse
import com.paydock.feature.paypal.checkout.presentation.state.PayPalCheckoutUIState
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

    private var walletToken: String? = null

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
        updateState { PayPalCheckoutUIState.Idle }
    }

    /**
     * Sets the PayPal UI to a loading state.
     *
     * Updates the state to `PayPalCheckoutUIState.Loading`.
     */
    override fun setLoadingState() {
        updateState { PayPalCheckoutUIState.Loading }
    }

    /**
     * Updates the PayPal UI state with the result of fetching wallet callback data.
     *
     * @param result The result containing the wallet callback data or an error.
     */
    override fun updateCallbackUIState(result: Result<WalletCallback>) {
        result.onSuccess { chargeData ->
            updateState {
                PayPalCheckoutUIState.LaunchIntent(chargeData)
            }
        }.onFailure { throwable ->
            val error: SdkException = throwable.mapApiException<PayPalException.FetchingUrlException>()
            updateState { PayPalCheckoutUIState.Error(error) }
        }
    }

    /**
     * Updates the PayPal UI state with the result of capturing a wallet transaction.
     *
     * @param result The result containing the charge response or an error.
     */
    override fun updateChargeUIState(result: Result<ChargeResponse>) {
        result.onSuccess { chargeData ->
            updateState {
                PayPalCheckoutUIState.Success(chargeData)
            }
        }.onFailure { throwable ->
            val error: SdkException = throwable.mapApiException<PayPalException.CapturingChargeException>()
            updateState { PayPalCheckoutUIState.Error(error) }
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
            shipping = requestShipping,
            walletType = WalletType.PAY_PAL.type
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
        walletToken?.let { captureWalletTransaction(it, request) }
    }

    /**
     * Composes the PayPal URL for payment processing based on a callback URL.
     *
     * @param callbackUrl The callback URL for the PayPal payment.
     * @return The composed URL with PayPal parameters.
     */
    fun createPayPalUrl(callbackUrl: String): String =
        "$callbackUrl&${MobileSDKConstants.PayPalConfig.REDIRECT_PARAM_NAME}" +
            "=${MobileSDKConstants.PayPalConfig.PAY_PAL_REDIRECT_PARAM_VALUE}"

    /**
     * Parses a PayPal URL to extract payment data such as the PayPal token and payer ID.
     *
     * Updates the state to `PayPalCheckoutUIState.Capture` if the required parameters are found.
     *
     * @param requestUrl The URL returned from the PayPal payment process.
     */
    fun parsePayPalUrl(requestUrl: String) {
        val requestUri = Uri.parse(requestUrl)
        val payPalToken = requestUri.getQueryParameter("token")
        val payerId = if (requestUrl.contains("PayerID")) {
            requestUri.getQueryParameter("PayerID")
        } else {
            requestUri.getQueryParameter("flowId")
        }
        if (payPalToken != null && payerId != null) {
            updateState { PayPalCheckoutUIState.Capture(payPalToken, payerId) }
        }
    }
}
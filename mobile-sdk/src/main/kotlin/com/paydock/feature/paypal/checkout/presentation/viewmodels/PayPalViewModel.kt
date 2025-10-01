package com.paydock.feature.paypal.checkout.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.PayPalException
import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.core.domain.error.extensions.mapApiException
import com.paydock.core.extensions.safeCastAs
import com.paydock.feature.paypal.checkout.domain.mapper.integration.mapToPayPalFundingSource
import com.paydock.feature.paypal.checkout.domain.model.integration.PayPalWidgetConfig
import com.paydock.feature.paypal.checkout.presentation.state.PayPalCheckoutUIState
import com.paydock.feature.paypal.core.domain.usecase.GetPayPalClientIdUseCase
import com.paydock.feature.wallet.data.dto.CaptureWalletChargeRequest
import com.paydock.feature.wallet.data.dto.CustomerData
import com.paydock.feature.wallet.data.dto.PaymentSourceData
import com.paydock.feature.wallet.data.dto.WalletCallbackRequest
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.model.integration.WalletTokenResult
import com.paydock.feature.wallet.domain.model.integration.WalletType
import com.paydock.feature.wallet.domain.model.ui.WalletCallback
import com.paydock.feature.wallet.domain.usecase.CaptureWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.DeclineWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.GetWalletCallbackUseCase
import com.paydock.feature.wallet.presentation.viewmodels.WalletViewModel
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource

/**
 * ViewModel for managing the PayPal checkout process in the mobile SDK.
 *
 * This ViewModel handles various aspects of the PayPal flow, such as retrieving wallet callback data,
 * capturing wallet transactions, managing UI states, and parsing PayPal URLs. It extends
 * `WalletViewModel` and leverages use cases for interacting with PayPal services.
 *
 * @param config The PayPal widget configuration.
 * @param savedStateHandle Handle for persisting state across process death.
 * @param captureWalletChargeUseCase Use case for capturing wallet charges.
 * @param declineWalletChargeUseCase Use case for declining wallet charges.
 * @param getWalletCallbackUseCase Use case for retrieving wallet callback data.
 * @param dispatchers Dispatcher provider for managing coroutine contexts.
 * @param getPayPalClientIdUseCase Use case for retrieving the PayPal Client ID.
 */
internal class PayPalViewModel(
    private val config: PayPalWidgetConfig,
    private val savedStateHandle: SavedStateHandle,
    captureWalletChargeUseCase: CaptureWalletChargeUseCase,
    declineWalletChargeUseCase: DeclineWalletChargeUseCase,
    getWalletCallbackUseCase: GetWalletCallbackUseCase,
    dispatchers: DispatchersProvider,
    private val getPayPalClientIdUseCase: GetPayPalClientIdUseCase,
) : WalletViewModel<PayPalCheckoutUIState>(
    captureWalletChargeUseCase,
    declineWalletChargeUseCase,
    getWalletCallbackUseCase,
    dispatchers
) {

    //region Private Properties
    /**
     * Holds the wallet token used for PayPal operations.
     * Persisted in SavedStateHandle to survive process death with "Don't keep activities"
     *
     * This token is essential for authenticating and managing PayPal transactions.
     * Access via getWalletToken() / setWalletToken() methods to avoid signature clash.
     */
    private fun getWalletToken(): String? = savedStateHandle[KEY_WALLET_TOKEN]
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
     * Persisted in SavedStateHandle to survive process death.
     *
     * @param token The wallet token.
     */
    override fun setWalletToken(token: String) {
        savedStateHandle[KEY_WALLET_TOKEN] = token
    }

    /**
     * Resets the PayPal result state, clearing payment and error information.
     *
     * Updates the state to `PayPalCheckoutUIState.Idle`.
     */
    override fun resetResultState() {
        savedStateHandle[KEY_WALLET_TOKEN] = null
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
            onSuccess = { callback ->
                // Continue loading while resolving clientId; then emit LaunchIntent(clientId, orderId)
                setLoadingState()
                val orderId = callback.id
                if (orderId.isNullOrBlank()) {
                    updateUiState(
                        PayPalCheckoutUIState.Error(
                            PayPalException.ConfigurationException("Missing PayPal ORDER_ID")
                        )
                    )
                    return
                }
                getPayPalClientId(orderId)
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
     * Retrieves funding source from the initial config and converts
     * to PayPal specific enum.
     *
     * @return PayPalFundingSource to use with PayPal initialisation
     */
    fun getFundingSource(): PayPalWebCheckoutFundingSource {
        return config.fundingSource.mapToPayPalFundingSource()
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

    private fun getPayPalClientId(orderId: String) {
        launchOnIO {
            getPayPalClientIdUseCase(config.accessToken, config.gatewayId)
                .onSuccess { clientId ->
                    updateUiState(
                        PayPalCheckoutUIState.LaunchIntent(
                            clientId = clientId,
                            orderId = orderId
                        )
                    )
                }
                .onFailure { error ->
                    val mapped = error.safeCastAs<SdkException>()
                        ?: error.mapApiException(PayPalException.GetPayPalClientIdException::class)
                    updateUiState(PayPalCheckoutUIState.Error(mapped))
                }
        }
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
        getWalletToken()?.let {
            captureWalletTransaction(it, request)
        }
    }

    /**
     * Handles the click event for the PayPal button.
     *
     * This function initiates the PayPal token request and then proceeds to fetch wallet callback data
     * if the token request is successful. If the token request fails, it updates the UI state with an error.
     *
     * @param config The PayPal widget configuration.
     * @param tokenRequest A lambda function that takes a callback for the token result and requests the PayPal token.
     */
    fun handlePayPalButtonClick(
        config: PayPalWidgetConfig,
        tokenRequest: (tokenResult: (Result<WalletTokenResult>) -> Unit) -> Unit
    ) {
        setLoadingState()
        // Use the callback to obtain the token asynchronously
        tokenRequest.invoke { tokenResult ->
            tokenResult.onSuccess { result ->
                setWalletToken(result.token)
                getWalletCallback(
                    walletToken = result.token,
                    requestShipping = config.requestShipping
                )
            }.onFailure { throwable ->
                updateUiState(
                    PayPalCheckoutUIState.Error(
                        PayPalException.InitialisationWalletTokenException(
                            throwable.message ?: MobileSDKConstants.PayPalConfig.Errors.WALLET_TOKEN_ERROR
                        )
                    )
                )
            }
        }
    }
    //endregion

    private companion object {
        const val KEY_WALLET_TOKEN: String = "paypal.checkout.wallet_token"
    }
}
package com.paydock.feature.afterpay.presentation.viewmodels

import com.afterpay.android.Afterpay
import com.afterpay.android.CancellationStatus
import com.paydock.MobileSDK
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
import com.paydock.core.domain.error.exceptions.AfterpayException
import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.core.domain.error.extensions.mapApiException
import com.paydock.core.domain.mapper.mapToAfterpayEnv
import com.paydock.core.utils.jwt.JwtHelper
import com.paydock.feature.afterpay.domain.mapper.integration.mapMessage
import com.paydock.feature.afterpay.domain.mapper.integration.mapToSDKShippingOptionResult
import com.paydock.feature.afterpay.domain.mapper.integration.mapToSDKShippingOptionUpdateResult
import com.paydock.feature.afterpay.domain.model.integration.AfterpaySDKConfig
import com.paydock.feature.afterpay.domain.model.integration.AfterpayShippingOption
import com.paydock.feature.afterpay.domain.model.integration.AfterpayShippingOptionUpdate
import com.paydock.feature.afterpay.presentation.state.AfterpayUIState
import com.paydock.feature.charge.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.presentation.viewmodels.WalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

/**
 * ViewModel responsible for managing the state and interactions for the Afterpay payment flow.
 *
 * This ViewModel inherits from `WalletViewModel` and provides Afterpay-specific implementations
 * for managing the payment lifecycle, including initialization, token handling, charge capturing,
 * and shipping updates. It ensures that the Afterpay SDK is properly configured and manages
 * communication through state flows and command channels.
 *
 * @constructor Creates an instance of `AfterpayViewModel` with required dependencies for handling
 * Afterpay-specific wallet interactions.
 *
 * @param captureWalletChargeUseCase Use case for capturing a wallet charge.
 * @param declineWalletChargeUseCase Use case for declining a wallet charge.
 * @param getWalletCallbackUseCase Use case for fetching a wallet callback.
 * @param dispatchers Provides coroutine dispatchers for managing asynchronous tasks.
 */
internal class AfterpayViewModel(
    captureWalletChargeUseCase: CaptureWalletChargeUseCase,
    declineWalletChargeUseCase: DeclineWalletChargeUseCase,
    getWalletCallbackUseCase: GetWalletCallbackUseCase,
    dispatchers: DispatchersProvider,
) : WalletViewModel<AfterpayUIState>(
    captureWalletChargeUseCase,
    declineWalletChargeUseCase,
    getWalletCallbackUseCase,
    dispatchers
) {

    // Private properties to manage internal state
    private var walletToken: String? = null
    private var checkoutToken: String? = null
    private var _isConfigured = MutableStateFlow(false)

    /**
     * Exposes the Afterpay SDK configuration status.
     *
     * @return A `StateFlow` emitting `true` when the SDK is successfully configured, `false` otherwise.
     */
    val isConfigured: StateFlow<Boolean> = _isConfigured

    /**
     * Sets the wallet token for the current session.
     *
     * @param token The wallet token to associate with the session.
     */
    override fun setWalletToken(token: String) {
        walletToken = token
    }

    /**
     * Creates the initial UI state for the Afterpay flow.
     *
     * @return The default state, `AfterpayUIState.Idle`.
     */
    override fun createInitialState(): AfterpayUIState = AfterpayUIState.Idle

    /**
     * Resets the state to the initial state and clears the wallet token.
     */
    override fun resetResultState() {
        walletToken = null
        updateState { AfterpayUIState.Idle }
    }

    /**
     * Sets the UI state to `Loading` when a process begins.
     */
    override fun setLoadingState() {
        updateState { AfterpayUIState.Loading }
    }

    /**
     * Updates the UI state based on the result of a wallet callback.
     *
     * @param result The result of the wallet callback operation.
     */
    override fun updateCallbackUIState(result: Result<WalletCallback>) {
        result.onSuccess { callbackData ->
            callbackData.refToken?.let {
                val tokenResult: Result<String> = result.mapCatching {
                    it.refToken ?: throw AfterpayException.TokenException("Error fetching checkout token")
                }
                checkoutToken = tokenResult.getOrNull()
                updateState { AfterpayUIState.ProvideCheckoutTokenResult(tokenResult) }
            } ?: updateState {
                AfterpayUIState.Error(
                    AfterpayException.TokenException(MobileSDKConstants.Errors.AFTER_PAY_CALLBACK_ERROR)
                )
            }
        }.onFailure { throwable ->
            val error: SdkException =
                throwable.mapApiException<AfterpayException.FetchingUrlException>()
            updateState { AfterpayUIState.Error(error) }
        }
    }

    /**
     * Updates the UI state based on the result of a wallet charge operation.
     *
     * @param result The result of the charge operation.
     */
    override fun updateChargeUIState(result: Result<ChargeResponse>) {
        result.onSuccess { chargeData ->
            updateState { AfterpayUIState.Success(chargeData) }
        }.onFailure { throwable ->
            val error: SdkException =
                throwable.mapApiException<AfterpayException.CapturingChargeException>()
            updateState { AfterpayUIState.Error(error) }
        }
    }

    /**
     * Updates the state to reflect a cancellation event with the given status.
     *
     * This method is used to handle checkout cancellations by mapping the
     * provided cancellation status to a user-friendly error message and updating
     * the state with an error event.
     *
     * @param status The cancellation status received from the Afterpay SDK.
     */
    fun updateCancellationState(status: CancellationStatus) {
        // Update error state to notify error
        updateState { AfterpayUIState.Error(AfterpayException.CancellationException(status.mapMessage())) }
    }

    /**
     * Declines the current wallet transaction if valid identifiers are available.
     *
     * This method retrieves the wallet token and charge ID associated with the current
     * transaction and invokes the decline process if both values are valid and non-blank.
     *
     * If either the wallet token or the charge ID is unavailable, the method does nothing.
     */
    fun declineWalletTransaction() {
        val token = walletToken
        val chargeId = walletToken?.let { JwtHelper.getChargeIdToken(it) }
        if (!token.isNullOrBlank() && !chargeId.isNullOrBlank()) {
            declineWalletTransaction(token, chargeId)
        }
    }

    /**
     * Captures a wallet transaction by invoking the corresponding use case with a charge request.
     */
    fun captureWalletTransaction() {
        val request = CaptureWalletChargeRequest(
            customer = CustomerData(
                paymentSource = PaymentSourceData(
                    refToken = checkoutToken
                )
            )
        )
        walletToken?.let { captureWalletTransaction(it, request) }
    }

    /**
     * Configures the Afterpay SDK with the provided configuration details.
     *
     * @param configuration The configuration details required for Afterpay SDK setup.
     */
    fun configureAfterpaySdk(configuration: AfterpaySDKConfig.AfterpayConfiguration) {
        launchOnIO {
            try {
                Afterpay.setConfiguration(
                    minimumAmount = configuration.minimumAmount,
                    maximumAmount = configuration.maximumAmount,
                    currencyCode = configuration.currency,
                    locale = Locale(configuration.language, configuration.country),
                    environment = MobileSDK.getInstance().environment.mapToAfterpayEnv()
                )
                _isConfigured.value = true
            } catch (e: IllegalArgumentException) {
                updateState {
                    AfterpayUIState.Error(
                        AfterpayException.ConfigurationException(
                            e.message ?: "Afterpay: unsupported country: ${Locale.getDefault().displayCountry}"
                        )
                    )
                }
            }
        }
    }

    /**
     * Initiates a request to load the checkout token for the session.
     */
    fun loadCheckoutToken() {
        val request = WalletCallbackRequest(
            type = MobileSDKConstants.WalletCallbackType.TYPE_CREATE_SESSION,
            walletType = WalletType.AFTER_PAY.type
        )
        walletToken?.let { getWalletCallback(it, request) }
    }

    /**
     * Provides the shipping options for the current session.
     *
     * @param shippingOptions The list of available shipping options.
     */
    fun provideShippingOptions(shippingOptions: List<AfterpayShippingOption>) {
        updateState { AfterpayUIState.ProvideShippingOptionsResult(shippingOptions.mapToSDKShippingOptionResult()) }
    }

    /**
     * Provides the shipping option update for the current session.
     *
     * @param shippingOptionUpdate The updated shipping option, if available.
     */
    fun provideShippingOptionUpdate(shippingOptionUpdate: AfterpayShippingOptionUpdate?) {
        updateState {
            AfterpayUIState.ProvideShippingOptionUpdateResult(shippingOptionUpdate?.mapToSDKShippingOptionUpdateResult())
        }
    }
}
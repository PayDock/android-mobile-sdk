package com.paydock.feature.afterpay.presentation.viewmodels

import com.afterpay.android.Afterpay
import com.afterpay.android.CancellationStatus
import com.paydock.MobileSDK
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.AfterpayException
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
import com.paydock.feature.wallet.data.dto.CaptureWalletChargeRequest
import com.paydock.feature.wallet.data.dto.CustomerData
import com.paydock.feature.wallet.data.dto.PaymentSourceData
import com.paydock.feature.wallet.data.dto.WalletCallbackRequest
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.model.integration.WalletType
import com.paydock.feature.wallet.domain.model.ui.WalletCallback
import com.paydock.feature.wallet.domain.usecase.CaptureWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.DeclineWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.GetWalletCallbackUseCase
import com.paydock.feature.wallet.presentation.viewmodels.WalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    //region Private Properties
    /**
     * Holds the wallet token used for Afterpay operations.
     *
     * This token is essential for authenticating and managing Afterpay transactions.
     */
    private var walletToken: String? = null
    private var checkoutToken: String? = null
    private val _isConfigured = MutableStateFlow(false)
    //endregion

    //region Public Properties
    /**
     * Exposes the Afterpay SDK configuration status.
     *
     * @return A `StateFlow` emitting `true` when the SDK is successfully configured, `false` otherwise.
     */
    val isConfigured: StateFlow<Boolean> = _isConfigured.asStateFlow()
    //endregion

    //region Overridden Methods
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
        updateUiState(AfterpayUIState.Idle)
    }

    /**
     * Sets the UI state to `Loading` when a process begins.
     */
    override fun setLoadingState() {
        updateUiState(AfterpayUIState.Loading)
    }

    /**
     * Updates the UI state based on the result of a wallet callback.
     *
     * @param result The result of the wallet callback operation.
     */
    override fun updateCallbackUIState(result: Result<WalletCallback>) {
        result.fold(
            onSuccess = { callbackData ->
                callbackData.refToken?.let { refToken ->
                    val tokenResult: Result<String> = Result.success(refToken)
                    checkoutToken = tokenResult.getOrNull()
                    updateUiState(AfterpayUIState.ProvideCheckoutTokenResult(tokenResult))
                } ?: updateUiState(
                    AfterpayUIState.Error(
                        AfterpayException.TokenException(MobileSDKConstants.Errors.AFTER_PAY_CALLBACK_ERROR)
                    )
                )
            },
            onFailure = { throwable ->
                updateUiState(AfterpayUIState.Error(throwable.mapApiException(AfterpayException.FetchingUrlException::class)))
            }
        )
    }

    /**
     * Updates the UI state based on the result of a wallet charge operation.
     *
     * @param result The result of the charge operation.
     */
    override fun updateChargeUIState(result: Result<ChargeResponse>) {
        result.fold(
            onSuccess = { chargeData ->
                updateUiState(AfterpayUIState.Success(chargeData))
            },
            onFailure = { throwable ->
                updateUiState(AfterpayUIState.Error(throwable.mapApiException(AfterpayException.CapturingChargeException::class)))
            }
        )
    }
    //endregion

    //region Public Methods
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
        updateUiState(AfterpayUIState.Error(AfterpayException.CancellationException(status.mapMessage())))
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

        if (token.isNullOrBlank() || chargeId.isNullOrBlank()) return

        declineWalletTransaction(token, chargeId)
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
        walletToken?.let {
            captureWalletTransaction(it, request)
        }
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
                val errorMessage = e.message
                    ?: "Afterpay: unsupported country: ${Locale.getDefault().displayCountry}"
                updateUiState(
                    AfterpayUIState.Error(
                        AfterpayException.ConfigurationException(errorMessage)
                    )
                )
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
        updateUiState(AfterpayUIState.ProvideShippingOptionsResult(shippingOptions.mapToSDKShippingOptionResult()))
    }

    /**
     * Provides the shipping option update for the current session.
     *
     * @param shippingOptionUpdate The updated shipping option, if available.
     */
    fun provideShippingOptionUpdate(shippingOptionUpdate: AfterpayShippingOptionUpdate?) {
        updateUiState(AfterpayUIState.ProvideShippingOptionUpdateResult(shippingOptionUpdate?.mapToSDKShippingOptionUpdateResult()))
    }
    //endregion
}
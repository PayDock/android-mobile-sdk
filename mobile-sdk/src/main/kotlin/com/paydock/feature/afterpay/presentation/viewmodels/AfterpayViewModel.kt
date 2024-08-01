package com.paydock.feature.afterpay.presentation.viewmodels

import com.afterpay.android.Afterpay
import com.afterpay.android.AfterpayEnvironment
import com.afterpay.android.CancellationStatus
import com.afterpay.android.model.ShippingOptionUpdateResult
import com.afterpay.android.model.ShippingOptionsSuccessResult
import com.paydock.MobileSDK
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.AfterpayException
import com.paydock.core.domain.model.Environment
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.exceptions.UnknownApiException
import com.paydock.feature.afterpay.presentation.mapper.mapMessage
import com.paydock.feature.afterpay.presentation.mapper.mapToSDKShippingOptionResult
import com.paydock.feature.afterpay.presentation.mapper.mapToSDKShippingOptionUpdateResult
import com.paydock.feature.afterpay.presentation.model.AfterpaySDKConfig
import com.paydock.feature.afterpay.presentation.model.AfterpayShippingOption
import com.paydock.feature.afterpay.presentation.model.AfterpayShippingOptionUpdate
import com.paydock.feature.afterpay.presentation.state.AfterpayViewState
import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.wallet.data.api.dto.Customer
import com.paydock.feature.wallet.data.api.dto.PaymentSource
import com.paydock.feature.wallet.data.api.dto.WalletCallbackRequest
import com.paydock.feature.wallet.data.api.dto.WalletCaptureRequest
import com.paydock.feature.wallet.domain.model.WalletCallback
import com.paydock.feature.wallet.domain.model.WalletType
import com.paydock.feature.wallet.domain.usecase.CaptureWalletTransactionUseCase
import com.paydock.feature.wallet.domain.usecase.DeclineWalletTransactionUseCase
import com.paydock.feature.wallet.domain.usecase.GetWalletCallbackUseCase
import com.paydock.feature.wallet.presentation.viewmodels.WalletViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.Locale

/**
 * ViewModel responsible for managing Afterpay payment-related data and state.
 *
 * @param captureWalletTransactionUseCase The use case responsible for capturing wallet transactions.
 * @param declineWalletTransactionUseCase The use case responsible for declining wallet transactions.
 * @param getWalletCallbackUseCase The use case getting wallet callback details.
 * @param getWalletCallbackUseCase The use case getting wallet callback details.
 * @param dispatchers Provides the coroutine dispatchers for handling asynchronous tasks.
 */
@Suppress("TooManyFunctions")
internal class AfterpayViewModel(
    captureWalletTransactionUseCase: CaptureWalletTransactionUseCase,
    declineWalletTransactionUseCase: DeclineWalletTransactionUseCase,
    getWalletCallbackUseCase: GetWalletCallbackUseCase,
    dispatchers: DispatchersProvider
) : WalletViewModel<AfterpayViewState>(
    captureWalletTransactionUseCase,
    declineWalletTransactionUseCase,
    getWalletCallbackUseCase,
    dispatchers
) {

    private val commandChannel = Channel<Command>(Channel.CONFLATED)

    /**
     * Provides a flow of commands for observing external actions.
     */
    fun commands(): Flow<Command> = commandChannel.receiveAsFlow()

    sealed class Command {
        data class ProvideCheckoutTokenResult(val tokenResult: Result<String>) : Command()
        data class ProvideShippingOptionsResult(val shippingOptionsResult: ShippingOptionsSuccessResult) :
            Command()

        data class ProvideShippingOptionUpdateResult(val shippingOptionUpdateResult: ShippingOptionUpdateResult?) :
            Command()
    }

    /**
     * Sets the Afterpay authentication token in the UI state.
     *
     * @param token The Afterpay authentication token.
     */
    override fun setWalletToken(token: String) {
        updateState { state ->
            state.copy(token = token)
        }
    }

    /**
     * Resets the result state for Afterpay, clearing payment data and error information.
     */
    override fun resetResultState() {
        updateState { state ->
            state.copy(
                token = null,
                chargeData = null,
                callbackData = null,
                error = null,
                isLoading = false
            )
        }
    }

    /**
     * Sets the Afterpay UI state to indicate loading.
     */
    override fun setLoadingState() {
        updateState { state -> state.copy(isLoading = true) }
    }

    /**
     * Updates the Afterpay UI state based on the result of fetching wallet callback data.
     *
     * @param result The result of fetching wallet callback data.
     */
    override fun updateCallbackUIState(result: Result<WalletCallback>) {
        if (result.isSuccess) {
            val tokenResult: Result<String> = result.mapCatching {
                it.refToken
                    ?: throw AfterpayException.TokenException("Error fetching checkout token")
            }
            commandChannel.trySend(Command.ProvideCheckoutTokenResult(tokenResult))
        }
        updateState { currentState ->
            val exception: Throwable? = result.exceptionOrNull()
            val error: AfterpayException? = when (exception) {
                is ApiException -> AfterpayException.FetchingUrlException(error = exception.error)
                is UnknownApiException -> AfterpayException.UnknownException(displayableMessage = exception.errorMessage)
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
     * Updates the Afterpay UI state based on the result of capturing a wallet transaction.
     *
     * @param result The result of capturing a wallet transaction.
     */
    override fun updateChargeUIState(result: Result<ChargeResponse>) {
        updateState { currentState ->
            val exception: Throwable? = result.exceptionOrNull()
            val error: AfterpayException? = when (exception) {
                is ApiException -> AfterpayException.CapturingChargeException(error = exception.error)
                is UnknownApiException -> AfterpayException.UnknownException(displayableMessage = exception.errorMessage)
                else -> currentState.error
            }
            currentState.copy(
                error = error,
                isLoading = false,
                chargeData = result.getOrNull()
            )
        }
    }

    /**
     * Creates the initial state for the Afterpay UI.
     *
     * @return Initial state of the Afterpay UI.
     */
    override fun createInitialState(): AfterpayViewState = AfterpayViewState()

    /**
     * Updates the Afterpay UI state based on the [CancellationStatus].
     *
     * @param status The result of [CancellationStatus] wallet transaction.
     */
    fun updateCancellationState(status: CancellationStatus) {
        updateState { currentState ->
            currentState.copy(
                error = AfterpayException.CancellationException(status.mapMessage())
            )
        }
    }

    /**
     * Initiates the process to retrieve the Afterpay wallet callback data based on the provided parameters.
     *
     * @param walletToken The Afterpay authentication token.
     */
    fun getWalletCallback(walletToken: String) {
        val request = WalletCallbackRequest(
            type = MobileSDKConstants.WalletCallbackType.TYPE_CREATE_SESSION,
            walletType = WalletType.AFTER_PAY.type
        )
        getWalletCallback(walletToken, request)
    }

    /**
     * Capture an Afterpay wallet transaction using the provided Afterpay token and reference token.
     *
     * @param walletToken The Afterpay authentication token.
     * @param afterPayToken The Afterpay checkout token.
     */
    fun captureWalletTransaction(
        walletToken: String,
        afterPayToken: String
    ) {
        val request = WalletCaptureRequest(
            customer = Customer(
                paymentSource = PaymentSource(
                    refToken = afterPayToken
                )
            )
        )
        captureWalletTransaction(walletToken, request)
    }

    /**
     * Configures the Afterpay SDK with the provided configuration.
     *
     * @param configuration The configuration for the Afterpay SDK.
     */
    fun configureAfterpaySdk(configuration: AfterpaySDKConfig.AfterpayConfiguration) {
        launchOnIO {
            try {
                Afterpay.setConfiguration(
                    minimumAmount = configuration.minimumAmount,
                    maximumAmount = configuration.maximumAmount,
                    currencyCode = configuration.currency,
                    locale = Locale(configuration.language, configuration.country),
                    environment = when (MobileSDK.getInstance().environment) {
                        Environment.SANDBOX, Environment.STAGING -> AfterpayEnvironment.SANDBOX
                        Environment.PRODUCTION -> AfterpayEnvironment.PRODUCTION
                    }
                )
                updateState { state -> state.copy(validLocale = true) }
            } catch (e: IllegalArgumentException) {
                updateState { state ->
                    state.copy(
                        error = AfterpayException.ConfigurationException(
                            e.message
                                ?: "Afterpay: unsupported country: ${Locale.getDefault().displayCountry}"
                        )
                    )
                }
            }
        }
    }

    /**
     * Loads the checkout token for Afterpay.
     */
    fun loadCheckoutToken() {
        val walletToken = stateFlow.value.token
        if (!walletToken.isNullOrBlank()) {
            getWalletCallback(walletToken = walletToken)
        }
    }

    /**
     * Provides shipping options to the UI.
     *
     * @param shippingOptions The list of Afterpay shipping options.
     */
    fun provideShippingOptions(shippingOptions: List<AfterpayShippingOption>) {
        commandChannel.trySend(Command.ProvideShippingOptionsResult(shippingOptions.mapToSDKShippingOptionResult()))
    }

    /**
     * Provides shipping option update to the UI.
     *
     * @param shippingOptionUpdate The Afterpay shipping option update.
     */
    fun provideShippingOptionUpdate(shippingOptionUpdate: AfterpayShippingOptionUpdate?) {
        commandChannel.trySend(Command.ProvideShippingOptionUpdateResult(shippingOptionUpdate?.mapToSDKShippingOptionUpdateResult()))
    }
}

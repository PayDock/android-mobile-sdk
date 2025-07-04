package com.paydock.feature.googlepay.presentation.viewmodels

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.WalletConstants
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.GooglePayException
import com.paydock.core.domain.error.extensions.mapApiException
import com.paydock.feature.googlepay.domain.model.GooglePayWidgetConfig
import com.paydock.feature.googlepay.presentation.state.GooglePayUIState
import com.paydock.feature.wallet.data.dto.CaptureWalletChargeRequest
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.model.integration.WalletTokenResult
import com.paydock.feature.wallet.domain.usecase.CaptureWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.DeclineWalletChargeUseCase
import com.paydock.feature.wallet.domain.usecase.GetWalletCallbackUseCase
import com.paydock.feature.wallet.presentation.viewmodels.WalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONException
import org.json.JSONObject

/**
 * ViewModel to manage the Google Pay payment flow and UI state.
 *
 * This ViewModel extends [WalletViewModel] and specifically handles the
 * UI state and payment interactions related to Google Pay. It is responsible
 * for checking Google Pay availability, initiating payment requests,
 * processing payment results, and handling potential errors during the flow.
 *
 * @param paymentsClient The Google Pay [PaymentsClient] instance for initiating payment requests.
 * @param config The [GooglePayWidgetConfig] containing configuration details for the Google Pay widget.
 * @param captureWalletChargeUseCase Use case for capturing wallet charges.
 * @param declineWalletChargeUseCase Use case for declining wallet charges.
 * @param getWalletCallbackUseCase Use case for retrieving wallet callback information.
 * @param dispatchers The dispatchers for coroutine context switching.
 */
internal class GooglePayViewModel(
    private val paymentsClient: PaymentsClient,
    private val config: GooglePayWidgetConfig,
    captureWalletChargeUseCase: CaptureWalletChargeUseCase,
    declineWalletChargeUseCase: DeclineWalletChargeUseCase,
    getWalletCallbackUseCase: GetWalletCallbackUseCase,
    dispatchers: DispatchersProvider
) : WalletViewModel<GooglePayUIState>(
    captureWalletChargeUseCase,
    declineWalletChargeUseCase,
    getWalletCallbackUseCase,
    dispatchers
) {

    //region Private Properties
    /**
     * Holds the wallet token used for Google Pay operations.
     *
     * This token is essential for authenticating and managing Google Pay transactions.
     */
    private var walletToken: String? = null

    /**
     * Mutable state flow to hold the UI state for Google Pay availability.
     */
    private val _googlePayAvailable: MutableStateFlow<Boolean> = MutableStateFlow(false)
    //endregion

    /**
     * Expose a read-only state flow for observing the Google Pay availability.
     */
    val googlePayAvailable: StateFlow<Boolean> = _googlePayAvailable.asStateFlow()

    init {
        fetchCanUseGooglePay(config.isReadyToPayRequest)
    }

    //region Overridden Methods
    /**
     * Provides the initial state for the Google Pay UI.
     *
     * @return The initial UI state, which is [GooglePayUIState.Idle].
     */
    override fun createInitialState(): GooglePayUIState = GooglePayUIState.Idle

    /**
     * Sets the Google Pay wallet token used for authentication and transaction processing.
     *
     * @param token The wallet token.
     */
    override fun setWalletToken(token: String) {
        walletToken = token
    }

    /**
     * Resets the Google Pay result state, clearing payment and error information.
     *
     * Updates the state to [GooglePayUIState.Idle].
     */
    override fun resetResultState() {
        walletToken = null
        updateUiState(GooglePayUIState.Idle)
    }

    /**
     * Sets the Google Pay UI to a loading state.
     *
     * Updates the state to [GooglePayUIState.Loading].
     */
    override fun setLoadingState() {
        updateUiState(GooglePayUIState.Loading)
    }

    /**
     * Updates the UI state with the result of a wallet charge operation.
     *
     * @param result The result of the wallet charge operation.
     */
    override fun updateChargeUIState(result: Result<ChargeResponse>) {
        result.fold(
            onSuccess = { chargeData ->
                updateUiState(GooglePayUIState.Success(chargeData))
            },
            onFailure = { throwable ->
                updateUiState(
                    GooglePayUIState.Error(
                        throwable.mapApiException(GooglePayException.CapturingChargeException::class)
                    )
                )
            }
        )
    }
    //endregion

    //region Private Methods
    /**
     * Determines the user's ability to pay with a payment method supported by your app
     * and updates the Google Pay availability state.
     */
    private fun fetchCanUseGooglePay(isReadyToPayJson: JSONObject) {
        launchOnIO {
            try {
                val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString())
                val isReadyToPay = paymentsClient.isReadyToPay(request).await()
                _googlePayAvailable.value = isReadyToPay
                if (!isReadyToPay) {
                    handleGooglePayInitializationError(MobileSDKConstants.GooglePayConfig.Errors.GOOGLE_PAY_ERROR)
                }
            } catch (exception: ApiException) {
                handleGooglePayInitializationError(exception.message ?: MobileSDKConstants.GooglePayConfig.Errors.GOOGLE_PAY_ERROR)
            }
        }
    }

    /**
     * Handles errors during Google Pay initialization.
     *
     * @param errorMessage The error message to be displayed.
     */
    private fun handleGooglePayInitializationError(errorMessage: String) {
        updateUiState(
            GooglePayUIState.Error(
                GooglePayException.InitialisationException(errorMessage)
            )
        )
    }

    /**
     * Updates the UI state with an error message.
     *
     * @param errorMessage The error message to be displayed.
     */
    private fun handleErrorResult(errorMessage: String) {
        updateUiState(
            GooglePayUIState.Error(
                GooglePayException.ResultException(errorMessage)
            )
        )
    }
    //endregion

    //region Public Methods

    /**
     * Initiates the Google Pay payment flow.
     *
     * This function sets the UI to a loading state and then invokes the [tokenProvider]
     * to obtain a wallet token. Once the token is received, it proceeds to the
     * `onTokenReceivedAndReadyToPay` method with the token.
     *
     * @param tokenProvider A higher-order function that takes a callback `(Result<WalletTokenResult>) -> Unit`
     *                      and is responsible for asynchronously providing the wallet token.
     *                      The callback should be invoked with the [Result] of the token retrieval operation.
     *                      On success, the [Result] will contain a [WalletTokenResult] with the token.
     *                      On failure, the [Result] will contain an appropriate [Throwable].
     */
    fun startGooglePayPaymentFlow(
        tokenProvider: (tokenResult: (Result<WalletTokenResult>) -> Unit) -> Unit,
    ) {
        setLoadingState()
        tokenProvider.invoke { tokenResult ->
            tokenResult.onSuccess { result ->
                // This is where the token is received.
                // Now we have the token and can proceed.
                onTokenReceivedAndReadyToPay(result.token)
            }.onFailure { throwable ->
                updateUiState(
                    GooglePayUIState.Error(
                        GooglePayException.InitialisationWalletTokenException(
                            throwable.message ?: MobileSDKConstants.GooglePayConfig.Errors.WALLET_TOKEN_ERROR
                        )
                    )
                )
            }
        }
    }
    //endregion

    //region Public Methods
    /**
     * Handles the cancellation result by updating the UI state to an error state.
     */
    fun handleCancellationResult(message: String = MobileSDKConstants.GooglePayConfig.Errors.CANCELLATION_ERROR) {
        updateUiState(
            GooglePayUIState.Error(
                GooglePayException.CancellationException(message)
            )
        )
    }

    /**
     * Extracts allowed payment methods from the payment request.
     *
     * @return A string representing the allowed payment methods, or null if extraction fails.
     */
    fun extractAllowedPaymentMethods(request: JSONObject = config.paymentRequest): String? {
        return runCatching {
            request.getJSONArray(MobileSDKConstants.GooglePayConfig.ALLOWED_PAYMENT_METHODS_KEY)
                .toString()
        }.getOrElse {
            updateUiState(
                GooglePayUIState.Error(
                    GooglePayException.InitialisationException()
                )
            )
            null
        }
    }

    /**
     * Extracts the Google Pay token from the payment data.
     *
     * This function parses the JSON representation of the payment data
     * and retrieves the token used for processing the transaction.
     *
     * @param paymentData The [PaymentData] object containing the payment result.
     * @return The Google Pay token as a string.
     * @throws JSONException If there is an error parsing the JSON data.
     */
    @Throws(JSONException::class)
    private fun extractGooglePayToken(paymentData: PaymentData): String {
        val paymentInformation = paymentData.toJson()
        val paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")
        return paymentMethodData.getJSONObject(MobileSDKConstants.GooglePayConfig.TOKENIZATION_DATA_KEY)
            .getString(MobileSDKConstants.GooglePayConfig.TOKEN_KEY)
    }

    /**
     * Handles the state after a wallet token is successfully received and prepares for payment.
     *
     * This function is typically called after the [tokenProvider] in [startGooglePayPaymentFlow]
     * has successfully obtained a wallet token. It sets the received [token],
     * updates the UI to a loading state, and then proceeds to initiate the Google Pay
     * payment process by creating and emitting a [Task] to launch the Google Pay sheet.
     *
     * In a real-world scenario, this function might involve an additional API call
     * using the `walletToken` before proceeding to get the payment task. For this
     * implementation, it directly proceeds to fetch the payment task.
     *
     * @param token The wallet token received from the token provider.
     */
    private fun onTokenReceivedAndReadyToPay(token: String) {
        setWalletToken(token)
        setLoadingState()
        // In a real scenario, you might do an API call here with the walletToken
        // For now, let's assume we proceed directly to getting the payment task
        launchOnIO {
            val task = getLoadPaymentDataTask()
            updateUiState(GooglePayUIState.LaunchGooglePayTask(task))
        }
    }

    /**
     * Creates a [Task] that starts the payment process with the transaction details included.
     *
     * @return A [Task] with the payment information.
     */
    private fun getLoadPaymentDataTask(): Task<PaymentData> {
        val request = PaymentDataRequest.fromJson(config.paymentRequest.toString())
        return paymentsClient.loadPaymentData(request)
    }

    /**
     * Processes the Google Pay payment result and captures the wallet transaction.
     *
     * @param paymentData The [PaymentData] containing the payment result.
     */
    fun processGooglePayPaymentResult(paymentData: PaymentData) {
        walletToken?.let { token ->
            runCatching { extractGooglePayToken(paymentData) }
                .onSuccess { googlePayToken ->
                    val request = CaptureWalletChargeRequest(paymentMethodId = googlePayToken)
                    captureWalletTransaction(token, request)
                }
                .onFailure { exception ->
                    handleErrorResult(
                        exception.message
                            ?: MobileSDKConstants.GooglePayConfig.Errors.TOKEN_ERROR
                    )
                }
        } ?: run {
            handleErrorResult(MobileSDKConstants.GooglePayConfig.Errors.GOOGLE_PAY_ERROR)
        }
    }

    /**
     * Handles errors resulting from the Google Pay result status code.
     *
     * @param statusCode The status code from the Google Pay result.
     */
    fun handleGooglePayResultErrors(statusCode: Int) {
        when (statusCode) {
            CommonStatusCodes.CANCELED -> handleCancellationResult()
            CommonStatusCodes.DEVELOPER_ERROR -> handleErrorResult(MobileSDKConstants.GooglePayConfig.Errors.DEV_ERROR)
            else -> {
                val statusCodeMessage = CommonStatusCodes.getStatusCodeString(statusCode)
                val errorMessage =
                    "[$statusCodeMessage] ${MobileSDKConstants.GooglePayConfig.Errors.GOOGLE_PAY_ERROR}"
                handleErrorResult(errorMessage)
            }
        }
    }

    /**
     * Handles errors resulting from the wallet result status.
     *
     * @param status The [Status] object containing the wallet result status.
     */
    fun handleWalletResultErrors(status: Status?) {
        when (status?.statusCode) {
            WalletConstants.ERROR_CODE_USER_CANCELLED ->
                handleCancellationResult(
                    status.statusMessage ?: MobileSDKConstants.GooglePayConfig.Errors.CANCELLATION_ERROR
                )

            WalletConstants.ERROR_CODE_DEVELOPER_ERROR -> handleErrorResult(
                status.statusMessage ?: MobileSDKConstants.GooglePayConfig.Errors.DEV_ERROR
            )

            else -> {
                val errorMessage =
                    status?.statusMessage ?: MobileSDKConstants.GooglePayConfig.Errors.GOOGLE_PAY_ERROR
                handleErrorResult(errorMessage)
            }
        }
    }
    //endregion
}
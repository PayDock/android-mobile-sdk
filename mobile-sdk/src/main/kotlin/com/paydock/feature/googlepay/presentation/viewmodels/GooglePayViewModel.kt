package com.paydock.feature.googlepay.presentation.viewmodels

import android.util.Base64
import androidx.lifecycle.viewModelScope
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
import com.paydock.core.presentation.viewmodels.BaseViewModel
import com.paydock.core.telemetry.SdkWidget
import com.paydock.core.telemetry.failPaymentWhileActive
import com.paydock.core.telemetry.finishPaymentWhileActive
import com.paydock.core.telemetry.reportSdkError
import com.paydock.core.telemetry.reportSdkErrorWhileActive
import com.paydock.core.telemetry.startPayment
import com.paydock.feature.googlepay.data.dto.CreateGooglePayTokenRequest
import com.paydock.feature.googlepay.data.dto.GooglePayPayloadData
import com.paydock.feature.googlepay.domain.model.integration.GooglePayBillingAddress
import com.paydock.feature.googlepay.domain.model.integration.GooglePayIsReadyToPayRequest
import com.paydock.feature.googlepay.domain.model.integration.GooglePayPaymentDataRequest
import com.paydock.feature.googlepay.domain.model.integration.GooglePayResult
import com.paydock.feature.googlepay.domain.model.integration.GooglePayWidgetConfig
import com.paydock.feature.googlepay.domain.usecase.CreateGooglePayTokenUseCase
import com.paydock.feature.googlepay.presentation.state.GooglePayUIState
import com.paydock.feature.googlepay.util.PaymentsUtil
import com.paydock.telemetry.Telemetry
import com.paydock.telemetry.TelemetryEvent
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ViewModel to manage the Google Pay payment flow and UI state.
 *
 * This ViewModel handles the UI state and payment interactions related to Google Pay.
 * It is responsible for checking Google Pay availability, initiating payment requests,
 * processing payment results, and creating payment tokens.
 *
 * @param paymentsClient The Google Pay [PaymentsClient] instance for initiating payment requests.
 * @param config The [GooglePayWidgetConfig] containing configuration details for the Google Pay widget.
 * @param createGooglePayTokenUseCase Use case for creating Google Pay payment tokens.
 * @param dispatchers The dispatchers for coroutine context switching.
 * @param telemetry The telemetry facade the view model reports its handled errors to.
 */
internal class GooglePayViewModel(
    private val paymentsClient: PaymentsClient,
    private val config: GooglePayWidgetConfig,
    private val createGooglePayTokenUseCase: CreateGooglePayTokenUseCase,
    dispatchers: DispatchersProvider,
    private val telemetry: Telemetry,
) : BaseViewModel(dispatchers) {

    //region Private Properties
    /**
     * Mutable state flow to hold the UI state for Google Pay availability.
     */
    private val _googlePayAvailable: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * Mutable state flow to hold the UI state.
     */
    private val _uiState: MutableStateFlow<GooglePayUIState> =
        MutableStateFlow(GooglePayUIState.Idle)

    /**
     * Whether the allowed payment methods failure was reported: [extractAllowedPaymentMethods] runs on every
     * composition, so its failure is reported once per view model.
     */
    private val allowedPaymentMethodsFailureReported = AtomicBoolean(false)

    /** The open payment attempt's ID (§ 5.9), or null when none is open or telemetry is inactive. */
    private var paymentId: String? = null
    //endregion

    /**
     * Expose a read-only state flow for observing the Google Pay availability.
     */
    val googlePayAvailable: StateFlow<Boolean> = _googlePayAvailable.asStateFlow()

    /**
     * Expose a read-only state flow for observing the UI state changes.
     */
    val uiState: StateFlow<GooglePayUIState> = _uiState.asStateFlow()

    init {
        fetchCanUseGooglePay(config.isReadyToPayRequest)
    }

    //region Private Methods
    /**
     * Updates the UI state to a new value.
     *
     * @param newState The new state to set for the UI.
     */
    private fun updateUiState(newState: GooglePayUIState) {
        _uiState.value = newState
        finishPaymentOn(newState)
    }

    /**
     * Ends the open payment attempt when [state] ends it: every Success, and every Error while one is open (a
     * cancelled sheet as cancelled, any other error with the code the merchant receives). An error set before any
     * attempt (readiness, initialisation) finds no open attempt and sends nothing.
     */
    private fun finishPaymentOn(state: GooglePayUIState) {
        if (paymentId == null) return
        when (state) {
            is GooglePayUIState.Success -> finishAttempt(TelemetryEvent.PaymentOutcome.SUCCESS)
            is GooglePayUIState.Error -> when (state.exception) {
                is GooglePayException.CancellationException,
                is GooglePayException.SDKException.CancelledBySdk -> finishAttempt(TelemetryEvent.PaymentOutcome.CANCELLED)
                else -> finishAttempt(TelemetryEvent.PaymentOutcome.ERROR, state.exception)
            }
            else -> Unit
        }
    }

    /**
     * Ends the open payment attempt with [outcome]; an error carries the code of [error], the exception the merchant
     * receives. Then no attempt is open. The finish runs undispatched in viewModelScope, so it is sent at once while
     * the view model lives, and not at all once it is cleared (the token coroutine cancelled): the attempt then stays
     * abandoned.
     */
    private fun finishAttempt(outcome: TelemetryEvent.PaymentOutcome, error: Throwable? = null) {
        val id = paymentId ?: return
        paymentId = null
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            if (outcome == TelemetryEvent.PaymentOutcome.ERROR) {
                telemetry.failPaymentWhileActive(SdkWidget.GOOGLE_PAY, id, error)
            } else {
                telemetry.finishPaymentWhileActive(SdkWidget.GOOGLE_PAY, id, outcome)
            }
        }
    }
    //endregion

    //region Private Methods
    /**
     * Determines the user's ability to pay with a payment method supported by your app
     * and updates the Google Pay availability state.
     *
     * Note: Initialization errors do not set the UI state to Error, as they are handled
     * by hiding the button when Google Pay is unavailable. Only errors during the actual
     * payment flow (after user interaction) will set the Error state.
     *
     * This check may fail due to:
     * - Timing: Google Play Services not fully initialized yet
     * - Network: No internet connection
     * - Configuration: Invalid payment method configuration
     * - Device: Device doesn't support Google Pay or user not signed in
     */
    private fun fetchCanUseGooglePay(isReadyToPayRequest: GooglePayIsReadyToPayRequest) {
        launchOnIO {
            try {
                val request =
                    IsReadyToPayRequest.fromJson(isReadyToPayRequest.toJsonString())
                val isReadyToPay = paymentsClient.isReadyToPay(request).await()
                _googlePayAvailable.value = isReadyToPay
                if (!isReadyToPay) {
                    handleIsReadyToPayError(MobileSDKConstants.GooglePayConfig.Errors.IS_READY_TO_PAY_ERROR)
                }
            } catch (exception: ApiException) {
                handleIsReadyToPayError(
                    exception.message
                        ?: MobileSDKConstants.GooglePayConfig.Errors.IS_READY_TO_PAY_ERROR,
                    cause = exception
                )
            }
        }
    }

    /**
     * Handles errors during Google Pay "Is Ready to Pay" check.
     *
     * @param errorMessage The error message to be displayed.
     * @param cause The gms exception the error was built from (GP2); reported, never handed on. Without one, Google
     *    Pay is simply not available on the device (GP1), an expected outcome that is not reported.
     */
    private suspend fun handleIsReadyToPayError(errorMessage: String, cause: Throwable? = null) {
        val failure = GooglePayException.IsReadyToPayException(errorMessage)
        if (cause != null) telemetry.reportSdkErrorWhileActive(failure, SdkWidget.GOOGLE_PAY, cause = cause)
        updateUiState(GooglePayUIState.Error(failure))
    }

    /**
     * Updates the UI state with an error message.
     *
     * @param errorMessage The error message to be displayed.
     * @param cause The original throwable the error was built from; reported, never handed on.
     */
    private fun handleErrorResult(errorMessage: String, cause: Throwable) {
        val failure = GooglePayException.ResultException(errorMessage)
        telemetry.reportSdkError(failure, SdkWidget.GOOGLE_PAY, cause = cause)
        updateUiState(GooglePayUIState.Error(failure))
    }

    /**
     * [handleErrorResult] for a failure inside a coroutine (GP6): the report is skipped once the coroutine is no
     * longer active, and the state is set as before.
     *
     * @param errorMessage The error message to be displayed.
     * @param cause The original throwable the error was built from; reported, never handed on.
     */
    private suspend fun handleErrorResultWhileActive(errorMessage: String, cause: Throwable) {
        val failure = GooglePayException.ResultException(errorMessage)
        telemetry.reportSdkErrorWhileActive(failure, SdkWidget.GOOGLE_PAY, cause = cause)
        updateUiState(GooglePayUIState.Error(failure))
    }
    //endregion

    //region Public Methods

    /**
     * Initiates the Google Pay payment flow.
     *
     * This function sets the UI to a loading state and then proceeds to launch the Google Pay sheet.
     */
    fun startGooglePayPaymentFlow() {
        updateUiState(GooglePayUIState.Loading)
        paymentId = telemetry.startPayment(SdkWidget.GOOGLE_PAY)
        // Build the loadPaymentData task and trigger the launch on the MAIN thread. Google Play
        // Services' PaymentsClient.loadPaymentData and the resulting Activity launch are
        // main-thread bound; creating the task on a background (IO) thread races with the
        // ActivityResult launch and intermittently surfaces an immediate RESULT_CANCELED (the
        // Google Pay sheet opens and closes instantly). The call is cheap (it only builds the
        // request and returns a Task), so it is safe to run on main.
        launchOnMain {
            val task = getLoadPaymentDataTask()
            updateUiState(GooglePayUIState.LaunchGooglePayTask(task))
        }
    }
    //endregion

    //region Public Methods
    /**
     * Handles the cancellation result by updating the UI state to an error state.
     * This is intended for user-initiated cancellation from Paydock's UI.
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
    fun extractAllowedPaymentMethods(request: GooglePayPaymentDataRequest = config.paymentRequest): String? {
        return runCatching {
            val json = request.allowedPaymentMethodsJsonArrayString()
            require(json.isNotBlank() && json != "[]") { "Allowed payment methods missing" }
            json
        }.getOrElse {
            val failure = GooglePayException.InitialisationException()
            if (allowedPaymentMethodsFailureReported.compareAndSet(false, true)) {
                telemetry.reportSdkError(failure, SdkWidget.GOOGLE_PAY, cause = it)
            }
            updateUiState(GooglePayUIState.Error(failure))
            null
        }
    }

    /**
     * Maps Google Pay payment data to payload data and creates the OTT request.
     *
     * This function extracts billing/shipping addresses, card info, and token from PaymentData JSON,
     * then wraps it in the OTT request structure matching the web client's createOTT function.
     * Note: Amount is NOT included in the payload, only used for event responses.
     *
     * @param paymentData The [PaymentData] object containing the payment result.
     * @return A [CreateGooglePayTokenRequest] wrapped OTT request with base64 encoded payload.
     * @throws JSONException If there is an error parsing the JSON data.
     */
    @Throws(JSONException::class)
    private fun mapGooglePayData(paymentData: PaymentData): CreateGooglePayTokenRequest {
        val jsonString = paymentData.toJson()
        val paymentJson = JSONObject(jsonString)
        val paymentMethodData = paymentJson.getJSONObject("paymentMethodData")
        val info = paymentMethodData.optJSONObject("info")
        val tokenizationData =
            paymentMethodData.getJSONObject(MobileSDKConstants.GooglePayConfig.TOKENIZATION_DATA_KEY)
        val refToken = tokenizationData.getString(MobileSDKConstants.GooglePayConfig.TOKEN_KEY)

        // Extract billing address
        val billingAddress = info?.optJSONObject("billingAddress")?.let { billing ->
            GooglePayPayloadData.BillingAddress(
                addressLine1 = billing.optString("address1").takeIf { it.isNotEmpty() },
                addressLine2 = billing.optString("address2").takeIf { it.isNotEmpty() },
                addressCountry = billing.optString("countryCode").takeIf { it.isNotEmpty() },
                addressCity = billing.optString("locality").takeIf { it.isNotEmpty() },
                addressPostcode = billing.optString("postalCode").takeIf { it.isNotEmpty() },
                addressState = billing.optString("administrativeArea").takeIf { it.isNotEmpty() }
            )
        }

        // Extract shipping address
        val shippingAddress = paymentJson.optJSONObject("shippingAddress")?.let { shipping ->
            GooglePayPayloadData.ShippingAddress(
                addressLine1 = shipping.optString("address1").takeIf { it.isNotEmpty() },
                addressLine2 = shipping.optString("address2").takeIf { it.isNotEmpty() },
                addressCountry = shipping.optString("countryCode").takeIf { it.isNotEmpty() },
                addressCity = shipping.optString("locality").takeIf { it.isNotEmpty() },
                addressPostcode = shipping.optString("postalCode").takeIf { it.isNotEmpty() },
                addressState = shipping.optString("administrativeArea").takeIf { it.isNotEmpty() }
            )
        }

        // Extract card info
        val cardInfo = info?.let {
            val cardNetwork = it.optString("cardNetwork").takeIf { it.isNotEmpty() }
            val cardDetails = it.optString("cardDetails").takeIf { it.isNotEmpty() }
            if (cardNetwork != null || cardDetails != null) {
                GooglePayPayloadData.CardInfo(
                    cardScheme = cardNetwork?.lowercase(),
                    cardNumberLast4 = cardDetails
                )
            } else {
                null
            }
        }

        // Create payload data (without amount - amount is only used in event responses)
        val payloadData = GooglePayPayloadData(
            refToken = refToken,
            billing = billingAddress,
            shipping = shippingAddress,
            cardInfo = cardInfo
        )

        // Serialize payload data to JSON and base64 encode it (matching web client's btoa)
        val payloadJson = Json.encodeToString(payloadData)
        val payloadBase64 = Base64.encodeToString(payloadJson.toByteArray(), Base64.NO_WRAP)

        // Create OTT request wrapper matching web client structure
        return CreateGooglePayTokenRequest(
            serviceId = config.serviceId,
            serviceType = "GooglePay",
            serviceGroup = "wallet",
            payload = payloadBase64,
            payloadFormat = "encrypted_string"
        )
    }

    /**
     * Creates a [Task] that starts the payment process with the transaction details included.
     *
     * @return A [Task] with the payment information.
     */
    private fun getLoadPaymentDataTask(): Task<PaymentData> {
        val request =
            PaymentDataRequest.fromJson(config.paymentRequest.toJsonString())
        return paymentsClient.loadPaymentData(request)
    }

    /**
     * Processes the Google Pay payment result and creates a payment token.
     *
     * @param paymentData The [PaymentData] containing the payment result.
     */
    fun processGooglePayPaymentResult(paymentData: PaymentData) {
        updateUiState(GooglePayUIState.Loading)

        runCatching {
            val response = PaymentsUtil.parsePaymentResponse(paymentData)

            val email = response.email.takeIf { !it.isNullOrBlank() }
            val billingAddress = response.paymentMethodData.info?.billingAddress
            val shippingAddress = response.shippingAddress

            // mapGooglePayData still needed for the raw Base64 payload required by Paydock OTT API
            val tokenRequest = mapGooglePayData(paymentData)

            // Pass structured data to createPaymentToken
            Triple(tokenRequest, email, billingAddress to shippingAddress)
        }.onSuccess { (tokenRequest, email, addresses) ->
            val (billing, shipping) = addresses
            createPaymentToken(tokenRequest, email, billing, shipping)
        }.onFailure { exception ->
            handleErrorResult(
                exception.message ?: MobileSDKConstants.GooglePayConfig.Errors.TOKEN_ERROR,
                cause = exception
            )
        }
    }

    /**
     * Creates a payment token from the Google Pay payment data.
     *
     * @param tokenRequest The [CreateGooglePayTokenRequest] containing the Google Pay token and related data.
     */
    private fun createPaymentToken(
        tokenRequest: CreateGooglePayTokenRequest,
        email: String?,
        billingAddress: GooglePayBillingAddress?,
        shippingAddress: GooglePayBillingAddress?
    ) {
        launchOnIO {
            createGooglePayTokenUseCase(config.accessToken, tokenRequest)
                .onSuccess { tokenDetails ->
                    updateUiState(
                        GooglePayUIState.Success(
                            GooglePayResult(
                                token = tokenDetails.token,
                                type = tokenDetails.type,
                                email = email,
                                billingAddress = billingAddress,
                                shippingAddress = shippingAddress
                            )
                        )
                    )
                }
                .onFailure { exception ->
                    handleErrorResultWhileActive(
                        exception.message
                            ?: MobileSDKConstants.GooglePayConfig.Errors.GOOGLE_PAY_ERROR,
                        cause = exception
                    )
                }
        }
    }

    /**
     * Handles errors returned from the Google Pay SDK after a payment attempt.
     * Maps Google Pay SDK status codes to internal [GooglePayException.SDKException] subclasses.
     *
     * @param statusCode The status code returned by the Google Pay SDK.
     */
    fun handleGooglePayResultErrors(statusCode: Int) {
        val exception = when (statusCode) {
            CommonStatusCodes.CANCELED,
            WalletConstants.ERROR_CODE_USER_CANCELLED -> GooglePayException.SDKException.CancelledBySdk(
                statusCodeString = "CANCELED"
            )

            CommonStatusCodes.NETWORK_ERROR -> GooglePayException.SDKException.NetworkError(
                statusCodeString = "NETWORK_ERROR"
            )

            CommonStatusCodes.TIMEOUT -> GooglePayException.SDKException.Timeout(statusCodeString = "TIMEOUT")
            CommonStatusCodes.DEVELOPER_ERROR,
            WalletConstants.ERROR_CODE_DEVELOPER_ERROR -> GooglePayException.SDKException.DeveloperError(
                statusCodeString = "DEVELOPER_ERROR"
            )

            CommonStatusCodes.SIGN_IN_REQUIRED -> GooglePayException.SDKException.PlayServicesError(
                statusCodeString = "SIGN_IN_REQUIRED"
            )

            CommonStatusCodes.INTERNAL_ERROR,
            CommonStatusCodes.ERROR,
            CommonStatusCodes.INTERRUPTED,
            WalletConstants.ERROR_CODE_INTERNAL_ERROR -> GooglePayException.SDKException.ServiceError(
                statusCodeString = statusCode.toString()
            )

            else -> {
                val statusString = "unknown status code: $statusCode"
                GooglePayException.SDKException.UnknownSdkException(
                    "[$statusString] ${MobileSDKConstants.GooglePayConfig.Errors.GOOGLE_PAY_ERROR}",
                    statusString
                )
            }
        }
        // A cancelled sheet and a required sign-in are the shopper's outcomes, not SDK failures: not reported.
        if (exception !is GooglePayException.SDKException.CancelledBySdk &&
            exception !is GooglePayException.SDKException.PlayServicesError
        ) {
            telemetry.reportSdkError(exception, SdkWidget.GOOGLE_PAY)
        }
        updateUiState(GooglePayUIState.Error(exception))
    }

    /**
     * Handles errors returned from the Google Pay SDK as a [Status] object.
     *
     * @param status The [Status] object returned by the Google Pay SDK.
     */
    fun handleWalletResultErrors(status: Status?) {
        val statusCode = status?.statusCode ?: CommonStatusCodes.INTERNAL_ERROR
        handleGooglePayResultErrors(statusCode)
    }

    /**
     * Reports a failure the widget builds and hands to the merchant itself, outside this view model's state (GP8).
     *
     * @param error The error the merchant gets.
     * @param cause The original throwable [error] was built from, if any.
     */
    fun reportWidgetError(error: Throwable, cause: Throwable? = null) {
        telemetry.reportSdkError(error, SdkWidget.GOOGLE_PAY, cause = cause)
    }

    /**
     * Ends the open payment attempt as an error with the ResultException code when Google Pay returns no payment data
     * (§ 5.9): the widget's no-data branches hand the merchant nothing, so no state changes here.
     */
    fun handleMissingPaymentData() {
        if (paymentId == null) return
        finishAttempt(
            TelemetryEvent.PaymentOutcome.ERROR,
            GooglePayException.ResultException(MobileSDKConstants.GooglePayConfig.Errors.TOKEN_ERROR)
        )
    }

    /**
     * Resets the UI state to Idle.
     */
    fun resetResultState() {
        updateUiState(GooglePayUIState.Idle)
    }
    //endregion
}

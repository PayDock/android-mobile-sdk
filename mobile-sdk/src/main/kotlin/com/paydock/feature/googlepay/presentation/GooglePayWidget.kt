package com.paydock.feature.googlepay.presentation

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.wallet.PaymentData
import com.google.pay.button.ButtonTheme
import com.google.pay.button.ButtonType
import com.google.pay.button.PayButton
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.GooglePayException
import com.paydock.designsystems.components.loader.SdkLoader
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.googlepay.presentation.viewmodels.GooglePayViewModel
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.koin.androidx.compose.koinViewModel

/**
 * A Composable for handling Google Pay payments and related interactions.
 *
 * @param modifier Modifier for customizing the appearance and behavior of the Composable.
 * @param token A callback to obtain the wallet token asynchronously.
 * @param isReadyToPayRequest The request to check if the user is ready to pay.
 * @param paymentRequest The payment request details.
 * @param completion A callback to handle the Google Pay result.
 */
@Suppress("LongMethod")
@Composable
fun GooglePayWidget(
    modifier: Modifier = Modifier,
    token: (onTokenReceived: (String) -> Unit) -> Unit,
    isReadyToPayRequest: JSONObject,
    paymentRequest: JSONObject,
    completion: (Result<ChargeResponse>) -> Unit
) {
    // Retrieve the GooglePayViewModel using Koin
    val viewModel: GooglePayViewModel = koinViewModel()

    // Get the current context and coroutine scope
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Ensure this is only done once
    LaunchedEffect(Unit) {
        // Fetch information about Google Pay availability
        viewModel.fetchCanUseGooglePay(isReadyToPayRequest)
    }

    // Collect the UI state from the ViewModel
    val uiState by viewModel.stateFlow.collectAsState()

    // Extract allowed payment methods from the payment request
    val allowedPaymentMethods: JSONArray? = runCatching {
        paymentRequest.getJSONArray("allowedPaymentMethods")
    }.onFailure {
        completion(
            Result.failure(
                GooglePayException.InitialisationException(stringResource(id = R.string.error_googlepay_payment_methods))
            )
        )
        viewModel.resetResultState()
    }.getOrNull()

    // Set up a launcher for handling Google Pay resolution
    val resolvePaymentForResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result: ActivityResult ->
        val walletToken = uiState.token
        if (!walletToken.isNullOrBlank()) {
            handleGooglePayActivityResult(context, result, walletToken, viewModel, completion)
        } else {
            completion(
                Result.failure(
                    GooglePayException.UnknownException(
                        displayableMessage = context.getString(
                            R.string.error_googlepay_unexpected
                        )
                    )
                )
            )
            viewModel.resetResultState()
        }
    }

    // Handle error flow and display
    LaunchedEffect(uiState.error) {
        handleGooglePayUiStateErrors(
            uiState.error,
            resolvePaymentForResult,
            completion
        )
    }

    // Handle token result and reset the payment state
    LaunchedEffect(uiState.paymentData) {
        uiState.paymentData?.let { paymentData ->
            val walletToken = uiState.token
            if (!walletToken.isNullOrBlank()) {
                handleGooglePayPaymentResult(paymentData, walletToken, viewModel, completion)
            } else {
                completion(
                    Result.failure(
                        GooglePayException.UnknownException(
                            displayableMessage = context.getString(
                                R.string.error_googlepay_unexpected
                            )
                        )
                    )
                )
                viewModel.resetResultState()
            }
        }
    }

    // Handle wallet response result and reset state
    LaunchedEffect(uiState.chargeData) {
        uiState.chargeData?.let { response ->
            completion(Result.success(response))
            viewModel.resetResultState()
        }
    }

    // Reset form state when the widget is dismissed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetResultState()
        }
    }

    // Composable content rendering
    SdkTheme {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(Theme.dimensions.spacing, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Show the Google Pay button when it's available
                AnimatedVisibility(
                    visible = uiState.googlePayAvailable,
                    enter = fadeIn(animationSpec = tween(MobileSDKConstants.General.DEFAULT_ANIMATION_DURATION)),
                    exit = fadeOut(animationSpec = tween(MobileSDKConstants.General.DEFAULT_ANIMATION_DURATION))
                ) {
                    // Google Pay Button - https://developers.google.com/pay/api/android/reference/request-objects#ButtonOptions
                    PayButton(
                        modifier = Modifier
                            .testTag("payButton")
                            .fillMaxWidth(),
                        theme = if (isSystemInDarkTheme()) ButtonTheme.Dark else ButtonTheme.Light,
                        type = ButtonType.Pay,
                        onClick = {
                            // Use the callback to obtain the token asynchronously
                            token { obtainedToken ->
                                scope.launch {
                                    viewModel.setWalletToken(obtainedToken)
                                    // Initiate the Google Pay payment process
                                    viewModel.requestPayment(paymentRequest)
                                }
                            }
                        },
                        radius = Theme.dimensions.cornerRadius,
                        allowedPaymentMethods = allowedPaymentMethods.toString()
                    )
                }
            }
            if (uiState.isLoading) {
                SdkLoader()
            }
        }
    }
}

/**
 * Handle Google Pay UI state errors.
 *
 * @param error ErrorModel indicating the error type.
 * @param resolvePaymentForResult Launcher for resolving payment issues.
 * @param onGooglePayResult Callback to handle Google Pay result.
 */
private fun handleGooglePayUiStateErrors(
    error: GooglePayException?,
    resolvePaymentForResult: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
    onGooglePayResult: (Result<ChargeResponse>) -> Unit
) {
    if (error != null) {
        when (error) {
            is GooglePayException.PaymentRequestException -> {
                when (error.exception) {
                    is ResolvableApiException -> {
                        // Resolve Google Pay payment issues using a launcher
                        resolvePaymentForResult.launch(
                            IntentSenderRequest.Builder(error.exception.resolution).build()
                        )
                    }
                }
            }

            else -> {
                handleUiStateErrors(error, onGooglePayResult)
            }
        }
    }
}

/**
 * Handle UI state errors and invoke the callback for error handling.
 *
 * @param error ErrorModel indicating the error type.
 * @param onGooglePayResult Callback to handle Google Pay result.
 */
private fun handleUiStateErrors(
    error: GooglePayException?,
    onGooglePayResult: (Result<ChargeResponse>) -> Unit
) {
    error?.let {
        onGooglePayResult(Result.failure(it))
    }
}

/**
 * Handle the result of a Google Pay payment attempt.
 *
 * @param result The [ActivityResult] indicating the result of the Google Pay payment attempt.
 * @param token The authentication token for initiating the payment.
 * @param viewModel The [GooglePayViewModel] responsible for capturing the wallet charge.
 * @param onGooglePayResult The callback to handle the Google Pay result.
 */
private fun handleGooglePayActivityResult(
    context: Context,
    result: ActivityResult,
    token: String,
    viewModel: GooglePayViewModel,
    onGooglePayResult: (Result<ChargeResponse>) -> Unit
) {
    when (result.resultCode) {
        ComponentActivity.RESULT_OK -> {
            result.data?.let { intent ->
                PaymentData.getFromIntent(intent)?.let { paymentData ->
                    handleGooglePayPaymentResult(
                        paymentData,
                        token,
                        viewModel,
                        onGooglePayResult
                    )
                }
            }
        }

        ComponentActivity.RESULT_CANCELED -> {
            // The user cancelled the payment attempt
            onGooglePayResult(
                Result.failure(
                    GooglePayException.CancellationException(
                        context.getString(R.string.error_googlepay_cancelled)
                    )
                )
            )
        }

        else -> {
            onGooglePayResult(Result.failure(GooglePayException.UnknownException(context.getString(R.string.error_googlepay_unexpected))))
        }
    }
}

/**
 * Handle a successful Google Pay payment and extract the payment token.
 * PaymentData response object contains the payment information, as well as any additional
 * requested information, such as billing and shipping address.
 *
 * @param paymentData A response object returned by Google after a payer approves payment.
 * @param token The wallet token provided for initiating the wallet charge
 * @param viewModel [GooglePayViewModel] responsible for capturing the wallet charge
 * @param onGooglePayResult result callback
 * @see [Payment
 * Data](https://developers.google.com/pay/api/android/reference/request-objects#PaymentData)
 */
private fun handleGooglePayPaymentResult(
    paymentData: PaymentData,
    token: String,
    viewModel: GooglePayViewModel,
    onGooglePayResult: (Result<ChargeResponse>) -> Unit
) {
    val paymentInformation = paymentData.toJson()
    runCatching {
        val paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")
        val googleToken = paymentMethodData.getJSONObject("tokenizationData").getString("token")
        if (!googleToken.isNullOrBlank()) {
            viewModel.captureWalletTransaction(token, googleToken)
        } else {
            onGooglePayResult(Result.failure(GooglePayException.ResultException("Google Pay Token Error")))
        }
    }.onFailure {
        onGooglePayResult(Result.failure(it))
    }
}
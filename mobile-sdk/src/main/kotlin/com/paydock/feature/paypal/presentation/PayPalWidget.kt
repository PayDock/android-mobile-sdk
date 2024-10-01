package com.paydock.feature.paypal.presentation

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.PayPalException
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.core.presentation.utils.getWebViewMessageExtra
import com.paydock.core.presentation.utils.getWebViewStatusExtra
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.paypal.presentation.components.PayPalButton
import com.paydock.feature.paypal.presentation.state.PayPalData
import com.paydock.feature.paypal.presentation.state.PayPalViewState
import com.paydock.feature.paypal.presentation.utils.CancellationStatus
import com.paydock.feature.paypal.presentation.utils.getCancellationStatusExtra
import com.paydock.feature.paypal.presentation.utils.getDecodedUrlExtra
import com.paydock.feature.paypal.presentation.utils.putCallbackUrlExtra
import com.paydock.feature.paypal.presentation.viewmodels.PayPalViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * A Composable for handling PayPal payments and related interactions.
 *
 * @param modifier Modifier for customizing the appearance and behavior of the Composable.
 * @param token A callback to obtain the wallet token asynchronously.
 * @param requestShipping Flag passed to determine if PayPal will ask the user for their shipping address.
 * @param completion A callback to handle the Wallet Charge result.
 */
@Suppress("LongMethod")
@Composable
fun PayPalWidget(
    modifier: Modifier = Modifier,
    token: (onTokenReceived: (String) -> Unit) -> Unit,
    requestShipping: Boolean = true,
    completion: (Result<ChargeResponse>) -> Unit
) {
    val context = LocalContext.current
    // Obtain instances of view models
    val viewModel: PayPalViewModel = koinViewModel()

    // Collect states for PayPal view models
    val uiState by viewModel.stateFlow.collectAsState()

    val scope = rememberCoroutineScope()

    // ActivityResultLauncher for handling payment resolution
    val resolvePaymentForResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        handlePayPalResult(context, result, completion, viewModel)
    }

    LaunchedEffect(uiState) {
        handleUiState(context, uiState, completion, resolvePaymentForResult, viewModel)
    }

    // Reset form state when the widget is dismissed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetResultState()
        }
    }

    SdkTheme {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            // Button to initiate PayPal transaction
            PayPalButton(isEnabled = !uiState.isLoading, isLoading = uiState.isLoading) {
                // Use the callback to obtain the token asynchronously
                token { obtainedToken ->
                    scope.launch {
                        viewModel.setWalletToken(obtainedToken)
                        viewModel.getWalletCallback(
                            walletToken = obtainedToken,
                            requestShipping = requestShipping
                        )
                    }
                }
            }
        }
    }
}

/**
 * Handles the result returned from the PayPal web activity, processing the decoded URL,
 * cancellation, or errors, and invoking the appropriate completion handler.
 *
 * @param context The context used for accessing resources and displaying error messages.
 * @param result The `ActivityResult` returned from the PayPal web activity.
 * This contains the result code and data such as the decoded URL or cancellation status.
 * @param completion A callback function to handle the result of the PayPal transaction.
 * It is invoked with a `Result` object containing either success or failure information.
 * @param viewModel The PayPalViewModel that processes PayPal-related data, including the parsed PayPal URL.
 */
private fun handlePayPalResult(
    context: Context,
    result: ActivityResult,
    completion: (Result<ChargeResponse>) -> Unit,
    viewModel: PayPalViewModel
) {
    result.data?.let { data ->
        when (result.resultCode) {
            // Handles the success case when the result code is RESULT_OK, parsing the PayPal URL.
            AppCompatActivity.RESULT_OK -> {
                data.getDecodedUrlExtra()?.let { decodedUrl ->
                    viewModel.parsePayPalUrl(decodedUrl)
                }
            }

            // Handles the cancellation case when the result code is RESULT_CANCELED.
            AppCompatActivity.RESULT_CANCELED -> {
                when (data.getCancellationStatusExtra()) {
                    // If the cancellation was user-initiated, the completion is invoked with a failure result.
                    CancellationStatus.USER_INITIATED -> {
                        completion(
                            Result.failure(
                                PayPalException.CancellationException(
                                    displayableMessage = context.getString(
                                        R.string.error_paypal_cancelled
                                    )
                                )
                            )
                        )
                    }

                    // If the cancellation was due to another reason, process the WebView error status and message.
                    else -> {
                        val status = data.getWebViewStatusExtra()
                        val message =
                            data.getWebViewMessageExtra(MobileSDKConstants.Errors.PAY_PAL_ERROR)
                        completion(
                            Result.failure(
                                PayPalException.WebViewException(
                                    code = status,
                                    displayableMessage = message
                                )
                            )
                        )
                    }
                }
            }

            // If no specific result code is handled, do nothing.
            else -> Unit
        }
    }
}

/**
 * Handles different states of the PayPal payment flow by inspecting the provided `uiState`.
 * Depending on the state, it either completes the process with success or failure,
 * launches a web activity, or processes payment data.
 *
 * @param context The context used for accessing resources and launching activities.
 * @param uiState The current UI state of the PayPal payment flow, containing various data like errors,
 * charge data, payment data, and callback data.
 * @param completion A callback function to handle the result of the charge operation.
 * This callback receives a `Result` object with either success or failure.
 * @param resolvePaymentForResult A launcher used to start the PayPal web activity for the callback URL.
 * @param viewModel The PayPalViewModel that manages the PayPal payment flow and UI state.
 */
private fun handleUiState(
    context: Context,
    uiState: PayPalViewState,
    completion: (Result<ChargeResponse>) -> Unit,
    resolvePaymentForResult: ActivityResultLauncher<Intent>,
    viewModel: PayPalViewModel
) {
    when {
        // Handles the error state by passing the error to the completion handler and resetting the state.
        uiState.error != null -> {
            completion(Result.failure(uiState.error))
            viewModel.resetResultState()
        }

        // Handles the success state when charge data is available and passes it to the completion handler.
        uiState.chargeData != null -> {
            completion(Result.success(uiState.chargeData))
            viewModel.resetResultState()
        }

        // Processes payment data if available, and handles cases where the wallet token is missing or invalid.
        uiState.paymentData != null -> {
            val walletToken = uiState.token
            if (!walletToken.isNullOrBlank()) {
                handlePayPalPaymentResult(uiState.paymentData, walletToken, viewModel)
            } else {
                // Handles unknown errors and completes with failure.
                completion(
                    Result.failure(
                        PayPalException.UnknownException(
                            displayableMessage = context.getString(R.string.error_web_unknown_error)
                        )
                    )
                )
                viewModel.resetResultState()
            }
        }

        // Launches the PayPal web activity if callback data is available with a valid callback URL.
        uiState.callbackData != null -> {
            uiState.callbackData.callbackUrl?.let { callbackUrl ->
                if (callbackUrl.isNotBlank()) {
                    val intent = Intent(context, PayPalWebActivity::class.java)
                        .putCallbackUrlExtra(callbackUrl)
                    resolvePaymentForResult.launch(intent)
                }
            }
        }
    }
}

/**
 * Handle a successful PayPal payment
 *
 * @param paymentData payment object containing the parsed data from the redirect url.
 * @param token The wallet token provided for initiating the wallet charge
 * @param viewModel [PayPalViewModel] responsible for capturing the wallet charge
 * @see [Payment
 * Data](https://developers.google.com/pay/api/android/reference/request-objects#PaymentData)
 */
internal fun handlePayPalPaymentResult(
    paymentData: PayPalData,
    token: String,
    viewModel: PayPalViewModel
) {
    viewModel.captureWalletTransaction(
        walletToken = token,
        paymentMethodId = paymentData.payPalToken,
        payerId = paymentData.payerId
    )
}

@LightDarkPreview
@Composable
private fun PreviewPayPalWidget() {
    SdkTheme {
        PayPalWidget(token = {}, completion = {})
    }
}
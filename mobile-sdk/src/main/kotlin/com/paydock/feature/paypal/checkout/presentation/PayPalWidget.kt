package com.paydock.feature.paypal.checkout.presentation

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.PayPalException
import com.paydock.core.presentation.extensions.getMessageExtra
import com.paydock.core.presentation.extensions.getStatusExtra
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.feature.charge.domain.model.integration.ChargeResponse
import com.paydock.feature.paypal.checkout.presentation.components.PayPalButton
import com.paydock.feature.paypal.checkout.presentation.state.PayPalCheckoutUIState
import com.paydock.feature.paypal.checkout.presentation.utils.CancellationStatus
import com.paydock.feature.paypal.checkout.presentation.utils.getCancellationStatusExtra
import com.paydock.feature.paypal.checkout.presentation.utils.getDecodedUrlExtra
import com.paydock.feature.paypal.checkout.presentation.utils.putCallbackUrlExtra
import com.paydock.feature.paypal.checkout.presentation.viewmodels.PayPalViewModel
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
    completion: (Result<ChargeResponse>) -> Unit,
) {
    val context = LocalContext.current
    // Obtain instances of view models
    val viewModel: PayPalViewModel = koinViewModel()

    // Collect states for PayPal view models
    val uiState by viewModel.stateFlow.collectAsState()

    val scope = rememberCoroutineScope()

    var isPayPalFlowLaunched by remember { mutableStateOf(false) }

    // ActivityResultLauncher for handling payment resolution
    val resolvePaymentForResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        handlePayPalResult(context, result, completion, viewModel)
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is PayPalCheckoutUIState.Idle,
            is PayPalCheckoutUIState.Loading -> Unit
            is PayPalCheckoutUIState.LaunchIntent -> {
                if (!isPayPalFlowLaunched) {
                    val (callbackData) = state
                    callbackData.callbackUrl?.let { callbackUrl ->
                        if (callbackUrl.isNotBlank()) {
                            val intent = Intent(context, PayPalWebActivity::class.java)
                                .putCallbackUrlExtra(callbackUrl)
                            resolvePaymentForResult.launch(intent)
                            isPayPalFlowLaunched = true
                        }
                    }
                }
            }

            is PayPalCheckoutUIState.Capture -> {
                val (paymentMethodId, payerId) = state
                viewModel.captureWalletTransaction(paymentMethodId, payerId)
            }

            is PayPalCheckoutUIState.Success -> {
                completion(Result.success(state.chargeData))
                // This ensures that we clear the state so it's not reused
                viewModel.resetResultState()
            }

            is PayPalCheckoutUIState.Error -> {
                completion(Result.failure(state.exception as Throwable))
                // This ensures that we clear the state so it's not reused
                viewModel.resetResultState()
            }
        }
    }

    SdkTheme {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            // Button to initiate PayPal transaction
            PayPalButton(isEnabled = uiState !is PayPalCheckoutUIState.Loading, isLoading = uiState is PayPalCheckoutUIState.Loading) {
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
    viewModel: PayPalViewModel,
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
                                        R.string.error_paypal_canceled
                                    )
                                )
                            )
                        )
                        viewModel.resetResultState()
                    }

                    // If the cancellation was due to another reason, process the WebView error status and message.
                    else -> {
                        val status = data.getStatusExtra()
                        val message =
                            data.getMessageExtra(MobileSDKConstants.Errors.PAY_PAL_ERROR)
                        completion(
                            Result.failure(
                                PayPalException.WebViewException(
                                    code = status,
                                    displayableMessage = message
                                )
                            )
                        )
                        viewModel.resetResultState()
                    }
                }
            }

            // If no specific result code is handled, do nothing.
            else -> Unit
        }
    }
}

@LightDarkPreview
@Composable
private fun PreviewPayPalWidget() {
    SdkTheme {
        PayPalWidget(token = {}, completion = {})
    }
}
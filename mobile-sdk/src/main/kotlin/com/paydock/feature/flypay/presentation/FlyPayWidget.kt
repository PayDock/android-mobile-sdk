package com.paydock.feature.flypay.presentation

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
import com.paydock.core.domain.error.exceptions.FlyPayException
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.core.presentation.utils.getWebViewMessageExtra
import com.paydock.core.presentation.utils.getWebViewStatusExtra
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.feature.flypay.presentation.components.FlyPayButton
import com.paydock.feature.flypay.presentation.state.FlyPayViewState
import com.paydock.feature.flypay.presentation.utils.CancellationStatus
import com.paydock.feature.flypay.presentation.utils.getCancellationStatusExtra
import com.paydock.feature.flypay.presentation.utils.getOrderIdExtra
import com.paydock.feature.flypay.presentation.utils.putClientIdExtra
import com.paydock.feature.flypay.presentation.utils.putOrderIdExtra
import com.paydock.feature.flypay.presentation.viewmodels.FlyPayViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Composable function for rendering the FlyPay widget.
 *
 * @param modifier Modifier for customizing the appearance and behavior of the Composable.
 * @param clientId Merchant clientId.
 * @param token A callback to obtain the wallet token asynchronously.
 * @param completion A callback to handle the result of the FlyPay operation.
 */
@Suppress("LongMethod")
@Composable
fun FlyPayWidget(
    modifier: Modifier = Modifier,
    clientId: String,
    token: (onTokenReceived: (String) -> Unit) -> Unit,
    completion: (Result<String>) -> Unit
) {
    val context = LocalContext.current
    // Obtain instances of view models
    val viewModel: FlyPayViewModel = koinViewModel()

    // Collect states for FlyPay and Wallet view models
    val uiState by viewModel.stateFlow.collectAsState()

    val scope = rememberCoroutineScope()

    // ActivityResultLauncher for handling payment resolution
    val resolvePaymentForResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        handleFlyPayResult(context, result, completion, viewModel)
    }

    // Handle wallet response result and reset state
    LaunchedEffect(uiState) {
        handleUiState(context, uiState, clientId, completion, resolvePaymentForResult, viewModel)
    }

    // Reset form state when the widget is dismissed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetResultState()
        }
    }

    // Composable content rendering
    SdkTheme {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            // Button to initiate FlyPay transaction
            FlyPayButton(
                onClick = {
                    // Use the callback to obtain the token asynchronously
                    token { obtainedToken ->
                        scope.launch {
                            viewModel.setWalletToken(obtainedToken)
                            viewModel.getWalletCallback(
                                walletToken = obtainedToken
                            )
                        }
                    }
                },
                isEnabled = !uiState.isLoading,
                isLoading = uiState.isLoading
            )
        }
    }
}

/**
 * Handles the result returned from the FlyPay web activity, processing the order ID,
 * cancellation, or errors, and invoking the appropriate completion handler.
 *
 * @param context The context used for accessing resources and displaying error messages.
 * @param result The `ActivityResult` returned from the FlyPay web activity.
 * This contains the result code and data such as the order ID or cancellation status.
 * @param completion A callback function to handle the result of the FlyPay transaction.
 * It is invoked with a `Result` object containing either success (with the order ID) or failure information.
 * @param viewModel The FlyPayViewModel that processes FlyPay-related data and manages the result state.
 */
private fun handleFlyPayResult(
    context: Context,
    result: ActivityResult,
    completion: (Result<String>) -> Unit,
    viewModel: FlyPayViewModel
) {
    result.data?.let { data ->
        when (result.resultCode) {
            // Handles the success case when the result code is RESULT_OK, passing the order ID.
            AppCompatActivity.RESULT_OK -> {
                data.getOrderIdExtra()?.let { orderId ->
                    completion(Result.success(orderId))
                    viewModel.resetResultState()
                }
            }

            // Handles the cancellation case when the result code is RESULT_CANCELED.
            AppCompatActivity.RESULT_CANCELED -> {
                when (data.getCancellationStatusExtra()) {
                    // If the cancellation was user-initiated, the completion is invoked with a failure result.
                    CancellationStatus.USER_INITIATED -> {
                        completion(
                            Result.failure(
                                FlyPayException.CancellationException(
                                    displayableMessage = context.getString(
                                        R.string.error_flypay_cancelled
                                    )
                                )
                            )
                        )
                    }

                    // If the cancellation was due to another reason, process the WebView error status and message.
                    else -> {
                        val status = data.getWebViewStatusExtra()
                        val message =
                            data.getWebViewMessageExtra(MobileSDKConstants.Errors.FLY_PAY_ERROR)
                        completion(
                            Result.failure(
                                FlyPayException.WebViewException(
                                    status,
                                    message
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
 * Handles the current UI state of the FlyPay payment process, processing errors,
 * callback data, and launching the FlyPay web activity if necessary.
 *
 * @param context The context used for accessing resources and launching activities.
 * @param uiState The current state of the FlyPay payment process, which may contain an error or callback data.
 * @param clientId The client ID used for authenticating or identifying the FlyPay transaction.
 * @param completion A callback function to handle the result of the FlyPay transaction.
 * It is invoked with a `Result` object containing either success (with a string) or failure information.
 * @param resolvePaymentForResult An `ActivityResultLauncher` used to launch the FlyPay web activity and handle the result.
 * @param viewModel The FlyPayViewModel that manages FlyPay-related data and result state.
 */
private fun handleUiState(
    context: Context,
    uiState: FlyPayViewState,
    clientId: String,
    completion: (Result<String>) -> Unit,
    resolvePaymentForResult: ActivityResultLauncher<Intent>,
    viewModel: FlyPayViewModel
) {
    when {
        // Handles the error state, invoking the completion handler with a failure result.
        uiState.error != null -> {
            completion(Result.failure(uiState.error))
            viewModel.resetResultState()
        }

        // Handles the callback data, extracting the FlyPay order ID and launching the FlyPay web activity.
        uiState.callbackData != null -> {
            uiState.callbackData.callbackId?.let { flyPayOrderId ->
                val intent = Intent(context, FlyPayWebActivity::class.java)
                    .putOrderIdExtra(flyPayOrderId) // Adds the FlyPay order ID to the intent.
                    .putClientIdExtra(clientId) // Adds the client ID to the intent.
                resolvePaymentForResult.launch(intent) // Launches the FlyPay web activity.
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun PreviewFlyPayWidget() {
    SdkTheme {
        FlyPayWidget(clientId = "", token = {}, completion = {})
    }
}
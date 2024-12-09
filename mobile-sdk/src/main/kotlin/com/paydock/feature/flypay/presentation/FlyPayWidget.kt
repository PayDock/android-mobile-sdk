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
import com.paydock.core.presentation.extensions.getMessageExtra
import com.paydock.core.presentation.extensions.getStatusExtra
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.feature.flypay.presentation.components.FlyPayButton
import com.paydock.feature.flypay.presentation.state.FlyPayUIState
import com.paydock.feature.flypay.presentation.utils.CancellationStatus
import com.paydock.feature.flypay.presentation.utils.getCancellationStatusExtra
import com.paydock.feature.flypay.presentation.utils.getOrderIdExtra
import com.paydock.feature.flypay.presentation.utils.putClientIdExtra
import com.paydock.feature.flypay.presentation.utils.putOrderIdExtra
import com.paydock.feature.flypay.presentation.viewmodels.FlyPayViewModel
import io.ktor.http.parameters
import io.ktor.http.parametersOf
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * A Composable for handling FlyPay payments and related interactions.
 *
 * @param modifier Modifier for customizing the appearance and behavior of the Composable.
 * @param enabled Controls the enabled state of this Widget. When false,
 * this component will not respond to user input, and it will appear visually disabled.
 * @param clientId FlyPay Merchant clientId.
 * @param token A callback to obtain the wallet token asynchronously.
 * @param loadingDelegate The delegate passed to overwrite control of showing loaders.
 * @param completion A callback to handle the result of the FlyPay operation.
 */
@Composable
fun FlyPayWidget(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    clientId: String,
    token: (onTokenReceived: (String) -> Unit) -> Unit,
    loadingDelegate: WidgetLoadingDelegate? = null,
    completion: (Result<String>) -> Unit
) {
    val context = LocalContext.current
    // Obtain instances of view models
    val viewModel: FlyPayViewModel = koinViewModel(parameters = {
        parametersOf(clientId)
    })

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
        handleUiState(
            context,
            uiState,
            viewModel,
            loadingDelegate,
            completion,
            resolvePaymentForResult
        )
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
                isEnabled = uiState !is FlyPayUIState.Loading && enabled,
                isLoading = loadingDelegate == null && uiState is FlyPayUIState.Loading
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
                val orderId = data.getOrderIdExtra()
                orderId?.let { viewModel.completeResult(orderId) }
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
                                        R.string.error_flypay_canceled
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
                            data.getMessageExtra(MobileSDKConstants.Errors.FLY_PAY_ERROR)
                        completion(
                            Result.failure(
                                FlyPayException.WebViewException(
                                    status,
                                    message
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

/**
 * Handles the current UI state of the FlyPay payment process, processing errors,
 * callback data, and launching the FlyPay web activity if necessary.
 *
 * @param context The context used for accessing resources and launching activities.
 * @param uiState The current state of the FlyPay payment process, which may contain an error or callback data.
 * @param completion A callback function to handle the result of the FlyPay transaction.
 * It is invoked with a `Result` object containing either success (with a string) or failure information.
 * @param loadingDelegate The delegate passed to overwrite control of showing loaders.
 * @param resolvePaymentForResult An `ActivityResultLauncher` used to launch the FlyPay web activity and handle the result.
 * @param viewModel The FlyPayViewModel that manages FlyPay-related data and result state.
 */
private fun handleUiState(
    context: Context,
    uiState: FlyPayUIState,
    viewModel: FlyPayViewModel,
    loadingDelegate: WidgetLoadingDelegate?,
    completion: (Result<String>) -> Unit,
    resolvePaymentForResult: ActivityResultLauncher<Intent>,
) {
    when (uiState) {
        // No action needed for the Idle state
        is FlyPayUIState.Idle -> Unit

        // Handle the loading state by notifying the loading delegate
        is FlyPayUIState.Loading -> {
            loadingDelegate?.widgetLoadingDidStart()
        }

        // Launch an intent to FlyPay's Web Activity if the callback URL is available
        is FlyPayUIState.LaunchIntent -> {
            loadingDelegate?.widgetLoadingDidFinish()
            val (callbackData) = uiState
            callbackData.callbackId?.let { flyPayOrderId ->
                val intent = Intent(context, FlyPayWebActivity::class.java)
                    .putOrderIdExtra(flyPayOrderId) // Adds the FlyPay order ID to the intent.
                    .putClientIdExtra(viewModel.clientId) // Adds the client ID to the intent.
                resolvePaymentForResult.launch(intent) // Launches the FlyPay web activity.
            }
        }

        // Handle success state, notify the loading delegate, and return orderId with success
        is FlyPayUIState.Success -> {
            loadingDelegate?.widgetLoadingDidFinish()
            completion(Result.success(uiState.orderId))
            // Reset the state to ensure it’s not reused
            viewModel.resetResultState()
        }

        // Handle error state, notify the loading delegate, and complete the transaction with failure
        is FlyPayUIState.Error -> {
            loadingDelegate?.widgetLoadingDidFinish()
            completion(Result.failure(uiState.exception))
            // Reset the state to ensure it’s not reused
            viewModel.resetResultState()
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
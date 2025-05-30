package com.paydock.feature.colespay.presentation

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.ColesPayException
import com.paydock.core.presentation.extensions.getMessageExtra
import com.paydock.core.presentation.extensions.getStatusExtra
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.components.button.SdkImageButton
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.feature.colespay.presentation.state.ColesPayUIState
import com.paydock.feature.colespay.presentation.utils.CancellationStatus
import com.paydock.feature.colespay.presentation.utils.getCancellationStatusExtra
import com.paydock.feature.colespay.presentation.utils.getOrderIdExtra
import com.paydock.feature.colespay.presentation.utils.putClientIdExtra
import com.paydock.feature.colespay.presentation.utils.putOrderIdExtra
import com.paydock.feature.colespay.presentation.viewmodels.ColesPayViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * A Composable for handling Coles Pay payments and related interactions.
 *
 * @param modifier Modifier for customizing the appearance and behavior of the Composable.
 * @param enabled Controls the enabled state of this Widget. When false,
 * this component will not respond to user input, and it will appear visually disabled.
 * @param clientId Coles Pay Merchant clientId.
 * @param token A callback to obtain the wallet token asynchronously.
 * @param loadingDelegate The delegate passed to overwrite control of showing loaders.
 * @param completion A callback to handle the result of the Coles Pay operation.
 */
@Suppress("MagicNumber")
@Composable
fun ColesPayWidget(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    clientId: String,
    token: (onTokenReceived: (String) -> Unit) -> Unit,
    loadingDelegate: WidgetLoadingDelegate? = null,
    completion: (Result<String>) -> Unit
) {
    val context = LocalContext.current
    // Obtain instances of view models
    val viewModel: ColesPayViewModel = koinViewModel(parameters = {
        parametersOf(clientId)
    })

    // Collect states for Coles Pay and Wallet view models
    val uiState by viewModel.uiState.collectAsState()

    // ActivityResultLauncher for handling payment resolution
    val resolvePaymentForResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        handleColesPayResult(context, result, completion, viewModel)
    }

    // Handle wallet response result and reset state
    LaunchedEffect(uiState::class) {
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
        val isLoading = loadingDelegate == null && uiState is ColesPayUIState.Loading
        if (!isLoading) {
            SdkImageButton(
                modifier = modifier,
                shape = RoundedCornerShape(percent = 50), // ensures pill shape
                rippleColor = Color.White,
                enabled = uiState !is ColesPayUIState.Loading && enabled,
                painter = painterResource(id = R.drawable.pay_with_coles_pay_button),
                contentDescription = LocalContext.current.getString(R.string.content_desc_coles_pay_button),
            ) {
                viewModel.startColesPayFlow(token)
            }
        } else {
            val painter = painterResource(id = R.drawable.pay_with_coles_placeholder)
            val imageAspectRatio: Float =
                painter.intrinsicSize.width / painter.intrinsicSize.height.coerceAtLeast(1f)
            Box(
                modifier = modifier.aspectRatio(imageAspectRatio),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    // Scale the image to fit the parent width
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )

                // Because the Box now has a defined aspect ratio (matching the image),
                // we can use BoxWithConstraints to get its height reliably for the loader.
                BoxWithConstraints(modifier = Modifier.matchParentSize()) {
                    // Calculate the desired loader size in PIXELS first. (don't let it exceed 60% of the width)
                    val loaderSizeInPx =
                        (constraints.maxHeight * 0.6f).coerceAtMost(constraints.maxWidth * 0.6f)
                    // Convert the loader size from PIXELS to DP using LocalDensity.
                    val loaderSizeInDp = with(LocalDensity.current) { loaderSizeInPx.toDp() }
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(loaderSizeInDp)
                            .align(Alignment.Center),
                        color = Color.White,
                        strokeWidth = (loaderSizeInDp.value * 0.1f).coerceAtLeast(2f).dp
                    )
                }
            }
        }
    }
}

/**
 * Handles the result returned from the Coles Pay web activity, processing the order ID,
 * cancellation, or errors, and invoking the appropriate completion handler.
 *
 * @param context The context used for accessing resources and displaying error messages.
 * @param result The `ActivityResult` returned from the Coles Pay web activity.
 * This contains the result code and data such as the order ID or cancellation status.
 * @param completion A callback function to handle the result of the Coles Pay transaction.
 * It is invoked with a `Result` object containing either success (with the order ID) or failure information.
 * @param viewModel The [ColesPayViewModel] that processes Coles Pay-related data and manages the result state.
 */
private fun handleColesPayResult(
    context: Context,
    result: ActivityResult,
    completion: (Result<String>) -> Unit,
    viewModel: ColesPayViewModel
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
                                ColesPayException.CancellationException(
                                    displayableMessage = context.getString(
                                        R.string.error_coles_pay_canceled
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
                            data.getMessageExtra(MobileSDKConstants.Errors.COLES_PAY_ERROR)
                        completion(
                            Result.failure(
                                ColesPayException.WebViewException(
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
 * Handles the current UI state of the Coles Pay payment process, processing errors,
 * callback data, and launching the Coles Pay web activity if necessary.
 *
 * @param context The context used for accessing resources and launching activities.
 * @param uiState The current state of the Coles Pay payment process, which may contain an error or callback data.
 * @param completion A callback function to handle the result of the Coles Pay transaction.
 * It is invoked with a `Result` object containing either success (with a string) or failure information.
 * @param loadingDelegate The delegate passed to overwrite control of showing loaders.
 * @param resolvePaymentForResult An `ActivityResultLauncher` used to launch the Coles Pay web activity and handle the result.
 * @param viewModel The [ColesPayViewModel] that manages Coles Pay-related data and result state.
 */
private fun handleUiState(
    context: Context,
    uiState: ColesPayUIState,
    viewModel: ColesPayViewModel,
    loadingDelegate: WidgetLoadingDelegate?,
    completion: (Result<String>) -> Unit,
    resolvePaymentForResult: ActivityResultLauncher<Intent>,
) {
    when (uiState) {
        // No action needed for the Idle state
        is ColesPayUIState.Idle -> Unit

        // Handle the loading state by notifying the loading delegate
        is ColesPayUIState.Loading -> {
            loadingDelegate?.widgetLoadingDidStart()
        }

        // Launch an intent to Coles Pay's Web Activity if the callback URL is available
        is ColesPayUIState.LaunchIntent -> {
            loadingDelegate?.widgetLoadingDidFinish()
            uiState.callbackData.callbackId?.let { colesPayOrderId ->
                val intent = Intent(context, ColesPayWebActivity::class.java)
                    .putOrderIdExtra(colesPayOrderId) // Adds the Coles Pay order ID to the intent.
                    .putClientIdExtra(viewModel.clientId) // Adds the client ID to the intent.
                resolvePaymentForResult.launch(intent) // Launches the Coles Pay web activity.
            }
        }

        // Handle success state, notify the loading delegate, and return orderId with success
        is ColesPayUIState.Success -> {
            loadingDelegate?.widgetLoadingDidFinish()
            completion(Result.success(uiState.orderId))
            // Reset the state to ensure it’s not reused
            viewModel.resetResultState()
        }

        // Handle error state, notify the loading delegate, and complete the transaction with failure
        is ColesPayUIState.Error -> {
            loadingDelegate?.widgetLoadingDidFinish()
            completion(Result.failure(uiState.exception))
            // Reset the state to ensure it’s not reused
            viewModel.resetResultState()
        }
    }
}

@PreviewLightDark
@Composable
internal fun PreviewColesPayWidget() {
    SdkTheme {
        ColesPayWidget(clientId = "", token = {}, completion = {})
    }
}
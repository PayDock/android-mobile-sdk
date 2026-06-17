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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.ColesPayException
import com.paydock.core.domain.model.Event
import com.paydock.core.domain.model.EventAction
import com.paydock.core.presentation.extensions.getMessageExtra
import com.paydock.core.presentation.extensions.getStatusExtra
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.core.presentation.util.WidgetEventDelegate
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.core.presentation.util.webviewLoadingAccessibility
import com.paydock.designsystems.components.button.ImageButtonAppearance
import com.paydock.designsystems.components.button.ImageButtonDefaults
import com.paydock.designsystems.components.button.SdkImageButton
import com.paydock.designsystems.components.loader.LoaderAppearance
import com.paydock.designsystems.components.loader.LoaderAppearanceDefaults
import com.paydock.designsystems.components.loader.SdkLoader
import com.paydock.feature.colespay.domain.model.ColesPayEventNames
import com.paydock.feature.colespay.integration.ColesPayWidgetConfig
import com.paydock.feature.colespay.presentation.state.ColesPayUIState
import com.paydock.feature.colespay.presentation.utils.CancellationStatus
import com.paydock.feature.colespay.presentation.utils.getCancellationStatusExtra
import com.paydock.feature.colespay.presentation.utils.getOrderIdExtra
import com.paydock.feature.colespay.presentation.utils.putClientIdExtra
import com.paydock.feature.colespay.presentation.utils.putOrderIdExtra
import com.paydock.feature.colespay.presentation.viewmodels.ColesPayViewModel
import com.paydock.feature.wallet.domain.model.integration.WalletTokenResult
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * A Composable for handling Coles Pay payments and related interactions.
 *
 * @param modifier Modifier for customizing the appearance and behavior of the Composable.
 * @param enabled Controls the enabled state of this Widget. When false,
 * this component will not respond to user input, and it will appear visually disabled.
 * @param config The configuration details required for the Coles Pay widget.
 * @param appearance The appearance configuration for the Coles Pay widget.
 * @param tokenRequest A callback to obtain the wallet token asynchronously.
 * @param loadingDelegate The delegate passed to overwrite control of showing loaders.
 * @param eventDelegate An optional [WidgetEventDelegate] for tracking widget events such as button clicks.
 * @param completion A callback to handle the result of the Coles Pay operation.
 */
@Suppress("MagicNumber")
@Composable
fun ColesPayWidget(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    config: ColesPayWidgetConfig,
    appearance: ColesPayWidgetAppearance = ColesPayWidgetAppearanceDefaults.appearance(),
    tokenRequest: (tokenResult: (Result<WalletTokenResult>) -> Unit) -> Unit,
    loadingDelegate: WidgetLoadingDelegate? = null,
    eventDelegate: WidgetEventDelegate? = null,
    completion: (Result<String>) -> Unit
) {
    val context = LocalContext.current
    // Obtain instances of view models
    val viewModel: ColesPayViewModel = koinViewModel(parameters = {
        parametersOf(config)
    })
    val focusManager = LocalFocusManager.current

    // Collect states for Coles Pay and Wallet view models
    val uiState by viewModel.uiState.collectAsState()

    // ActivityResultLauncher for handling payment resolution
    val resolvePaymentForResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        handleColesPayResult(result, completion, viewModel)
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
    val isLoading = loadingDelegate == null && uiState is ColesPayUIState.Loading
    if (!isLoading) {
        SdkImageButton(
            modifier = modifier,
            appearance = appearance.imageButton,
            enabled = uiState !is ColesPayUIState.Loading && enabled,
            painter = painterResource(id = R.drawable.pay_with_coles_pay_button),
            contentDescription = LocalContext.current.getString(R.string.content_desc_coles_pay_button),
        ) {
            focusManager.clearFocus()
            // Emit button event
            eventDelegate?.widgetEvent(
                Event.ButtonEvent(
                    name = ColesPayEventNames.COLES_PAY_CHECKOUT_BUTTON,
                    action = EventAction.CLICK
                )
            )
            viewModel.startColesPayFlow(tokenRequest)
        }
    } else {
        val painter = painterResource(id = R.drawable.pay_with_coles_placeholder)
        val imageAspectRatio: Float =
            painter.intrinsicSize.width / painter.intrinsicSize.height.coerceAtLeast(1f)
        Box(
            modifier = modifier
                .aspectRatio(imageAspectRatio)
                .clip(appearance.imageButton.shape)
                .webviewLoadingAccessibility(isLoading = isLoading)
                .semantics(mergeDescendants = false) {
                    // mergeDescendants = false ensures our contentDescription from webviewLoadingAccessibility
                    // is announced when tapping anywhere on the Box
                },
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
                SdkLoader(
                    modifier = Modifier
                        .size(loaderSizeInDp)
                        .align(Alignment.Center),
                    appearance = appearance.loader.copy(
                        strokeWidth = (loaderSizeInDp.value * 0.1f).coerceAtLeast(2f).dp
                    )
                )
            }
        }
    }
}

/**
 * Defines the appearance of the Coles Pay widget.
 *
 * @property imageButton The appearance of the Coles Pay image button.
 * @property loader The appearance of the loader displayed during processing.
 */
@Immutable
class ColesPayWidgetAppearance(
    val imageButton: ImageButtonAppearance,
    val loader: LoaderAppearance
) {
    fun copy(
        imageButton: ImageButtonAppearance = this.imageButton,
        loader: LoaderAppearance = this.loader
    ): ColesPayWidgetAppearance =
        ColesPayWidgetAppearance(
            imageButton = imageButton.copy(),
            loader = loader.copy()
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ColesPayWidgetAppearance

        if (imageButton != other.imageButton) return false
        if (loader != other.loader) return false

        return true
    }

    override fun hashCode(): Int {
        var result = imageButton.hashCode()
        result = 31 * result + loader.hashCode()
        return result
    }
}

/**
 * Provides default appearance settings for the [ColesPayWidget].
 *
 * This object defines the standard visual presentation for the Coles Pay widget,
 * including its button and loader components. These defaults can be overridden
 * by providing a custom [ColesPayWidgetAppearance] instance to the widget.
 */
object ColesPayWidgetAppearanceDefaults {
    /**
     * Composable function that returns the default appearance settings for the ColesPayWidget.
     * This function is intended to be used within a Composable context.
     *
     * @return [ColesPayWidgetAppearance] The default appearance settings for the ColesPayWidget.
     *  - The `imageButton` is configured with a pill shape (50% rounded corners) and a white ripple color.
     *  - The `loaderAppearance` is configured with a white color.
     */
    @Composable
    fun appearance(): ColesPayWidgetAppearance = ColesPayWidgetAppearance(
        imageButton = ImageButtonDefaults.appearance().copy(
            shape = RoundedCornerShape(percent = 50), // ensures pill shape
            rippleColor = Color.White,
        ),
        loader = LoaderAppearanceDefaults.appearance().copy(
            color = Color.White
        )
    )
}

/**
 * Handles the result returned from the Coles Pay web activity, processing the order ID,
 * cancellation, or errors, and invoking the appropriate completion handler.
 *
 * @param result The `ActivityResult` returned from the Coles Pay web activity.
 * This contains the result code and data such as the order ID or cancellation status.
 * @param completion A callback function to handle the result of the Coles Pay transaction.
 * It is invoked with a `Result` object containing either success (with the order ID) or failure information.
 * @param viewModel The [ColesPayViewModel] that processes Coles Pay-related data and manages the result state.
 */
private fun handleColesPayResult(
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
                    CancellationStatus.USER_INITIATED, CancellationStatus.PAGE_CLOSED -> {
                        completion(
                            Result.failure(
                                ColesPayException.CancellationException(
                                    displayableMessage = MobileSDKConstants.ColesPayConfig.Errors.CANCELLATION_ERROR
                                )
                            )
                        )
                        viewModel.resetResultState()
                    }

                    // If the cancellation was due to another reason, process the WebView error status and message.
                    else -> {
                        val status = data.getStatusExtra()
                        val message =
                            data.getMessageExtra(MobileSDKConstants.ColesPayConfig.Errors.COLES_PAY_ERROR)
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
                    .putClientIdExtra(viewModel.config.clientId) // Adds the client ID to the intent.
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

@SdkLightDarkPreviews
@Composable
internal fun PreviewColesPayWidget() {
    ColesPayWidget(config = ColesPayWidgetConfig(clientId = ""), tokenRequest = {}, completion = {})
}
package com.paydock.feature.zip.presentation

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.model.Event
import com.paydock.core.domain.model.EventAction
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.core.presentation.util.WidgetEventDelegate
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.components.button.ButtonAppearanceDefaults
import com.paydock.designsystems.components.loader.LoaderAppearance
import com.paydock.designsystems.components.loader.LoaderAppearanceDefaults
import com.paydock.designsystems.components.loader.SdkLoader
import com.paydock.feature.zip.domain.model.ZipCallbackData
import com.paydock.feature.zip.domain.model.ZipEventNames
import com.paydock.feature.zip.domain.model.ZipResult
import com.paydock.feature.zip.domain.model.ZipStatus
import com.paydock.feature.zip.domain.model.integration.ZipWidgetConfig
import com.paydock.feature.zip.presentation.state.ZipUIState
import com.paydock.feature.zip.presentation.utils.ZipButtonStyle
import com.paydock.feature.zip.presentation.utils.getErrorCodeExtra
import com.paydock.feature.zip.presentation.utils.getErrorMessageExtra
import com.paydock.feature.zip.presentation.utils.getResultCheckoutIdExtra
import com.paydock.feature.zip.presentation.utils.getResultOrderIdExtra
import com.paydock.feature.zip.presentation.utils.getResultStatusExtra
import com.paydock.feature.zip.presentation.utils.putCheckoutTokenExtra
import com.paydock.feature.zip.presentation.utils.putCheckoutUrlExtra
import com.paydock.feature.zip.presentation.viewmodels.ZipViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.math.BigDecimal

/**
 * A Composable for handling Zip payments.
 *
 * This widget displays a Zip payment button that, when clicked, initiates the Zip checkout flow.
 * The checkout is handled in a separate activity containing a WebView.
 *
 * @param modifier Modifier for customizing the appearance and behavior of the widget.
 * @param enabled Controls the enabled state of this widget. When false,
 *                the widget will not respond to user input and will appear visually disabled.
 * @param config The configuration details required for the Zip widget.
 * @param appearance The appearance configuration for the Zip widget.
 * @param loadingDelegate Optional delegate for controlling loader display.
 * @param eventDelegate Optional delegate for tracking widget events such as button clicks.
 * @param completion A callback to handle the result of the Zip payment operation.
 */
@Composable
fun ZipWidget(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    config: ZipWidgetConfig,
    appearance: ZipWidgetAppearance = ZipWidgetAppearanceDefaults.appearance(),
    loadingDelegate: WidgetLoadingDelegate? = null,
    eventDelegate: WidgetEventDelegate? = null,
    completion: (Result<ZipResult>) -> Unit
) {
    val context = LocalContext.current
    val viewModel: ZipViewModel = koinViewModel(parameters = { parametersOf(config) })

    val uiState by viewModel.stateFlow.collectAsState()

    // ActivityResultLauncher for handling the checkout result
    val resolvePaymentForResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleZipResult(result, viewModel)
    }

    // Handle UI state changes
    LaunchedEffect(uiState) {
        handleUiState(
            context = context,
            uiState = uiState,
            viewModel = viewModel,
            loadingDelegate = loadingDelegate,
            resolvePaymentForResult = resolvePaymentForResult,
            completion = completion
        )
    }

    // Determine loading state
    val isLoading = loadingDelegate == null && uiState is ZipUIState.Loading
    val isEnabled =
        enabled && uiState !is ZipUIState.Loading && uiState !is ZipUIState.LaunchCheckout

    // Render button or loading state
    if (isLoading) {
        // Show loading placeholder
        ZipLoadingButton(
            modifier = modifier,
            appearance = appearance
        )
    } else {
        // Show interactive button
        ZipPaymentButton(
            modifier = modifier,
            enabled = isEnabled,
            appearance = appearance,
            onClick = {
                // Emit button event
                eventDelegate?.widgetEvent(
                    Event.ButtonEvent(
                        name = ZipEventNames.ZIP_CHECKOUT_BUTTON,
                        action = EventAction.CLICK
                    )
                )
                viewModel.initializeCheckout()
            }
        )
    }
}

/**
 * Handles the result from the Zip checkout activity.
 */
private fun handleZipResult(
    result: ActivityResult,
    viewModel: ZipViewModel
) {
    val data = result.data

    when (result.resultCode) {
        AppCompatActivity.RESULT_OK -> {
            // Success - parse callback data and handle
            val status = data?.getResultStatusExtra()?.let { ZipStatus.fromValue(it) }
            val checkoutId = data?.getResultCheckoutIdExtra()
            val orderId = data?.getResultOrderIdExtra()

            val callbackData = ZipCallbackData(
                status = status ?: ZipStatus.APPROVED,
                checkoutId = checkoutId,
                orderId = orderId
            )
            viewModel.handleZipCallback(callbackData)
        }

        AppCompatActivity.RESULT_CANCELED -> {
            // Failure or cancellation
            val status = data?.getResultStatusExtra()?.let { ZipStatus.fromValue(it) }
            val checkoutId = data?.getResultCheckoutIdExtra()
            val orderId = data?.getResultOrderIdExtra()
            val errorCode = data?.getErrorCodeExtra()
            val errorMessage = data?.getErrorMessageExtra()

            if (errorMessage != null) {
                // WebView error
                viewModel.handleWebViewError(errorCode, errorMessage)
            } else {
                // Zip callback (cancelled, declined, etc.)
                val callbackData = ZipCallbackData(
                    status = status ?: ZipStatus.CANCELLED,
                    checkoutId = checkoutId,
                    orderId = orderId
                )
                viewModel.handleZipCallback(callbackData)
            }
        }
    }
}

/**
 * The Zip payment button composable.
 */
@Composable
private fun ZipPaymentButton(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    appearance: ZipWidgetAppearance,
    onClick: () -> Unit
) {
    val zipButtonContentDescription = stringResource(R.string.content_desc_zip_button)

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonAppearanceDefaults.ButtonHeight)
            .semantics { contentDescription = zipButtonContentDescription },
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = appearance.buttonStyle.backgroundColor,
            disabledContainerColor = appearance.buttonStyle.backgroundColor.copy(alpha = 0.8f)
        ),
        border = if (appearance.buttonStyle.borderWidth > 0.dp) {
            BorderStroke(appearance.buttonStyle.borderWidth, appearance.buttonStyle.borderColor)
        } else {
            null
        }
    ) {
        Image(
            painter = painterResource(id = appearance.buttonStyle.imageResId),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxHeight(0.8f),
            alpha = if (enabled) 1f else 0.8f
        )
    }
}

/**
 * The Zip button in loading state.
 */
@Composable
private fun ZipLoadingButton(
    modifier: Modifier = Modifier,
    appearance: ZipWidgetAppearance
) {
    val zipLoadingContentDescription = stringResource(R.string.content_desc_zip_loading)

    Button(
        onClick = { },
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonAppearanceDefaults.ButtonHeight)
            .semantics { contentDescription = zipLoadingContentDescription },
        enabled = false,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = appearance.buttonStyle.backgroundColor.copy(alpha = 0.8f)
        ),
        border = if (appearance.buttonStyle.borderWidth > 0.dp) {
            BorderStroke(appearance.buttonStyle.borderWidth, appearance.buttonStyle.borderColor)
        } else {
            null
        }
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            SdkLoader(
                modifier = Modifier.size(24.dp),
                appearance = appearance.loader
            )
        }
    }
}

/**
 * Validates if a URL is valid for launching in a WebView.
 *
 * @param url The URL to validate.
 * @return `true` if the URL is valid (not null, not empty, and starts with http:// or https://), otherwise `false`.
 */
private fun isValidWebViewUrl(url: String?): Boolean {
    if (url.isNullOrBlank()) {
        return false
    }
    val trimmedUrl = url.trim()
    return trimmedUrl.startsWith("http://", ignoreCase = true) ||
        trimmedUrl.startsWith("https://", ignoreCase = true)
}

/**
 * Handles the current UI state of the Zip payment process.
 */
private fun handleUiState(
    context: Context,
    uiState: ZipUIState,
    viewModel: ZipViewModel,
    loadingDelegate: WidgetLoadingDelegate?,
    resolvePaymentForResult: ActivityResultLauncher<Intent>,
    completion: (Result<ZipResult>) -> Unit
) {
    when (uiState) {
        is ZipUIState.Idle -> Unit

        is ZipUIState.Loading -> {
            loadingDelegate?.widgetLoadingDidStart()
        }

        is ZipUIState.LaunchCheckout -> {
            loadingDelegate?.widgetLoadingDidFinish()

            // Validate URL before launching WebView; report error via completion for merchant to handle
            if (!isValidWebViewUrl(uiState.checkoutUrl)) {
                viewModel.handleWebViewError(null, MobileSDKConstants.ZipConfig.Errors.INVALID_URL)
                return
            }

            // Launch the Zip checkout activity
            val intent = Intent(context, ZipWebActivity::class.java)
                .putCheckoutUrlExtra(uiState.checkoutUrl)
                .putCheckoutTokenExtra(uiState.checkoutToken)
            resolvePaymentForResult.launch(intent)
        }

        is ZipUIState.Success -> {
            loadingDelegate?.widgetLoadingDidFinish()
            completion(Result.success(ZipResult(token = uiState.token)))
            viewModel.resetResultState()
        }

        is ZipUIState.Error -> {
            loadingDelegate?.widgetLoadingDidFinish()
            completion(Result.failure(uiState.exception))
            viewModel.resetResultState()
        }
    }
}

/**
 * Represents the appearance settings for the Zip widget.
 *
 * @property buttonStyle The button style following Zip's brand guidelines.
 * @property loader The loader appearance for the loading state.
 */
@Immutable
class ZipWidgetAppearance(
    val buttonStyle: ZipButtonStyle,
    val loader: LoaderAppearance
) {
    fun copy(
        buttonStyle: ZipButtonStyle = this.buttonStyle,
        loader: LoaderAppearance = this.loader
    ): ZipWidgetAppearance = ZipWidgetAppearance(
        buttonStyle = buttonStyle,
        loader = loader.copy()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ZipWidgetAppearance

        if (buttonStyle != other.buttonStyle) return false
        if (loader != other.loader) return false

        return true
    }

    override fun hashCode(): Int {
        var result = buttonStyle.hashCode()
        result = 31 * result + loader.hashCode()
        return result
    }
}

/**
 * Provides default appearance settings for the [ZipWidget].
 */
object ZipWidgetAppearanceDefaults {

    /**
     * Returns the default appearance settings for the ZipWidget.
     *
     * @return [ZipWidgetAppearance] The default appearance settings.
     */
    @Composable
    fun appearance(): ZipWidgetAppearance = ZipWidgetAppearance(
        buttonStyle = ZipButtonStyle.WHITE_ON_BLACK,
        loader = LoaderAppearanceDefaults.appearance()
            .copy(color = ZipButtonStyle.WHITE_ON_BLACK.loaderColor)
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewZipWidget() {
    ZipWidget(
        config = ZipWidgetConfig(
            accessToken = "",
            gatewayId = "",
            amount = BigDecimal("100.00"),
            currency = "AUD",
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com"
        ),
        completion = {}
    )
}
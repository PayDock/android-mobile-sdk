package com.paydock.feature.afterpay.presentation

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.afterpay.android.Afterpay
import com.afterpay.android.view.AfterpayColorScheme
import com.afterpay.android.view.AfterpayPaymentButton
import com.paydock.R
import com.paydock.core.domain.error.exceptions.AfterpayException
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.components.loader.LoaderAppearance
import com.paydock.designsystems.components.loader.LoaderAppearanceDefaults
import com.paydock.designsystems.components.loader.SdkLoader
import com.paydock.feature.address.domain.model.integration.BillingAddress
import com.paydock.feature.afterpay.domain.mapper.integration.mapFromBillingAddress
import com.paydock.feature.afterpay.domain.mapper.integration.mapFromShippingOption
import com.paydock.feature.afterpay.domain.model.integration.AfterpaySDKConfig
import com.paydock.feature.afterpay.domain.model.integration.AfterpayShippingOption
import com.paydock.feature.afterpay.domain.model.integration.AfterpayShippingOptionUpdate
import com.paydock.feature.afterpay.presentation.components.AfterpayPaymentButtonView
import com.paydock.feature.afterpay.presentation.state.AfterpayUIState
import com.paydock.feature.afterpay.presentation.utils.CheckoutHandler
import com.paydock.feature.afterpay.presentation.viewmodels.AfterpayViewModel
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import org.koin.androidx.compose.koinViewModel

/**
 * A Composable that provides the Afterpay widget for integrating Afterpay's checkout functionality.
 *
 * This widget handles the complete lifecycle of Afterpay's payment process, including configuration,
 * token handling, address selection, shipping option updates, and final result handling.
 *
 * @param modifier The `Modifier` to be applied to the widget.
 * @param config The configuration object for the Afterpay SDK, including checkout and button options.
 * @param enabled A boolean to enable or disable the payment button.
 * @param token A lambda function to obtain a token asynchronously. Accepts a callback to provide the obtained token.
 * @param selectAddress A lambda for selecting a billing address and providing corresponding shipping options.
 * @param selectShippingOption A lambda for selecting a shipping option and providing the updated shipping option result.
 * @param loadingDelegate An optional delegate for managing the widget's loading state.
 * @param completion A callback to handle the final result of the payment process, either success or failure.
 */
@Composable
fun AfterpayWidget(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    config: AfterpaySDKConfig,
    appearance: AfterpayWidgetAppearance = AfterpayAppearanceDefaults.appearance(),
    token: (onTokenReceived: (String) -> Unit) -> Unit,
    selectAddress: (
        address: BillingAddress,
        provideShippingOptions: (List<AfterpayShippingOption>) -> Unit
    ) -> Unit = { _, _ -> },
    selectShippingOption: (
        shippingOption: AfterpayShippingOption,
        provideShippingOptionUpdateResult: (AfterpayShippingOptionUpdate?) -> Unit,
    ) -> Unit = { _, _ -> },
    loadingDelegate: WidgetLoadingDelegate? = null,
    completion: (Result<ChargeResponse>) -> Unit,
) {
    val viewModel: AfterpayViewModel = koinViewModel()
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isConfigured by viewModel.isConfigured.collectAsState()

    // ActivityResultLauncher for handling the checkout result
    val resolvePaymentForResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleAfterpayResult(context, result, viewModel, completion)
    }

    // Configure the Afterpay SDK and set up the checkout handler
    viewModel.configureAfterpaySdk(config.config)
    val checkoutHandler = remember {
        CheckoutHandler(
            onDidCommenceCheckout = { viewModel.loadCheckoutToken() },
            onShippingAddressDidChange = { address ->
                selectAddress(address.mapFromBillingAddress()) { shippingOptions ->
                    viewModel.provideShippingOptions(shippingOptions)
                }
            },
            onShippingOptionDidChange = { shippingOption ->
                selectShippingOption(shippingOption.mapFromShippingOption()) { shippingOptionUpdate ->
                    viewModel.provideShippingOptionUpdate(shippingOptionUpdate)
                }
            }
        )
    }
    Afterpay.setCheckoutV2Handler(checkoutHandler)

    // Observe and handle UI state changes
    LaunchedEffect(uiState) {
        handleUIState(uiState, viewModel, loadingDelegate, checkoutHandler, completion)
    }

    // Render the Afterpay widget UI
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isConfigured) {
            AfterpayPaymentButtonView(
                config = config,
                enabled = enabled,
                buttonText = appearance.buttonText,
                colorScheme = appearance.colorScheme,
                onObtainToken = token,
                viewModel = viewModel,
                resolvePaymentForResult = resolvePaymentForResult
            )
        }
        if (uiState is AfterpayUIState.Loading && loadingDelegate == null) {
            SdkLoader(appearance = appearance.loader)
        }
    }
}

/**
 * Represents the appearance configuration for the Afterpay widget.
 *
 * This class defines the visual properties of the Afterpay payment button and the loader
 * displayed within the widget. It allows for customization of the button's text,
 * color scheme, and the appearance of the loading indicator.
 *
 * @property buttonText The text to display on the Afterpay payment button.
 * @property colorScheme The color scheme to apply to the Afterpay payment button.
 * @property loader The appearance configuration for the loader shown during processing.
 */
@Immutable
class AfterpayWidgetAppearance(
    val buttonText: AfterpayPaymentButton.ButtonText,
    val colorScheme: AfterpayColorScheme,
    val loader: LoaderAppearance
) {
    /**
     * Creates a copy of this [AfterpayWidgetAppearance] with optional overridden values.
     *
     * This function allows you to create a new instance of [AfterpayWidgetAppearance] by
     * copying the properties of the current instance, while selectively providing new values
     * for [buttonText], [colorScheme], or [loader]. This is useful for creating variations
     * of an existing appearance configuration without modifying the original.
     *
     * @param buttonText The new button text to use for the copied appearance. Defaults to the current instance's value.
     * @param colorScheme The new color scheme to use for the copied appearance. Defaults to the current instance's value.
     * @param loader The new loader appearance to use for the copied appearance. Defaults to a copy of the current instance's loader.
     * @return A new [AfterpayWidgetAppearance] instance with the specified values.
     */
    fun copy(
        buttonText: AfterpayPaymentButton.ButtonText = this.buttonText,
        colorScheme: AfterpayColorScheme = this.colorScheme,
        loader: LoaderAppearance = this.loader
    ): AfterpayWidgetAppearance = AfterpayWidgetAppearance(
        buttonText = buttonText,
        colorScheme = colorScheme,
        loader = loader.copy()
    )
}

/**
 * Defines default appearances for the Afterpay widget.
 *
 * This object provides a default [AfterpayWidgetAppearance] using standard configurations for
 * the button text, color scheme, and loader. This allows for a quick and easy way to
 * apply a default look and feel to the Afterpay widget when no custom appearance is specified.
 */
object AfterpayAppearanceDefaults {
    /**
     * Provides a default [AfterpayWidgetAppearance] with predefined values.
     *
     * This function is a Composable that returns a default appearance configuration for the
     * Afterpay widget. It sets the default button text to [AfterpayPaymentButton.ButtonText.DEFAULT],
     * the color scheme to [AfterpayColorScheme.BLACK_ON_MINT], and uses the default
     * appearance for the loader via [LoaderAppearanceDefaults.appearance].
     *
     * @return The default [AfterpayWidgetAppearance] instance.
     */
    @Composable
    fun appearance(): AfterpayWidgetAppearance = AfterpayWidgetAppearance(
        buttonText = AfterpayPaymentButton.ButtonText.DEFAULT,
        colorScheme = AfterpayColorScheme.BLACK_ON_MINT,
        loader = LoaderAppearanceDefaults.appearance()
    )
}

/**
 * Handles the result of the Afterpay checkout process, determining the next action
 * based on the result code and data.
 *
 * @param context The context of the application.
 * @param result The result from the checkout activity.
 * @param viewModel The `AfterpayViewModel` managing the widget's state.
 * @param completion A callback to handle the final result of the payment process.
 */
private fun handleAfterpayResult(
    context: Context,
    result: ActivityResult,
    viewModel: AfterpayViewModel,
    completion: (Result<ChargeResponse>) -> Unit,
) {
    try {
        when (result.resultCode) {
            AppCompatActivity.RESULT_OK -> viewModel.captureWalletTransaction()
            AppCompatActivity.RESULT_CANCELED -> handleCancellation(result.data, context, viewModel, completion)
            else -> Unit
        }
    } catch (e: IllegalStateException) {
        handleInvalidError(e.message ?: "", viewModel, completion)
    }
}

/**
 * Processes the cancellation result of the Afterpay checkout process.
 *
 * This function is responsible for handling the cancellation response from the Afterpay SDK.
 * It checks if the intent contains valid cancellation data and updates the ViewModel accordingly.
 * If the cancellation data is invalid or missing, it triggers an error completion and resets the result state.
 *
 * @param data The intent containing the cancellation data, or `null` if no data is provided.
 * @param context The context used to access application resources for error messages.
 * @param viewModel The ViewModel responsible for handling the Afterpay checkout state.
 * @param completion A callback function that returns the result of the Afterpay operation.
 *                   It is invoked with a failure result if the cancellation data is invalid.
 */
private fun handleCancellation(
    data: Intent?,
    context: Context,
    viewModel: AfterpayViewModel,
    completion: (Result<ChargeResponse>) -> Unit
) {
    data?.let { intent ->
        Afterpay.parseCheckoutCancellationResponse(intent)?.let { status ->
            viewModel.updateCancellationState(status)
        } ?: run {
            handleInvalidError(context.getString(R.string.error_afterpay_sdk_internal_status), viewModel, completion)
        }
    } ?: run {
        handleInvalidError(context.getString(R.string.error_afterpay_sdk_internal_data), viewModel, completion)
    }
}

/**
 * Handles errors related to invalid cancellation responses or missing data.
 *
 * This function is invoked when an invalid cancellation result or missing data is encountered.
 * It creates an `InvalidResultException` with a specified error message and passes it back via the
 * completion callback. Additionally, it resets the state of the ViewModel to ensure that any
 * invalid or unexpected state is cleared.
 *
 * @param message The error message that will be included in the failure result.
 * @param viewModel The ViewModel responsible for handling the Afterpay checkout state.
 * @param completion A callback function that returns the failure result with the provided error message.
 */
private fun handleInvalidError(
    message: String,
    viewModel: AfterpayViewModel,
    completion: (Result<ChargeResponse>) -> Unit
) {
    completion(Result.failure(AfterpayException.InvalidResultException(message)))
    viewModel.resetResultState()
}

/**
 * Processes the current UI state of the widget and performs the necessary actions
 * such as showing loading states, completing the transaction, or handling errors.
 *
 * @param uiState The current UI state of the widget.
 * @param viewModel The `AfterpayViewModel` managing the widget's state.
 * @param loadingDelegate An optional delegate for managing loading state transitions.
 * @param checkoutHandler The `CheckoutHandler` for managing Afterpay SDK interactions.
 * @param completion A callback to handle the final result of the payment process.
 */
private fun handleUIState(
    uiState: AfterpayUIState,
    viewModel: AfterpayViewModel,
    loadingDelegate: WidgetLoadingDelegate?,
    checkoutHandler: CheckoutHandler,
    completion: (Result<ChargeResponse>) -> Unit,
) {
    when (uiState) {
        is AfterpayUIState.Idle -> Unit
        is AfterpayUIState.Loading -> loadingDelegate?.widgetLoadingDidStart()
        is AfterpayUIState.Success -> {
            loadingDelegate?.widgetLoadingDidFinish()
            completion(Result.success(uiState.chargeData))
            viewModel.resetResultState()
        }
        is AfterpayUIState.Error -> {
            loadingDelegate?.widgetLoadingDidFinish()
            completion(Result.failure(uiState.exception))
            // If we do receive an error, we should cancel the charge
            viewModel.declineWalletTransaction()
            viewModel.resetResultState()
        }
        is AfterpayUIState.ProvideCheckoutTokenResult -> checkoutHandler.provideTokenResult(
            uiState.tokenResult
        )
        is AfterpayUIState.ProvideShippingOptionUpdateResult -> checkoutHandler.provideShippingOptionUpdateResult(
            uiState.shippingOptionUpdateResult
        )
        is AfterpayUIState.ProvideShippingOptionsResult -> checkoutHandler.provideShippingOptionsResult(
            uiState.shippingOptionsResult
        )
    }
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewAfterpayWidget() {
    AndroidView(factory = { context ->
        AfterpayPaymentButton(context).apply {
            this.buttonText = buttonText
            this.colorScheme = colorScheme
            setOnClickListener {}
        }
    })
}
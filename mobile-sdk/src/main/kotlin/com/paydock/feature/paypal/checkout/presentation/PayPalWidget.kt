package com.paydock.feature.paypal.checkout.presentation

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.PayPalException
import com.paydock.core.presentation.extensions.getMessageExtra
import com.paydock.core.presentation.extensions.getStatusExtra
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.components.button.ButtonAppearanceDefaults
import com.paydock.designsystems.components.loader.LoaderAppearance
import com.paydock.designsystems.components.loader.LoaderAppearanceDefaults
import com.paydock.designsystems.components.loader.SdkLoader
import com.paydock.feature.paypal.checkout.domain.model.integration.PayPalWidgetConfig
import com.paydock.feature.paypal.checkout.presentation.state.PayPalCheckoutUIState
import com.paydock.feature.paypal.checkout.presentation.utils.CancellationStatus
import com.paydock.feature.paypal.checkout.presentation.utils.getCancellationStatusExtra
import com.paydock.feature.paypal.checkout.presentation.utils.getPayerIdExtra
import com.paydock.feature.paypal.checkout.presentation.utils.getPaymentMethodIdExtra
import com.paydock.feature.paypal.checkout.presentation.utils.putClientIdExtra
import com.paydock.feature.paypal.checkout.presentation.utils.putFundingSourceExtra
import com.paydock.feature.paypal.checkout.presentation.utils.putOrderIdExtra
import com.paydock.feature.paypal.checkout.presentation.viewmodels.PayPalViewModel
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.model.integration.WalletTokenResult
import com.paypal.android.paymentbuttons.PayPalButton
import com.paypal.android.paymentbuttons.PayPalButtonColor
import com.paypal.android.paymentbuttons.PayPalButtonLabel
import com.paypal.android.paymentbuttons.PaymentButtonShape
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * A Composable that renders the PayPal checkout button and orchestrates the PayPal payment flow.
 *
 * The button is provided by the PayPal Android SDK [PayPalButton]
 * and is hosted via `AndroidView`. All business logic (tokenization, URL parsing, capture/decline,
 * and error handling) remains in the existing `PayPalViewModel`.
 *
 * Visuals can be customized through [appearance], which maps directly to PayPal SDK styling:
 * - [PayPalWidgetAppearance.buttonColour] to control the button color
 * - [PayPalWidgetAppearance.buttonLabel] to control the button label/wordmark treatment
 * - [PayPalWidgetAppearance.buttonShape] to control the button shape (e.g. rounded, pill)
 * - [PayPalWidgetAppearance.loader] to control the overlay loader shown during `Loading` when
 *   [loadingDelegate] is not provided
 *
 * While processing (`Loading`) or when launching the browser intent (`LaunchIntent`), the button is
 * automatically disabled. When [loadingDelegate] is null, an overlay loader is shown; otherwise the
 * caller owns loader presentation via the delegate callbacks.
 *
 * @param modifier Modifier for customizing the appearance and behavior of the Composable.
 * @param enabled When false, disables the PayPal button and blocks user interaction.
 * @param config Widget configuration for initiating the PayPal flow.
 * @param appearance Appearance configuration mapping to PayPal SDK button styling and loader.
 * @param tokenRequest A callback to asynchronously provide a wallet token to the flow.
 * @param loadingDelegate Optional delegate to externally control loading lifecycle.
 * @param completion Callback invoked with the final [ChargeResponse] or an error.
 */
@Composable
fun PayPalWidget(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    config: PayPalWidgetConfig,
    appearance: PayPalWidgetAppearance = PayPalAppearanceDefaults.appearance(),
    tokenRequest: (tokenResult: (Result<WalletTokenResult>) -> Unit) -> Unit,
    loadingDelegate: WidgetLoadingDelegate? = null,
    completion: (Result<ChargeResponse>) -> Unit,
) {
    val context = LocalContext.current
    // Obtain instances of view models
    val viewModel: PayPalViewModel = koinViewModel(parameters = { parametersOf(config) })

    // Collect states for PayPal view models
    val uiState by viewModel.uiState.collectAsState()

    // ActivityResultLauncher for handling payment resolution
    val resolvePaymentForResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        handlePayPalResult(result, viewModel, completion)
    }

    LaunchedEffect(uiState) {
        handleUIState(
            context,
            uiState,
            viewModel,
            loadingDelegate,
            resolvePaymentForResult,
            completion
        )
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(ButtonAppearanceDefaults.ButtonHeight),
            factory = { context ->
                PayPalButton(context).apply {
                    color = appearance.buttonColour
                    label = appearance.buttonLabel
                    shape = appearance.buttonShape
                    isEnabled = enabled
                }
            },
            update = { view ->
                // Compute current enabled state (external flag + internal UI state)
                val isButtonEnabled =
                    enabled && uiState !is PayPalCheckoutUIState.Loading && uiState !is PayPalCheckoutUIState.LaunchIntent

                // Apply enable/disable and interactivity flags
                view.isEnabled = isButtonEnabled
                view.isClickable = isButtonEnabled
                view.isFocusable = isButtonEnabled
                view.isLongClickable = isButtonEnabled

                // Apply style updates
                view.color = appearance.buttonColour
                view.label = appearance.buttonLabel
                view.shape = appearance.buttonShape
                view.requestLayout()

                // Attach/detach click listener based on the current enabled state
                if (isButtonEnabled) {
                    view.setOnClickListener {
                        viewModel.handlePayPalButtonClick(config, tokenRequest)
                    }
                } else {
                    view.setOnClickListener(null)
                }
            }
        )
        if (uiState is PayPalCheckoutUIState.Loading && loadingDelegate == null) {
            SdkLoader(appearance = appearance.loader)
        }
    }
}

/**
 * Defines the appearance configuration for the PayPal Widget.
 *
 * This appearance maps directly to PayPal SDK button properties and the SDK’s loader used by this
 * widget when no [WidgetLoadingDelegate] is supplied.
 *
 * @property loader Appearance for the overlay loader shown while `Loading` when no delegate is used.
 * @property buttonColour The PayPal button color (e.g., [PayPalButtonColor.GOLD], [PayPalButtonColor.BLUE], [PayPalButtonColor.WHITE], [PayPalButtonColor.BLACK], [PayPalButtonColor.SILVER]).
 * @property buttonLabel The PayPal button label style (e.g., [PayPalButtonLabel.PAYPAL], [PayPalButtonLabel.CHECKOUT], [PayPalButtonLabel.BUY_NOW], [PayPalButtonLabel.PAY]).
 * @property buttonShape The PayPal button shape (e.g., [PaymentButtonShape.ROUNDED], [PaymentButtonShape.PILL]).
 */
@Immutable
class PayPalWidgetAppearance(
    val loader: LoaderAppearance,
    val buttonColour: PayPalButtonColor,
    val buttonLabel: PayPalButtonLabel,
    val buttonShape: PaymentButtonShape,
) {
    /**
     * Creates a copy of the current [PayPalWidgetAppearance], allowing for modification of
     * specific properties while retaining others.
     *
     * @param loader The new [LoaderAppearance] to use for the copied appearance. Defaults to the
     * current [loader] if not provided. The provided [LoaderAppearance] will also be copied to
     * ensure immutability.
     * @return A new [PayPalWidgetAppearance] instance with the specified modifications.
     */
    fun copy(
        loader: LoaderAppearance = this.loader,
        paypalColor: PayPalButtonColor = this.buttonColour,
        paypalLabel: PayPalButtonLabel = this.buttonLabel,
        buttonShape: PaymentButtonShape = this.buttonShape,
    ): PayPalWidgetAppearance =
        PayPalWidgetAppearance(
            loader = loader.copy(),
            buttonColour = paypalColor,
            buttonLabel = paypalLabel,
            buttonShape = buttonShape,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PayPalWidgetAppearance

        if (loader != other.loader) return false
        if (buttonColour != other.buttonColour) return false
        if (buttonLabel != other.buttonLabel) return false
        if (buttonShape != other.buttonShape) return false
        return true
    }

    override fun hashCode(): Int {
        var result = loader.hashCode()
        result = 31 * result + buttonColour.hashCode()
        result = 31 * result + buttonLabel.hashCode()
        result = 31 * result + buttonShape.hashCode()
        return result
    }
}

/**
 * Default appearance values for the PayPal Widget.
 *
 * Provides a default [PayPalWidgetAppearance] configured as follows:
 * - Loader: default loader with `ButtonLoaderWidth` and black stroke color
 * - Button color: [PayPalButtonColor.GOLD]
 * - Button label: [PayPalButtonLabel.PAYPAL] (wordmark-only)
 * - Button shape: [PaymentButtonShape.ROUNDED]
 */
object PayPalAppearanceDefaults {

    /**
     * Returns the default appearance for the PayPal Widget.
     * This composable function provides a pre-configured [PayPalWidgetAppearance],
     * specifically designed for the loader component displayed within the widget.
     * The loader is configured with a default stroke width and a black color.
     *
     * @return The default [PayPalWidgetAppearance] with pre-set loader configuration.
     */
    @Composable
    fun appearance(): PayPalWidgetAppearance = PayPalWidgetAppearance(
        loader = LoaderAppearanceDefaults.appearance()
            .copy(strokeWidth = ButtonAppearanceDefaults.ButtonLoaderWidth, color = Color.Black),
        buttonColour = PayPalButtonColor.GOLD,
        buttonLabel = PayPalButtonLabel.PAYPAL,
        buttonShape = PaymentButtonShape.ROUNDED,
    )

}

/**
 * Handles results from the PayPal web approval activity.
 *
 * - RESULT_OK → extracts and forwards the decoded approval URL to the [viewModel]
 * - RESULT_CANCELED → maps user cancellation or web errors into SDK exceptions and completes
 * - otherwise → ignored
 *
 * @param result Activity result from `PayPalWebActivity`.
 * @param viewModel ViewModel coordinating URL parsing, capture/decline and state resets.
 * @param completion Invoked with success or failure of the PayPal charge.
 */
private fun handlePayPalResult(
    result: ActivityResult,
    viewModel: PayPalViewModel,
    completion: (Result<ChargeResponse>) -> Unit,
) {
    result.data?.let { data ->
        when (result.resultCode) {
            // Success: directly proceed to capture using paymentMethodId and payerId
            AppCompatActivity.RESULT_OK -> {
                val paymentMethodId = data.getPaymentMethodIdExtra()
                val payerId = data.getPayerIdExtra()
                // On success, data should be present; call into VM to proceed
                if (paymentMethodId != null && payerId != null) {
                    viewModel.captureWalletTransaction(paymentMethodId, payerId)
                }
            }

            // Cancellation handling (user or error)
            AppCompatActivity.RESULT_CANCELED -> {
                when (data.getCancellationStatusExtra()) {
                    CancellationStatus.USER_INITIATED -> {
                        completion(
                            Result.failure(
                                PayPalException.CancellationException(
                                    displayableMessage = MobileSDKConstants.PayPalConfig.Errors.CANCELLATION_ERROR
                                )
                            )
                        )
                        viewModel.resetResultState()
                    }

                    else -> {
                        val status = data.getStatusExtra()
                        val message =
                            data.getMessageExtra(MobileSDKConstants.PayPalConfig.Errors.PAY_PAL_ERROR)
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

            else -> Unit
        }
    }
}

/**
 * Reacts to [PayPalCheckoutUIState] updates and drives the PayPal flow:
 * - Idle → No action.
 * - Loading → notifies [loadingDelegate].
 * - LaunchIntent → launches `PayPalWebCheckoutActivity` with the provided client ID and order ID.
 * - Success → completes with [ChargeResponse], notifies [loadingDelegate], and resets state.
 * - Error → completes with an exception, notifies [loadingDelegate], and resets state.
 *
 * @param context Current context.
 * @param uiState Current UI state for the PayPal checkout flow.
 * @param viewModel Coordinator for network operations and state transitions.
 * @param loadingDelegate Optional external loader controller.
 * @param resolvePaymentForResult Launcher to start the web approval activity for result.
 * @param completion Final result callback for the PayPal transaction.
 */
private fun handleUIState(
    context: Context,
    uiState: PayPalCheckoutUIState,
    viewModel: PayPalViewModel,
    loadingDelegate: WidgetLoadingDelegate?,
    resolvePaymentForResult: ManagedActivityResultLauncher<Intent, ActivityResult>,
    completion: (Result<ChargeResponse>) -> Unit,
) {
    when (uiState) {
        // No action needed for the Idle state
        is PayPalCheckoutUIState.Idle -> Unit

        // Handle the loading state by notifying the loading delegate
        is PayPalCheckoutUIState.Loading -> {
            loadingDelegate?.widgetLoadingDidStart()
        }

        // Launch an intent to PayPal's Web Activity if the callback URL is available
        is PayPalCheckoutUIState.LaunchIntent -> {
            loadingDelegate?.widgetLoadingDidFinish()
            val (clientId, orderId) = uiState
            val intent = Intent(context, PayPalWebCheckoutActivity::class.java)
                .putClientIdExtra(clientId)
                .putOrderIdExtra(orderId)
                .putFundingSourceExtra(viewModel.getFundingSource())
            resolvePaymentForResult.launch(intent)
        }

        // Handle success state, notify the loading delegate, and complete the transaction with success
        is PayPalCheckoutUIState.Success -> {
            loadingDelegate?.widgetLoadingDidFinish()
            completion(Result.success(uiState.chargeData))
            // Reset the state to ensure it’s not reused
            viewModel.resetResultState()
        }

        // Handle error state, notify the loading delegate, and complete the transaction with failure
        is PayPalCheckoutUIState.Error -> {
            loadingDelegate?.widgetLoadingDidFinish()
            completion(Result.failure(uiState.exception))
            // Reset the state to ensure it’s not reused
            viewModel.resetResultState()
        }
    }
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewPayPalWidget() {
    PayPalWidget(
        config = PayPalWidgetConfig(
            accessToken = "xxx",
            gatewayId = "xxx"
        ),
        tokenRequest = {}, completion = {}
    )
}
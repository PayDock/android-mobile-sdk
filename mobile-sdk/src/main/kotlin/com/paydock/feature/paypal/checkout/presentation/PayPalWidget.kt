package com.paydock.feature.paypal.checkout.presentation

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.PayPalException
import com.paydock.core.presentation.extensions.alpha40
import com.paydock.core.presentation.extensions.getMessageExtra
import com.paydock.core.presentation.extensions.getStatusExtra
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.components.button.ButtonAppearanceDefaults
import com.paydock.designsystems.components.loader.LoaderAppearance
import com.paydock.designsystems.components.loader.LoaderAppearanceDefaults
import com.paydock.designsystems.components.loader.SdkButtonLoader
import com.paydock.designsystems.theme.PayPal
import com.paydock.feature.paypal.checkout.domain.model.integration.PayPalWidgetConfig
import com.paydock.feature.paypal.checkout.presentation.components.PayPalButton
import com.paydock.feature.paypal.checkout.presentation.state.PayPalCheckoutUIState
import com.paydock.feature.paypal.checkout.presentation.utils.CancellationStatus
import com.paydock.feature.paypal.checkout.presentation.utils.getCancellationStatusExtra
import com.paydock.feature.paypal.checkout.presentation.utils.getDecodedUrlExtra
import com.paydock.feature.paypal.checkout.presentation.utils.putCallbackUrlExtra
import com.paydock.feature.paypal.checkout.presentation.viewmodels.PayPalViewModel
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.model.integration.WalletTokenResult
import org.koin.androidx.compose.koinViewModel

/**
 * A Composable for handling PayPal payments and related interactions.
 *
 * @param modifier Modifier for customizing the appearance and behavior of the Composable.
 * @param enabled Controls the enabled state of this Widget. When false,
 * this component will not respond to user input, and it will appear visually disabled.
 * @param config Configuration for the PayPal Widget.
 * @param appearance Configuration for the visual appearance of the PayPal Widget.
 * @param tokenRequest A callback to obtain the wallet token asynchronously.
 * @param loadingDelegate The delegate passed to overwrite control of showing loaders.
 * @param completion A callback to handle the Wallet Charge result.
 */
@Composable
fun PayPalWidget(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    config: PayPalWidgetConfig = PayPalWidgetConfig(),
    appearance: PayPalWidgetAppearance = PayPalAppearanceDefaults.appearance(),
    tokenRequest: (tokenResult: (Result<WalletTokenResult>) -> Unit) -> Unit,
    loadingDelegate: WidgetLoadingDelegate? = null,
    completion: (Result<ChargeResponse>) -> Unit,
) {
    val context = LocalContext.current
    // Obtain instances of view models
    val viewModel: PayPalViewModel = koinViewModel()

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
        if (loadingDelegate == null && uiState is PayPalCheckoutUIState.Loading) {
            Button(
                onClick = {},
                modifier = Modifier
                    .testTag("loadingPayPalButton")
                    .fillMaxWidth()
                    .height(ButtonAppearanceDefaults.ButtonHeight),
                enabled = true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PayPal,
                    disabledContainerColor = PayPal.alpha40
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        ButtonAppearanceDefaults.ButtonSpacing,
                        Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SdkButtonLoader(appearance = appearance.loader)
                }
            }
        } else {
            // Button to initiate PayPal transaction
            PayPalButton(
                shape = MaterialTheme.shapes.small,
                loaderAppearance = appearance.loader,
                isEnabled = uiState !is PayPalCheckoutUIState.Loading && enabled,
                // Loading is handled by a mock loader button
                onClick = { viewModel.handlePayPalButtonClick(config, tokenRequest) }
            )
        }
    }
}

/**
 * Defines the appearance configuration for the PayPal Widget.
 *
 * This class holds properties that customize the visual aspects of the PayPal Widget,
 * specifically focusing on the components displayed within it, such as the loader.
 *
 * @property loader The appearance configuration for the loader component displayed within the PayPal Widget.
 */
@Immutable
class PayPalWidgetAppearance(val loader: LoaderAppearance) {
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
        loader: LoaderAppearance = this.loader
    ): PayPalWidgetAppearance =
        PayPalWidgetAppearance(loader = loader.copy())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PayPalWidgetAppearance

        return loader == other.loader
    }

    override fun hashCode(): Int {
        return loader.hashCode()
    }
}

/**
 * Default values for the appearance of the PayPal Widget.
 * This object provides a default [PayPalWidgetAppearance] configuration, specifically setting
 * the appearance of the loader shown during the PayPal checkout process.
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
            .copy(strokeWidth = ButtonAppearanceDefaults.ButtonLoaderWidth, color = Color.Black)
    )

}

/**
 * Handles the result returned from the PayPal web activity, processing the decoded URL,
 * cancellation, or errors, and invoking the appropriate completion handler.
 *
 * @param result The `ActivityResult` returned from the PayPal web activity.
 * This contains the result code and data such as the decoded URL or cancellation status.
 * @param viewModel The PayPalViewModel that processes PayPal-related data, including the parsed PayPal URL.
 * @param completion A callback function to handle the result of the PayPal transaction.
 * It is invoked with a `Result` object containing either success or failure information.
 */
private fun handlePayPalResult(
    result: ActivityResult,
    viewModel: PayPalViewModel,
    completion: (Result<ChargeResponse>) -> Unit,
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
                                    displayableMessage = MobileSDKConstants.PayPalConfig.Errors.CANCELLATION_ERROR
                                )
                            )
                        )
                        viewModel.resetResultState()
                    }

                    // If the cancellation was due to another reason, process the WebView error status and message.
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

            // If no specific result code is handled, do nothing.
            else -> Unit
        }
    }
}

/**
 * Processes the current UI state of the PayPal checkout widget and executes the corresponding actions
 * such as launching intents, capturing transactions, or handling success and error states.
 *
 * @param context The current application context.
 * @param uiState The current `PayPalCheckoutUIState` representing the state of the PayPal checkout process.
 * @param viewModel The `PayPalViewModel` managing the PayPal checkout flow and its state.
 * @param loadingDelegate An optional delegate for managing the widget's loading state transitions.
 * @param resolvePaymentForResult A `ManagedActivityResultLauncher` to handle activity results for payment resolution.
 * @param completion A callback to handle the final result of the PayPal checkout process, either success or failure.
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
            val (callbackData) = uiState
            callbackData.callbackUrl?.let { callbackUrl ->
                if (callbackUrl.isNotBlank()) {
                    val intent = Intent(context, PayPalWebActivity::class.java)
                        .putCallbackUrlExtra(callbackUrl)
                    resolvePaymentForResult.launch(intent)
                }
            }
        }

        // Capture the transaction by passing the payment method ID and payer ID to the ViewModel
        is PayPalCheckoutUIState.Capture -> {
            val (paymentMethodId, payerId) = uiState
            viewModel.captureWalletTransaction(paymentMethodId, payerId)
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
    PayPalWidget(tokenRequest = {}, completion = {})
}
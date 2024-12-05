package com.paydock.feature.afterpay.presentation.components

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.afterpay.android.Afterpay
import com.afterpay.android.view.AfterpayPaymentButton
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.afterpay.domain.mapper.integration.mapToAfterpayV2Options
import com.paydock.feature.afterpay.domain.model.integration.AfterpaySDKConfig
import com.paydock.feature.afterpay.presentation.viewmodels.AfterpayViewModel
import kotlinx.coroutines.launch

/**
 * A Composable that displays the Afterpay payment button using `AndroidView`.
 * The button is configured based on the provided `AfterpaySDKConfig` and triggers
 * the checkout process when clicked.
 *
 * @param config The configuration for Afterpay SDK, including button styling and options.
 * @param enabled Indicates whether the payment button is enabled or disabled.
 * @param onObtainToken A lambda function to obtain the wallet token asynchronously.
 *        Accepts a callback to provide the obtained token.
 * @param viewModel The `AfterpayViewModel` instance managing the widget's state and interactions.
 * @param resolvePaymentForResult The launcher to handle the checkout intent's result.
 */
@Composable
internal fun AfterpayPaymentButtonView(
    config: AfterpaySDKConfig,
    enabled: Boolean,
    onObtainToken: (onTokenReceived: (String) -> Unit) -> Unit,
    viewModel: AfterpayViewModel,
    resolvePaymentForResult: ActivityResultLauncher<Intent>,
) {
    val scope = rememberCoroutineScope()

    // AndroidView displays a native Afterpay payment button
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(Theme.dimensions.buttonHeight),
        factory = { context ->
            AfterpayPaymentButton(context).apply {
                this.buttonText = config.buttonTheme.buttonText
                this.colorScheme = config.buttonTheme.colorScheme
                this.isEnabled = enabled

                // Set up click listener to initiate the checkout process
                setOnClickListener {
                    scope.launch {
                        onObtainToken { obtainedToken ->
                            viewModel.setWalletToken(obtainedToken)
                            val intent = createCheckoutIntent(context, config)
                            resolvePaymentForResult.launch(intent)
                        }
                    }
                }
            }
        }
    )
}

/**
 * Creates an intent for the Afterpay checkout process.
 *
 * @param context The context to create the intent.
 * @param config The configuration for the Afterpay SDK.
 * @return The `Intent` to launch the Afterpay checkout activity.
 */
private fun createCheckoutIntent(context: Context, config: AfterpaySDKConfig): Intent =
    config.options?.let {
        Afterpay.createCheckoutV2Intent(context, it.mapToAfterpayV2Options())
    } ?: Afterpay.createCheckoutV2Intent(context)

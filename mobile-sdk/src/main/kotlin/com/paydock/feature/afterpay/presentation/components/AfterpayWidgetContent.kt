package com.paydock.feature.afterpay.presentation.components

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.afterpay.android.Afterpay
import com.afterpay.android.view.AfterpayPaymentButton
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.afterpay.presentation.mapper.mapToAfterpayV2Options
import com.paydock.feature.afterpay.presentation.model.AfterpaySDKConfig
import com.paydock.feature.afterpay.presentation.viewmodels.AfterpayViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Composable function representing the content of the Afterpay payment widget.
 *
 * This function displays the payment button and handles the token acquisition process.
 *
 * @param scope The coroutine scope.
 * @param config The configuration for the Afterpay SDK.
 * @param tokenProvider The callback to obtain the authentication token asynchronously.
 * @param resolvePaymentForResult The ActivityResultLauncher for resolving payment.
 * @param viewModel The AfterpayViewModel instance.
 */
@Composable
internal fun AfterpayWidgetContent(
    scope: CoroutineScope,
    config: AfterpaySDKConfig,
    tokenProvider: (onTokenReceived: (String) -> Unit) -> Unit,
    resolvePaymentForResult: ActivityResultLauncher<Intent>,
    viewModel: AfterpayViewModel,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Theme.dimensions.spacing, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (viewModel.stateFlow.collectAsState().value.validLocale) {
            // Display Afterpay payment button
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Theme.dimensions.buttonHeight),
                factory = { context ->
                    AfterpayPaymentButton(context).apply {
                        this.buttonText = config.buttonTheme.buttonText
                        this.colorScheme = config.buttonTheme.colorScheme
                        setOnClickListener {
                            // Obtain token asynchronously
                            scope.launch {
                                tokenProvider { obtainedToken ->
                                    // Handle obtained token
                                    handleTokenObtained(
                                        context,
                                        obtainedToken,
                                        viewModel,
                                        config.options,
                                        resolvePaymentForResult
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

/**
 * Handles the obtained token and initiates the payment process.
 *
 * @param context The context.
 * @param token The obtained token.
 * @param viewModel The AfterpayViewModel instance.
 * @param checkoutOptions The checkout options.
 * @param resolvePaymentForResult The ActivityResultLauncher for resolving payment.
 */
private fun handleTokenObtained(
    context: Context,
    token: String,
    viewModel: AfterpayViewModel,
    checkoutOptions: AfterpaySDKConfig.CheckoutOptions?,
    resolvePaymentForResult: ActivityResultLauncher<Intent>
) {
    viewModel.setWalletToken(token)
    val intent = checkoutOptions?.let {
        Afterpay.createCheckoutV2Intent(
            context,
            it.mapToAfterpayV2Options()
        )
    }
        ?: Afterpay.createCheckoutV2Intent(context)
    resolvePaymentForResult.launch(intent)
}
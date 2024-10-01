package com.paydock.feature.flypay.presentation.components

import androidx.compose.runtime.Composable
import com.paydock.core.MobileSDKConstants
import com.paydock.designsystems.components.web.SdkWebView

/**
 * A composable function that displays a WebView for processing FlyPay transactions.
 *
 * This WebView loads the provided FlyPay URL, handles specific redirects for successful
 * transactions, and reports failures using the provided callbacks. The function integrates
 * FlyPay's web-based interface into the app via the WebView.
 *
 * @param flyPayUrl The URL to be loaded in the WebView, pointing to the FlyPay transaction page.
 * @param onSuccess Callback invoked when the FlyPay transaction is successfully completed and the
 * redirect URL is detected.
 * @param onFailure Callback invoked if there is a failure in loading the WebView or processing
 * the FlyPay transaction. The callback provides an error status code and an error message.
 */
@Composable
internal fun FlyPayWebView(
    flyPayUrl: String,
    onSuccess: () -> Unit,
    onFailure: (Int, String) -> Unit
) {
    // WebView for displaying the FlyPay URL
    SdkWebView<Unit>(
        webUrl = flyPayUrl,
        shouldShowCustomLoader = false,
        // This is required for FlyPay Web to work
        onShouldOverrideUrlLoading = { request ->
            val requestUrl = request?.url.toString()
            // Handle redirection URLs
            return@SdkWebView if (requestUrl == MobileSDKConstants.FlyPayConfig.FLY_PAY_REDIRECT_URL) {
                onSuccess()
                true
            } else {
                false
            }
        }
    ) { status, message ->
        // Invoke the failure callback if loading fails or an error occurs
        onFailure(status, message)
    }
}

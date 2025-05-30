package com.paydock.feature.colespay.presentation.components

import androidx.compose.runtime.Composable
import com.paydock.core.MobileSDKConstants
import com.paydock.designsystems.components.web.SdkWebView

/**
 * A composable function that displays a WebView for processing Coles Pay transactions.
 *
 * This WebView loads the provided Coles Pay URL, handles specific redirects for successful
 * transactions, and reports failures using the provided callbacks. The function integrates
 * Coles Pay's web-based interface into the app via the WebView.
 *
 * @param colesPayUrl The URL to be loaded in the WebView, pointing to the Coles Pay transaction page.
 * @param onSuccess Callback invoked when the Coles Pay transaction is successfully completed and the
 * redirect URL is detected.
 * @param onFailure Callback invoked if there is a failure in loading the WebView or processing
 * the Coles Pay transaction. The callback provides an error status code and an error message.
 */
@Composable
internal fun ColesPayWebView(
    colesPayUrl: String,
    onSuccess: () -> Unit,
    onFailure: (Int, String) -> Unit
) {
    // WebView for displaying the Coles Pay URL
    SdkWebView<Unit>(
        webUrl = colesPayUrl,
        shouldShowCustomLoader = false,
        // This is required for Coles Pay Web to work
        onShouldOverrideUrlLoading = { request ->
            val requestUrl = request?.url.toString()
            // Handle redirection URLs
            return@SdkWebView if (requestUrl == MobileSDKConstants.ColesPayConfig.COLES_PAY_REDIRECT_URL) {
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

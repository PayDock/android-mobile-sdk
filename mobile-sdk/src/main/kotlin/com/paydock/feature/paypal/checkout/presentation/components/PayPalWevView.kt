package com.paydock.feature.paypal.checkout.presentation.components

import android.net.Uri
import androidx.compose.runtime.Composable
import com.paydock.designsystems.components.web.SdkWebView

/**
 * A composable function that displays a WebView for processing PayPal transactions.
 *
 * This WebView loads the provided PayPal URL, handles specific redirects for successful
 * transactions, and reports failures using the provided callbacks. The function integrates
 * PayPal's web-based interface into the app via the WebView.
 *
 * @param payPayUrl The URL to be loaded in the WebView, pointing to the PayPal transaction page.
 * @param onSuccess Callback invoked when the PayPal transaction is successfully completed and a
 * successful redirect URL is detected. The decoded redirect URL is passed as a parameter.
 * @param onFailure Callback invoked if there is a failure in loading the WebView or processing
 * the PayPal transaction. The callback provides an error status code and an error message.
 */
@Composable
internal fun PayPalWebView(
    payPayUrl: String,
    onSuccess: (String) -> Unit,
    onFailure: (Int, String) -> Unit
) {
    // WebView for displaying the PayPal URL
    SdkWebView<Unit>(
        webUrl = payPayUrl,
        shouldShowCustomLoader = false,
        // This is required for PayPal Web to work
        onShouldOverrideUrlLoading = { request ->
            val requestUrl = request?.url.toString()
            // Handle redirection URLs
            val decodedUrl = Uri.decode(requestUrl)
            return@SdkWebView if (isSuccessfulRedirect(decodedUrl)) {
                onSuccess(decodedUrl)
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

/**
 * Checks if the provided redirect URL indicates a successful PayPal payment.
 *
 * This function verifies the presence of both "token"and "PayerID" parameters in the query string of the redirect URL.
 * These parameters are typically present in a successful PayPal redirect.
 *
 * @param redirectUrl The URL received after the PayPal payment process.
 * @return `true` if the redirect indicates a successful payment, `false` otherwise.
 */
private fun isSuccessfulRedirect(redirectUrl: String): Boolean {
    val uri = Uri.parse(redirectUrl)
    val params = uri.query?.split("&")
        ?.associate { query ->
            val parts = query.split("=")
            parts[0] to (parts.getOrNull(1) ?: "")
        } ?: emptyMap()

    params["token"] ?: return false // Or throw an exception, log an error, etc.
    params["PayerID"] ?: return false // Same as above

    return true
}
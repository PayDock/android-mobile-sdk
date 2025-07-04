package com.paydock.feature.paypal.checkout.presentation.components

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import com.paydock.core.MobileSDKConstants
import com.paydock.designsystems.components.web.SdkWebView

/**
 * Displays a WebView for handling PayPal payments.
 *
 * @param payPayUrl The URL for the PayPal payment page.
 * @param onSuccess Callback function invoked when the payment is successfully completed.
 * It receives the decoded redirect URL as a parameter.
 * @param onCancel Callback function invoked when the payment is cancelled by the user.
 * It receives the decoded redirect URL as a parameter.
 * @param onFailure Callback function invoked when the WebView encounters an error during loading
 * or if the payment fails.  It receives an error code and a corresponding error message.
 */
@Composable
internal fun PayPalWebView(
    payPayUrl: String,
    onSuccess: (String) -> Unit,
    onCancel: (String) -> Unit,
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
            when (validateRedirect(decodedUrl)) {
                PaymentStatus.Complete -> {
                    onSuccess(decodedUrl)
                    return@SdkWebView true
                }

                PaymentStatus.Cancel -> {
                    onCancel(decodedUrl)
                    return@SdkWebView true
                }

                PaymentStatus.NONE -> {
                    // Continue with the request
                    return@SdkWebView false
                }
            }
        }
    ) { status, message ->
        // Invoke the failure callback if loading fails or an error occurs
        onFailure(status, message)
    }
}

/**
 * Checks if the provided redirect URL indicates a successful or canceled PayPal payment.
 *
 * This function verifies the presence of specific parameters in the query string of the
 * redirect URL to determine the payment status.
 *
 * @param redirectUrl The URL received after the PayPal payment process.
 * @return A sealed class representing the outcome of the redirect:
 *         - [PaymentStatus.Success] if the redirect indicates a successful payment.
 *         - [PaymentStatus.Cancel] if the redirect indicates a canceled payment.
 *         - [PaymentStatus.NONE] if the redirect does not match any known pattern.
 */
private fun validateRedirect(redirectUrl: String): PaymentStatus {
    val uri = redirectUrl.toUri()
    val params = uri.query?.split("&")
        ?.associate { query ->
            val parts = query.split("=")
            parts[0] to (parts.getOrNull(1) ?: "")
        } ?: emptyMap()

    // Check for cancellation first
    return when (params[MobileSDKConstants.PayPalConfig.OP_TYPE_KEY]) {
        MobileSDKConstants.PayPalConfig.CANCEL_TYPE -> PaymentStatus.Cancel
        MobileSDKConstants.PayPalConfig.COMPLETE_TYPE -> PaymentStatus.Complete
        else -> PaymentStatus.NONE
    }
}

/**
 * Represents the status of a payment operation.
 *
 * This sealed class provides a type-safe way to represent the different states a payment can be in.
 * It can be either [Complete], [Cancel], or [NONE].
 */
internal sealed class PaymentStatus {
    // This could be success or failure
    data object Complete : PaymentStatus()
    data object Cancel : PaymentStatus()
    data object NONE : PaymentStatus()
}
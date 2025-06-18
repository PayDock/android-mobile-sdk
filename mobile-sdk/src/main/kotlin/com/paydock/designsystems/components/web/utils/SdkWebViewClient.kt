package com.paydock.designsystems.components.web.utils

import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import com.kevinnzou.web.AccompanistWebViewClient
import com.paydock.core.MobileSDKConstants

/**
 * Custom WebViewClient for handling various WebView events such as URL loading, page finished loading,
 * and error handling. This class extends AccompanistWebViewClient and provides callbacks for key events
 * like overriding URL loading and handling critical/non-fatal WebView errors.
 *
 * @param onShouldOverrideUrlLoading Optional callback that allows customization of URL loading behavior.
 * @param onPageFinished Callback that is triggered when a page finishes loading.
 * @param onWebViewError Callback that is triggered when a WebView encounters an error.
 * It provides the error code and a user-friendly error message.
 */
internal class SdkWebViewClient(
    private val onShouldOverrideUrlLoading: ((request: WebResourceRequest?) -> Boolean)? = null,
    private val onPageFinished: (WebView) -> Unit = {},
    private val onWebViewError: (Int, String) -> Unit,
) : AccompanistWebViewClient() {

    /**
     * Called when a page has finished loading. Invokes the provided [onPageFinished] callback.
     *
     * @param view The WebView that is loading the content.
     * @param url The URL of the loaded page.
     */
    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        onPageFinished(view)
    }

    /**
     * Decides whether to override the URL loading. If a custom [onShouldOverrideUrlLoading] callback is provided,
     * it is called to determine whether the WebView should handle the URL request. Otherwise, the default behavior
     * is used.
     *
     * @param view The WebView that is requesting to load the URL.
     * @param request The request containing the details of the URL.
     * @return Boolean value indicating whether the WebView should handle the request.
     */
    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest?
    ): Boolean {
        return onShouldOverrideUrlLoading?.let {
            it(request)
        } ?: super.shouldOverrideUrlLoading(view, request)
    }

    /**
     * Handles WebView errors. Differentiates between fatal and non-fatal errors. For fatal errors
     * (those affecting the main frame), it logs the error and triggers the [onWebViewError] callback.
     * Non-fatal errors (those affecting sub-frames) are logged but not escalated.
     *
     * @param view The WebView that encountered an error.
     * @param request The request associated with the error.
     * @param error The WebResourceError containing the details of the error.
     */
    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest?,
        error: WebResourceError?,
    ) {
        if (error != null) {
            // Get a user-friendly error message based on the WebView error code.
            val errorMessage = getWebViewErrorMessage(error.errorCode)

            // If the error occurred on the main frame, consider it critical and trigger the error callback.
            if (request?.isForMainFrame == true) {
                Log.d(MobileSDKConstants.MOBILE_SDK_TAG, "Fatal error: $errorMessage")
                onWebViewError(error.errorCode, errorMessage)
            } else {
                // Log non-fatal errors without triggering the error callback.
                Log.d(MobileSDKConstants.MOBILE_SDK_TAG, "Non-Fatal error: $errorMessage")
            }
        }
    }

    /**
     * Maps WebView error codes to user-friendly error messages. This function provides a more readable
     * and understandable error message for various WebView errors such as network issues, SSL failures,
     * and file-related problems.
     *
     * @param errorCode The error code returned by the WebView, which corresponds to specific types of errors.
     * @return A user-friendly error message corresponding to the given error code.
     */
    @Suppress("CyclomaticComplexMethod")
    private fun getWebViewErrorMessage(errorCode: Int): String = when (errorCode) {
        ERROR_AUTHENTICATION -> "User authentication failed. Please check your credentials and try again."
        ERROR_TIMEOUT -> "The server is taking too much time to respond. Please try again later."
        ERROR_TOO_MANY_REQUESTS -> "Too many requests. Please try again later."
        ERROR_UNKNOWN -> "An unknown error occurred. Please try again later."
        ERROR_BAD_URL -> "The URL you entered is not valid. Please check the URL and try again."
        ERROR_CONNECT -> "Failed to connect to the server. Please check your internet connection and try again."
        ERROR_FAILED_SSL_HANDSHAKE -> "Failed to establish a secure connection to the server."
        ERROR_HOST_LOOKUP -> "Failed to lookup server hostname. Please check your internet connection and try again."
        ERROR_PROXY_AUTHENTICATION -> "Proxy authentication failed. Please check your proxy credentials and try again."
        ERROR_REDIRECT_LOOP -> "Too many redirects. Please try again later."
        ERROR_UNSUPPORTED_AUTH_SCHEME -> "Unsupported authentication scheme. Please try again later."
        ERROR_UNSUPPORTED_SCHEME -> "Unsupported URL scheme. Please try again later."
        ERROR_FILE -> "File-related error. Please try again later."
        ERROR_FILE_NOT_FOUND -> "File not found. Please try again later."
        ERROR_IO -> "The server failed to communicate. Please try again later."
        else -> "An unknown error occurred. Please try again later."
    }
}
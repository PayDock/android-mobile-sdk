package com.paydock.designsystems.components.web.utils

import android.graphics.Bitmap
import android.net.Uri
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
 * @param isSubResourceNetworkErrorFatal Decides, by request host, whether a critical network error
 * on a sub-resource (script, iframe, XHR…) ends the session. Defaults to always fatal, because widgets
 * loaded from HTML data only see connectivity loss through their sub-resources.
 */
internal class SdkWebViewClient(
    private val onShouldOverrideUrlLoading: ((request: WebResourceRequest?) -> Boolean)? = null,
    private val onPageFinished: (WebView) -> Unit = {},
    private val onWebViewError: (Int, String) -> Unit,
    private val onCloseRequested: () -> Unit = {},
    /**
     * When true, delegates lifecycle callbacks to the base AccompanistWebViewClient.
     * Must be false when this client is used with a raw Android WebView (not the Compose WebView),
     * because the base class relies on an internal state that is only initialized by the Compose wrapper.
     */
    private val delegateToAccompanist: Boolean = true,
    private val isSubResourceNetworkErrorFatal: (host: String?) -> Boolean = { true },
) : AccompanistWebViewClient() {

    companion object {
        private val errorMessageByCode: Map<Int, String> = mapOf(
            ERROR_AUTHENTICATION to "User authentication failed. Please check your credentials and try again.",
            ERROR_TIMEOUT to "The server is taking too much time to respond. Please try again later.",
            ERROR_TOO_MANY_REQUESTS to "Too many requests. Please try again later.",
            ERROR_UNKNOWN to "An unknown error occurred. Please try again later.",
            ERROR_BAD_URL to "The URL you entered is not valid. Please check the URL and try again.",
            ERROR_CONNECT to "Failed to connect to the server. Please check your internet connection and try again.",
            ERROR_FAILED_SSL_HANDSHAKE to "Failed to establish a secure connection to the server.",
            ERROR_HOST_LOOKUP to "Failed to lookup server hostname. Please check your internet connection and try again.",
            ERROR_PROXY_AUTHENTICATION to "Proxy authentication failed. Please check your proxy credentials and try again.",
            ERROR_REDIRECT_LOOP to "Too many redirects. Please try again later.",
            ERROR_UNSUPPORTED_AUTH_SCHEME to "Unsupported authentication scheme. Please try again later.",
            ERROR_UNSUPPORTED_SCHEME to "Unsupported URL scheme. Please try again later.",
            ERROR_FILE to "File-related error. Please try again later.",
            ERROR_FILE_NOT_FOUND to "File not found. Please try again later.",
            ERROR_IO to "The server failed to communicate. Please try again later."
        )

        fun mapWebViewErrorMessage(errorCode: Int): String =
            errorMessageByCode[errorCode] ?: "An unknown error occurred. Please try again later."

        /**
         * Whether [errorCode] is a concrete network failure. ERROR_UNKNOWN (-1) is deliberately excluded:
         * it's frequently emitted for benign sub-resource failures (e.g. feature flag streams like LaunchDarkly).
         */
        fun isCriticalNetworkError(errorCode: Int): Boolean = when (errorCode) {
            ERROR_HOST_LOOKUP,
            ERROR_CONNECT,
            ERROR_TIMEOUT,
            ERROR_IO -> true
            else -> false
        }

        /**
         * Describes a failed request for logs as scheme, host and path only. The query string is dropped
         * because 3DS URLs can carry transaction identifiers and tokens.
         */
        fun describeFailedRequest(uri: Uri?): String =
            uri?.let { "${it.scheme}://${it.host}${it.path.orEmpty()}" } ?: "unknown"

        /** String form of [describeFailedRequest]: drops the query string and fragment. */
        fun describeUrl(url: String?): String =
            url?.substringBefore('?')?.substringBefore('#') ?: "unknown"

        private val URL_QUERY_REGEX = Regex("""(https?://[^\s?#"'<>]+)[?#][^\s"'<>]*""")

        /** Drops the query string and fragment of every http(s) URL inside free text (e.g. JS console logs). */
        fun redactUrlQueries(text: String): String = text.replace(URL_QUERY_REGEX, "$1")

        /**
         * Logs a critical network error that was not escalated because it happened on a non-essential
         * host (e.g. an issuer page's analytics script). Logged as a warning so it reaches release log
         * collectors (e.g. Sentry logcat breadcrumbs) while the flow continues.
         */
        fun logIgnoredNetworkError(source: String, errorCode: Int, error: WebResourceError, uri: Uri?) {
            Log.w(
                MobileSDKConstants.MOBILE_SDK_TAG,
                "Ignored network error on non-essential $source (code $errorCode, " +
                    "error: ${error.description}, url: ${describeFailedRequest(uri)})"
            )
        }
    }

    @Volatile
    private var criticalErrorOccurredInSession: Boolean = false
    private var currentMainUrl: String? = null

    /**
     * Called when a page starts loading. This method resets the critical error flag if the main URL
     * changes or if it's the first page load.
     *
     * @param view The WebView that is loading the content.
     * @param url The URL of the page that is starting to load.
     * @param favicon The favicon for the page, or null if there isn't one.
     */
    override fun onPageStarted(
        view: WebView,
        url: String?,
        favicon: Bitmap?
    ) {
        if (delegateToAccompanist) {
            super.onPageStarted(view, url, favicon)
        }
        // If the main URL changes or it's the first load, reset the error flag.
        if (currentMainUrl != url) {
            Log.d(
                MobileSDKConstants.MOBILE_SDK_TAG,
                "New page loading (${describeUrl(url)}), resetting critical error flag."
            )
            criticalErrorOccurredInSession = false
            currentMainUrl = url
        }
    }

    /**
     * Called when a page has finished loading. Invokes the provided [onPageFinished] callback.
     *
     * @param view The WebView that is loading the content.
     * @param url The URL of the loaded page.
     */
    override fun onPageFinished(view: WebView, url: String?) {
        if (delegateToAccompanist) {
            super.onPageFinished(view, url)
        }
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
        // Intercept our custom scheme emitted from injected JS window.close override
        if (request?.url?.scheme == MobileSDKConstants.WEB_SCHEME && request.url.host == "close") {
            onCloseRequested()
            return true
        }
        return onShouldOverrideUrlLoading?.let { callback ->
            callback(request)
        } ?: if (delegateToAccompanist) {
            super.shouldOverrideUrlLoading(view, request)
        } else {
            // Default WebView behavior: do not override
            false
        }
    }

    /**
     * Handles WebView errors. Differentiates between fatal and non-fatal errors. Main-frame errors are
     * always fatal; critical network errors on sub-resources are fatal when [isSubResourceNetworkErrorFatal]
     * accepts the request host. Fatal errors trigger [onWebViewError] once per session; the rest are only logged.
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
            val errorCode = error.errorCode

            val isMainFrame = request?.isForMainFrame == true
            val isErrorFatalForSession = isMainFrame ||
                (isCriticalNetworkError(errorCode) && isSubResourceNetworkErrorFatal(request?.url?.host))

            if (isErrorFatalForSession) {
                if (!criticalErrorOccurredInSession) {
                    criticalErrorOccurredInSession = true
                    Log.e(
                        MobileSDKConstants.MOBILE_SDK_TAG,
                        "First critical error in session (code $errorCode, error: ${error.description}, " +
                            "mainFrame: $isMainFrame, url: ${describeFailedRequest(request?.url)}): $errorMessage"
                    )
                    onWebViewError(errorCode, errorMessage)
                } else {
                    Log.d(
                        MobileSDKConstants.MOBILE_SDK_TAG,
                        "Subsequent critical error in session ignored (code $errorCode, " +
                            "url: ${describeFailedRequest(request?.url)}): $errorMessage"
                    )
                }
            } else if (isCriticalNetworkError(errorCode)) {
                // A third-party resource (e.g. an issuer page's script or image) failed: keep the flow going but record it.
                logIgnoredNetworkError("sub-resource", errorCode, error, request?.url)
            } else {
                // Log other non-fatal errors (e.g. benign ERROR_UNKNOWN noise) without triggering the error callback.
                Log.d(
                    MobileSDKConstants.MOBILE_SDK_TAG,
                    "Non-Fatal sub-resource error (code $errorCode, url: ${describeFailedRequest(request?.url)}): $errorMessage"
                )
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
    private fun getWebViewErrorMessage(errorCode: Int): String = mapWebViewErrorMessage(errorCode)
}
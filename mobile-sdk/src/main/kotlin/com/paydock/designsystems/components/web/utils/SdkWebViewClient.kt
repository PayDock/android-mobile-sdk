package com.paydock.designsystems.components.web.utils

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.annotation.RequiresApi
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
 * It provides the error code, a user-friendly error message and the host of the failed request, if known.
 * @param isSubResourceNetworkErrorFatal Decides, by request host, whether a critical network error
 * on a sub-resource (script, iframe, XHR…) ends the session. Defaults to always fatal, because widgets
 * loaded from HTML data only see connectivity loss through their sub-resources.
 * @param criticalResourceUrls Exact URLs whose network errors or HTTP error responses (4xx/5xx) end the session,
 * e.g. the Client SDK script: without it the page can never progress. Empty by default, so HTTP errors are
 * ignored (favicons, issuer page assets and API calls, which the Client SDK reports itself).
 * @param onRendererGone Called once when the WebView's renderer process stops (Android 8+), with `true` if it
 * crashed and `false` if Android reclaimed it for memory. When set, the host app survives and the WebView must not
 * be used again. When null, the platform default applies: Android kills the host app.
 */
internal class SdkWebViewClient(
    private val onShouldOverrideUrlLoading: ((request: WebResourceRequest?) -> Boolean)? = null,
    private val onPageFinished: (WebView) -> Unit = {},
    private val onWebViewError: (code: Int, message: String, host: String?) -> Unit,
    private val onCloseRequested: () -> Unit = {},
    /**
     * When true, delegates lifecycle callbacks to the base AccompanistWebViewClient.
     * Must be false when this client is used with a raw Android WebView (not the Compose WebView),
     * because the base class relies on an internal state that is only initialized by the Compose wrapper.
     */
    private val delegateToAccompanist: Boolean = true,
    private val isSubResourceNetworkErrorFatal: (host: String?) -> Boolean = { true },
    private val criticalResourceUrls: Set<String> = emptySet(),
    private val onRendererGone: ((didCrash: Boolean) -> Unit)? = null,
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
         * Describes a failed request for logs and error messages as its host only. Paths, query strings and
         * fragments are dropped because 3DS URLs can carry transaction identifiers and tokens.
         */
        fun describeFailedRequest(uri: Uri?): String = uri?.host?.takeIf { it.isNotBlank() } ?: "unknown"

        /** User-friendly message for an HTTP error response on a critical resource. */
        fun criticalResourceHttpErrorMessage(statusCode: Int): String =
            "Failed to load the payment page (HTTP $statusCode). Please try again later."

        /** String form of [describeFailedRequest]: keeps the host only. */
        fun describeUrl(url: String?): String = url
            ?.substringAfter("://")
            ?.substringBefore('/')
            ?.substringBefore('?')
            ?.substringBefore('#')
            ?.substringAfterLast('@')
            ?.takeIf { it.isNotBlank() }
            ?: "unknown"

        private val URL_QUERY_REGEX = Regex("""(https?://[^\s?#"'<>]+)[?#][^\s"'<>]*""")

        /** Drops the query string and fragment of every http(s) URL inside free text (e.g. JS console logs). */
        fun redactUrlQueries(text: String): String = text.replace(URL_QUERY_REGEX, "$1")

        /**
         * Logs a critical network error that was not escalated because it happened on a non-essential
         * host (e.g. an issuer page's analytics script). Logged as a warning so it reaches release log
         * collectors (e.g. Sentry logcat breadcrumbs) while the flow continues. Only the host is logged:
         * these pages are outside Paydock's control and their paths can carry session identifiers.
         */
        fun logIgnoredNetworkError(source: String, errorCode: Int, error: WebResourceError, uri: Uri?) {
            Log.w(
                MobileSDKConstants.MOBILE_SDK_TAG,
                "Ignored network error on non-essential $source (code $errorCode, " +
                    "error: ${error.description}, host: ${uri?.host ?: "unknown"})"
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
     * always fatal, and so is any error on [criticalResourceUrls]; critical network errors on other sub-resources
     * are fatal when [isSubResourceNetworkErrorFatal] accepts the request host.
     * Fatal errors trigger [onWebViewError] once per session; the rest are only logged.
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
                request?.url?.toString() in criticalResourceUrls ||
                (isCriticalNetworkError(errorCode) && isSubResourceNetworkErrorFatal(request?.url?.host))

            if (isErrorFatalForSession) {
                reportCriticalError(errorCode, errorMessage, error.description, isMainFrame, request?.url)
            } else if (isCriticalNetworkError(errorCode)) {
                // A third-party resource (e.g. an issuer page's script or image) failed: keep the flow going but record it.
                logIgnoredNetworkError("sub-resource", errorCode, error, request?.url)
            } else {
                // Log other non-fatal errors (e.g. benign ERROR_UNKNOWN noise) without triggering the error callback.
                Log.d(
                    MobileSDKConstants.MOBILE_SDK_TAG,
                    "Non-Fatal sub-resource error (code $errorCode, host: ${describeFailedRequest(request?.url)}): $errorMessage"
                )
            }
        }
    }

    /**
     * Handles HTTP error responses (4xx/5xx). Only responses for [criticalResourceUrls] (exact match) end the
     * session; every other HTTP error is left to the page, as before.
     *
     * @param view The WebView that received the response.
     * @param request The request associated with the response.
     * @param errorResponse The HTTP error response.
     */
    override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?,
    ) {
        super.onReceivedHttpError(view, request, errorResponse)
        val statusCode = errorResponse?.statusCode ?: return
        if (request?.url?.toString() !in criticalResourceUrls) return
        reportCriticalError(
            errorCode = statusCode,
            errorMessage = criticalResourceHttpErrorMessage(statusCode),
            errorName = "HTTP $statusCode",
            isMainFrame = request?.isForMainFrame == true,
            uri = request?.url,
        )
    }

    /**
     * Handles the WebView's renderer process stopping. With [onRendererGone] set, reports it once per session
     * (unless a critical error already ended the session) and returns `true` so Android keeps the host app alive.
     * Without it, defers to the platform default, which kills the host app.
     *
     * @param view The WebView whose renderer stopped. It can't be used again.
     * @param detail Whether the renderer crashed or was reclaimed for memory.
     * @return `true` if handled, so the host app survives.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
        val callback = onRendererGone ?: return super.onRenderProcessGone(view, detail)
        val didCrash = detail.didCrash()
        Log.e(MobileSDKConstants.MOBILE_SDK_TAG, "WebView renderer process gone (crashed: $didCrash)")
        if (!criticalErrorOccurredInSession) {
            criticalErrorOccurredInSession = true
            callback(didCrash)
        }
        return true
    }

    /**
     * Reports the first critical error of the session through [onWebViewError] and logs it; later critical
     * errors in the same session are only logged.
     */
    private fun reportCriticalError(
        errorCode: Int,
        errorMessage: String,
        errorName: CharSequence?,
        isMainFrame: Boolean,
        uri: Uri?,
    ) {
        if (!criticalErrorOccurredInSession) {
            criticalErrorOccurredInSession = true
            Log.e(
                MobileSDKConstants.MOBILE_SDK_TAG,
                "First critical error in session (code $errorCode, error: $errorName, " +
                    "mainFrame: $isMainFrame, host: ${describeFailedRequest(uri)}): $errorMessage"
            )
            onWebViewError(errorCode, errorMessage, uri?.host)
        } else {
            Log.d(
                MobileSDKConstants.MOBILE_SDK_TAG,
                "Subsequent critical error in session ignored (code $errorCode, " +
                    "host: ${describeFailedRequest(uri)}): $errorMessage"
            )
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
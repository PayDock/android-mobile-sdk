package com.paydock.feature.zip.presentation.components

import android.net.Uri
import androidx.compose.runtime.Composable
import com.paydock.core.MobileSDKConstants
import com.paydock.designsystems.components.web.SdkWebView
import com.paydock.feature.zip.domain.model.ZipCallbackData
import com.paydock.feature.zip.domain.model.ZipStatus
import com.paydock.feature.zip.presentation.utils.ZipJSBridge

/**
 * A Composable WebView for handling Zip checkout flow.
 *
 * This component loads the Zip checkout URL and intercepts callback URLs to detect
 * when the user has completed (or cancelled/declined) the checkout process.
 * It uses the shared [SdkWebView] component for consistent WebView behavior across the SDK.
 *
 * @param checkoutUrl The Zip checkout URL to load.
 * @param onApprove Callback invoked when the Zip checkout is approved.
 * @param onFailure Callback invoked when the Zip checkout fails or is cancelled.
 * @param onWebViewError Callback invoked when a WebView error occurs.
 */
@Composable
internal fun ZipWebView(
    checkoutUrl: String,
    onApprove: (ZipCallbackData) -> Unit,
    onFailure: (ZipCallbackData) -> Unit,
    onWebViewError: (Int, String) -> Unit
) {
    // Track whether we've already handled the callback to prevent duplicate processing
    var hasHandledCallback = false

    /**
     * Parses the callback URL and invokes the appropriate callback.
     */
    fun handleCallbackUrl(urlString: String): Boolean {
        if (hasHandledCallback) return true

        val uri = Uri.parse(urlString)
        val result = uri.getQueryParameter("result") ?: return false

        hasHandledCallback = true

        val checkoutId = uri.getQueryParameter("checkoutId")
        val orderId = uri.getQueryParameter("order_id") ?: uri.getQueryParameter("id")

        val status = ZipStatus.fromValue(result)

        if (status == null) {
            // Unknown status - treat as failure
            val callbackData = ZipCallbackData(
                status = ZipStatus.UNEXPECTED,
                checkoutId = checkoutId,
                orderId = orderId
            )
            onFailure(callbackData)
            return true
        }

        val callbackData = ZipCallbackData(
            status = status,
            checkoutId = checkoutId,
            orderId = orderId
        )

        when {
            status.isSuccess -> onApprove(callbackData)
            else -> onFailure(callbackData)
        }

        return true
    }

    /**
     * Checks if the URL is a Zip callback URL.
     */
    fun isCallbackUrl(urlString: String): Boolean {
        return urlString.contains("/merchant/callback") ||
            urlString.startsWith(MobileSDKConstants.ZipConfig.ZIP_REDIRECT_URL)
    }

    // WebView for displaying the Zip checkout URL.
    // Open target="_blank" links (e.g. "Forgot password", "Apply now") in the same WebView so they load instead of a blank screen.
    SdkWebView(
        webUrl = checkoutUrl,
        openTargetBlankInSameWebView = true,
        jsBridge = ZipJSBridge { url ->
            // Handle URL changes detected by JavaScript (React History API)
            if (isCallbackUrl(url) && url.contains("result=")) {
                handleCallbackUrl(url)
            }
        },
        shouldShowCustomLoader = false,
        onCloseRequested = {
            if (!hasHandledCallback) {
                hasHandledCallback = true
                onFailure(
                    ZipCallbackData(
                        status = ZipStatus.CANCELLED,
                        checkoutId = null,
                        orderId = null
                    )
                )
            }
        },
        onShouldOverrideUrlLoading = { request ->
            val requestUrl = request?.url?.toString() ?: return@SdkWebView false

            // Check if this is a callback URL with a result parameter
            if (isCallbackUrl(requestUrl) && requestUrl.contains("result=")) {
                handleCallbackUrl(requestUrl)
            } else {
                false
            }
        },
        onPageFinished = { webView ->
            // Inject JavaScript to listen for navigation via React's History API
            // This is necessary because Zip's web app uses client-side routing
            val reactHistoryAPIScript = """
            (function() {
                function notifyAndroid() {
                    ${MobileSDKConstants.JS_BRIDGE_NAME}.postMessage(window.location.href);
                }

                history.pushState = (function(f) {
                    return function pushState() {
                        var result = f.apply(this, arguments);
                        notifyAndroid();
                        return result;
                    };
                })(history.pushState);

                history.replaceState = (function(f) {
                    return function replaceState() {
                        var result = f.apply(this, arguments);
                        notifyAndroid();
                        return result;
                    };
                })(history.replaceState);

                window.addEventListener('popstate', notifyAndroid);
                notifyAndroid();
            })();
            """.trimIndent()
            webView.evaluateJavascript(reactHistoryAPIScript, null)
        },
        onWebViewError = { status, message ->
            if (!hasHandledCallback) {
                hasHandledCallback = true
                onWebViewError(status, message)
            }
        }
    )
}

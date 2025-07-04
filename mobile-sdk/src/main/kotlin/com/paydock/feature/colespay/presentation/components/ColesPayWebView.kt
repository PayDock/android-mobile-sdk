package com.paydock.feature.colespay.presentation.components

import androidx.compose.runtime.Composable
import com.paydock.core.MobileSDKConstants
import com.paydock.designsystems.components.web.SdkWebView
import com.paydock.feature.colespay.presentation.utils.ColesPayJSBridge

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
    SdkWebView(
        webUrl = colesPayUrl,
        jsBridge = ColesPayJSBridge {
            if (it.contains(MobileSDKConstants.ColesPayConfig.COLES_PAY_SUCCESS_PATH)) {
                onSuccess()
            }
        },
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
        },
        onPageFinished = { webView ->
            // Listens for navigation in the History APU which is a usual method of React apps changing their pages
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
            // Invoke the failure callback if loading fails or an error occurs
            onFailure(status, message)
        }
    )
}

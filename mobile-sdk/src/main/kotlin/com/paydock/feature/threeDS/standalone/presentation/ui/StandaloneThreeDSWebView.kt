package com.paydock.feature.threeDS.standalone.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberSaveableWebViewState
import com.kevinnzou.web.rememberWebViewNavigator
import com.paydock.core.MobileSDKConstants
import com.paydock.core.extensions.safeCastAs
import com.paydock.core.presentation.extensions.openBrowser
import com.paydock.designsystems.components.web.extensions.setup
import com.paydock.designsystems.components.web.utils.SdkJSBridge
import com.paydock.designsystems.components.web.utils.SdkWebChromeClient
import com.paydock.designsystems.components.web.utils.SdkWebViewClient
import java.net.HttpURLConnection

/** Script injected when [openTargetBlankInSameWebView] is true so target="_blank" / window.open load in the same WebView. */
private val OPEN_IN_SAME_WINDOW_SCRIPT = """
(function() {
    var origOpen = window.open;
    window.open = function(url, target, features) {
        if (!target || String(target) === '_blank') {
            if (url) window.location.href = url;
            return null;
        }
        return origOpen ? origOpen.call(window, url, target, features) : null;
    };
    document.addEventListener('click', function(e) {
        var a = e.target && (e.target.closest ? e.target.closest('a') : (function(n) { while (n && n !== document) { if (n.tagName === 'A') return n; n = n.parentNode; } return null; })(e.target));
        if (a && (a.target === '_blank' || a.getAttribute('target') === '_blank')) {
            e.preventDefault();
            e.stopPropagation();
            if (a.href) window.location.href = a.href;
        }
    }, true);
})();
""".trimIndent()

/**
 * Composable function to display a WebView with customizable settings and event handling.
 *
 * @param webUrl The URL to load in the WebView.
 * @param data Optional HTML data to load in the WebView instead of a URL.
 * @param jsBridge The generic JavaScript interface for communication between WebView and Android code.
 * @param openTargetBlankInSameWebView When true, links with target="_blank" (e.g. "Forgot password", "Apply now")
 *        load in the current WebView instead of opening a new window. The SDK injects JS (window.open override
 *        and click interception) so navigation works like a single-tab browser. Default is false.
 * @param criticalResourceUrls Exact URLs whose network or HTTP errors end the session (the Client SDK script).
 *        Besides those and main-frame errors, failed requests inside the page (Paydock, GPayments, issuer pages)
 *        and child windows never end the session: the Client SDK times out and retries its own requests and
 *        reports a failed flow through its events.
 * @param onRendererGone Called once if the WebView's renderer process stops (Android 8+), with `true` if it
 *        crashed and `false` if Android reclaimed it for memory. The host app survives; the session can't continue.
 *        The dead WebView is removed from the screen and destroyed straight away.
 * @param onWebViewError Callback to handle WebView errors, with the error code, message and failing host.
 */
@Suppress("LongMethod")
@Composable
internal fun <T : Any?> StandaloneThreeDSWebView(
    webUrl: String,
    data: String? = null,
    jsBridge: SdkJSBridge<T>? = null,
    onCloseRequested: () -> Unit = {},
    openTargetBlankInSameWebView: Boolean = false,
    criticalResourceUrls: Set<String> = emptySet(),
    onRendererGone: (didCrash: Boolean) -> Unit = {},
    onWebViewError: (code: Int, message: String, host: String?) -> Unit,
) {
    // Get the current context
    val context = LocalContext.current

    // Remember the state of the WebView
    val state = rememberSaveableWebViewState()
    val navigator = rememberWebViewNavigator()

    // Once the renderer is gone the WebView can't be used again: it's removed from the screen and destroyed
    var rendererGone by remember { mutableStateOf(false) }
    val currentOnRendererGone by rememberUpdatedState(onRendererGone)

    // Load the initial URL (or HTML data) using LaunchedEffect.
    // This ensures the URL is loaded only when `webUrl` or `data` changes,
    // or on initial composition if not already loaded by the restored state.
    // `state.lastLoadedUrl` helps determine if it's truly an initial load vs. restoration.
    LaunchedEffect(state, navigator, webUrl, data) {
        // Only load if the state doesn't already have a lastLoadedUrl (i.e., it's a fresh state, not restored)
        // OR if the webUrl/data prop has changed and it's different from what was last loaded.
        // The library's state restoration should handle keeping the current page on configuration change.
        // This effect is more for the *initial* setup or if the input `webUrl`/`data` prop changes.
        val currentLoadedUrl = state.lastLoadedUrl
        if (currentLoadedUrl == null || (data == null && currentLoadedUrl != webUrl)) {
            if (!data.isNullOrBlank()) {
                // Ensure a base URL is provided for HTML data if it needs to resolve relative paths
                navigator.loadHtml(data, baseUrl = webUrl, mimeType = "text/html")
            } else {
                navigator.loadUrl(webUrl)
            }
        }
    }

    // Display a WebView in a Box composable
    Box(
        contentAlignment = Alignment.Center,
    ) {
        if (rendererGone) return@Box
        WebView(
            state = state,
            modifier = Modifier.fillMaxSize(),
            navigator = navigator,
            client = remember(openTargetBlankInSameWebView, criticalResourceUrls) {
                SdkWebViewClient(
                    onPageFinished = { webView ->
                        // When enabled, make target="_blank" / window.open load in the same WebView (SDK-wide)
                        if (openTargetBlankInSameWebView) {
                            webView.evaluateJavascript(OPEN_IN_SAME_WINDOW_SCRIPT, null)
                        }
                        // Inject a small shim to map window.close() into a custom scheme that we intercept
                        webView.evaluateJavascript(
                            """
                            (function(){
                              try {
                                var __origClose = window.close;
                                window.close = function(){
                                  location.href = "${MobileSDKConstants.WEB_CLOSE_DEEPLINK}";
                                };
                              } catch(e) {}
                            })();
                            """.trimIndent(),
                            null
                        )
                    },
                    onWebViewError = onWebViewError,
                    onCloseRequested = onCloseRequested,
                    // The Client SDK owns its requests (e.g. the 3DS result polls, which fail whenever the network
                    // drops while the shopper approves in their banking app): it retries them and reports failures.
                    isSubResourceNetworkErrorFatal = { false },
                    criticalResourceUrls = criticalResourceUrls,
                    onRendererGone = { didCrash ->
                        rendererGone = true
                        currentOnRendererGone(didCrash)
                    },
                )
            },
            chromeClient = remember(openTargetBlankInSameWebView) {
                SdkWebChromeClient(
                    context = context,
                    openTargetBlankInSameWebView = openTargetBlankInSameWebView,
                    onOpenWebView = { webView -> },
                    onPageFinished = { webView ->
                        // Ensure child windows also map window.close to our scheme
                        webView.evaluateJavascript(
                            """
                            (function(){
                              try {
                                var __origClose = window.close;
                                window.close = function(){
                                  location.href = "${MobileSDKConstants.WEB_CLOSE_DEEPLINK}";
                                };
                              } catch(e) {}
                            })();
                            """.trimIndent(),
                            null
                        )
                    },
                    onWebViewError = { code, message -> onWebViewError(code, message, null) },
                    onCloseRequested = onCloseRequested,
                    isChildWindowErrorFatal = { _, _ -> false },
                    openExternalLink = { url ->
                        context.openBrowser(uri = url, onError = { failedUrl ->
                            onWebViewError(
                                HttpURLConnection.HTTP_UNAVAILABLE,
                                "No application found to open a link to ${SdkWebViewClient.describeFailedRequest(failedUrl)}",
                                failedUrl.host,
                            )
                        })
                    }
                )
            },
            onCreated = { webView ->
                webView.setup()
                // Add JavaScript interface for communication between WebView and Android code
                jsBridge?.let { jsInterface ->
                    jsInterface.safeCastAs<SdkJSBridge<*>>()?.setWebViewReference(webView)
                    webView.addJavascriptInterface(
                        jsInterface,
                        MobileSDKConstants.JS_BRIDGE_NAME
                    )
                }
            },
            onDispose = { webView ->
                if (rendererGone) webView.destroy()
            }
        )
    }
}
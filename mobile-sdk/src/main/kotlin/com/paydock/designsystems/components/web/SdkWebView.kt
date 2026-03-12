package com.paydock.designsystems.components.web

import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import com.kevinnzou.web.LoadingState
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberSaveableWebViewState
import com.kevinnzou.web.rememberWebViewNavigator
import com.paydock.core.MobileSDKConstants
import com.paydock.core.extensions.safeCastAs
import com.paydock.core.presentation.extensions.openBrowser
import com.paydock.core.presentation.util.webviewLoadingAccessibility
import com.paydock.designsystems.components.loader.LoaderAppearance
import com.paydock.designsystems.components.loader.LoaderAppearanceDefaults
import com.paydock.designsystems.components.loader.SdkLoader
import com.paydock.designsystems.components.loader.SdkProgressLoader
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
 * @param shouldShowCustomLoader Flag to determine whether to display a custom loading indicator.
 * @param jsBridge The generic JavaScript interface for communication between WebView and Android code.
 * @param onShouldOverrideUrlLoading Custom logic for handling URL loading events.
 * @param onPageFinished  Callback triggered when a page finishes loading in the WebView.
 * @param openTargetBlankInSameWebView When true, links with target="_blank" (e.g. "Forgot password", "Apply now")
 *        load in the current WebView instead of opening a new window. The SDK injects JS (window.open override
 *        and click interception) so navigation works like a single-tab browser. Default is false.
 * @param onWebViewError Callback to handle WebView errors.
 */
@Composable
internal fun <T : Any?> SdkWebView(
    webUrl: String,
    data: String? = null,
    shouldShowCustomLoader: Boolean = true,
    jsBridge: SdkJSBridge<T>? = null,
    loaderAppearance: LoaderAppearance = LoaderAppearanceDefaults.appearance(),
    onCloseRequested: () -> Unit = {},
    onShouldOverrideUrlLoading: ((request: WebResourceRequest?) -> Boolean)? = null,
    onPageFinished: ((WebView) -> Unit)? = null,
    openTargetBlankInSameWebView: Boolean = false,
    onWebViewError: (Int, String) -> Unit,
) {
    // Get the current context
    val context = LocalContext.current

    // Remember the state of the WebView
    val state = rememberSaveableWebViewState()
    val navigator = rememberWebViewNavigator()

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

    // Additional loader to show if we are showing additional popup window
    var showWindowLoader: Boolean by remember {
        mutableStateOf(false)
    }

    val isLoading = state.isLoading || showWindowLoader

    // Display a WebView in a Box composable
    Box(
        contentAlignment = Alignment.Center,
    ) {
        WebView(
            state = state,
            modifier = Modifier.fillMaxSize(),
            navigator = navigator,
            client = remember(openTargetBlankInSameWebView) {
                SdkWebViewClient(
                    onShouldOverrideUrlLoading = onShouldOverrideUrlLoading,
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
                        onPageFinished?.let { callback ->
                            callback(webView)
                        }
                    },
                    onWebViewError = onWebViewError,
                    onCloseRequested = onCloseRequested,
                )
            },
            chromeClient = remember(openTargetBlankInSameWebView) {
                SdkWebChromeClient(
                    context = context,
                    openTargetBlankInSameWebView = openTargetBlankInSameWebView,
                    onOpenWebView = { showWindowLoader = true },
                    onPageFinished = { webView ->
                        showWindowLoader = false
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
                        onPageFinished?.let { callback ->
                            callback(webView)
                        }
                    },
                    onWebViewError = onWebViewError,
                    onCloseRequested = onCloseRequested,
                    openExternalLink = { url ->
                        context.openBrowser(uri = url, onError = { failedUrl ->
                            onWebViewError(
                                HttpURLConnection.HTTP_UNAVAILABLE,
                                "No application found to open URL: $failedUrl"
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
            }
        )

        // Display a loading indicator if the WebView is still loading
        // Wrap loader in full-screen Box with accessibility semantics so tapping anywhere reads "Loading"
        if (shouldShowCustomLoader && isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .webviewLoadingAccessibility(isLoading = true)
                    .semantics(mergeDescendants = false) {
                        // mergeDescendants = false ensures our contentDescription from webviewLoadingAccessibility
                        // overrides the default CircularProgressIndicator semantics ("In progress, progress bar")
                    },
                contentAlignment = Alignment.Center
            ) {
                state.safeCastAs<LoadingState.Loading>()?.let {
                    SdkProgressLoader(
                        appearance = loaderAppearance,
                        progress = { (state.loadingState as LoadingState.Loading).progress }
                    )
                } ?: SdkLoader(
                    appearance = loaderAppearance
                )
            }
        }
    }
}
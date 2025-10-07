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
import com.kevinnzou.web.LoadingState
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberSaveableWebViewState
import com.kevinnzou.web.rememberWebViewNavigator
import com.paydock.core.MobileSDKConstants
import com.paydock.core.extensions.safeCastAs
import com.paydock.core.presentation.extensions.openBrowser
import com.paydock.designsystems.components.loader.LoaderAppearance
import com.paydock.designsystems.components.loader.LoaderAppearanceDefaults
import com.paydock.designsystems.components.loader.SdkLoader
import com.paydock.designsystems.components.loader.SdkProgressLoader
import com.paydock.designsystems.components.web.extensions.setup
import com.paydock.designsystems.components.web.utils.SdkJSBridge
import com.paydock.designsystems.components.web.utils.SdkWebChromeClient
import com.paydock.designsystems.components.web.utils.SdkWebViewClient
import java.net.HttpURLConnection

/**
 * Composable function to display a WebView with customizable settings and event handling.
 *
 * @param webUrl The URL to load in the WebView.
 * @param data Optional HTML data to load in the WebView instead of a URL.
 * @param shouldShowCustomLoader Flag to determine whether to display a custom loading indicator.
 * @param jsBridge The generic JavaScript interface for communication between WebView and Android code.
 * @param onShouldOverrideUrlLoading Custom logic for handling URL loading events.
 * @param onPageFinished  Callback triggered when a page finishes loading in the WebView.
 * @param onWebViewError Callback to handle WebView errors.
 */
@Composable
internal fun <T : Any?> SdkWebView(
    webUrl: String,
    data: String? = null,
    shouldShowCustomLoader: Boolean = true,
    jsBridge: SdkJSBridge<T>? = null,
    loaderAppearance: LoaderAppearance = LoaderAppearanceDefaults.appearance(),
    onShouldOverrideUrlLoading: ((request: WebResourceRequest?) -> Boolean)? = null,
    onPageFinished: ((WebView) -> Unit)? = null,
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
    // Display a WebView in a Box composable
    Box(
        contentAlignment = Alignment.Center,
    ) {
        WebView(
            state = state,
            modifier = Modifier
                .fillMaxSize(),
            navigator = navigator,
            client = remember {
                SdkWebViewClient(
                    onShouldOverrideUrlLoading = onShouldOverrideUrlLoading,
                    onPageFinished = { webView ->
                        onPageFinished?.let { callback ->
                            callback(webView)
                        }
                    },
                    onWebViewError = onWebViewError
                )
            },
            chromeClient = remember {
                SdkWebChromeClient(
                    context = context,
                    onOpenWebView = { showWindowLoader = true },
                    onPageFinished = { webView ->
                        showWindowLoader = false
                        onPageFinished?.let { callback ->
                            callback(webView)
                        }
                    },
                    onWebViewError = onWebViewError,
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

        // Display a loading indicator if the WebView is still loading co
        if (shouldShowCustomLoader && (state.isLoading || showWindowLoader)) {
            state.safeCastAs<LoadingState.Loading>()?.let {
                SdkProgressLoader(appearance = loaderAppearance, progress = { (state.loadingState as LoadingState.Loading).progress })
            } ?: SdkLoader(appearance = loaderAppearance)
        }
    }
}
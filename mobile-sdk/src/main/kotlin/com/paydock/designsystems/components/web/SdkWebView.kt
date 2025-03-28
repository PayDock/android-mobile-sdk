package com.paydock.designsystems.components.web

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.kevinnzou.web.LoadingState
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberWebViewNavigator
import com.kevinnzou.web.rememberWebViewState
import com.kevinnzou.web.rememberWebViewStateWithHTMLData
import com.paydock.core.MobileSDKConstants
import com.paydock.core.extensions.safeCastAs
import com.paydock.designsystems.components.web.extensions.setup
import com.paydock.designsystems.components.web.utils.SdkJSBridge
import com.paydock.designsystems.components.web.utils.SdkWebChromeClient
import com.paydock.designsystems.components.web.utils.SdkWebViewClient
import com.paydock.designsystems.theme.Theme

/**
 * Composable function to display a WebView with customizable settings and event handling.
 *
 * @param webUrl The URL to load in the WebView.
 * @param data Optional HTML data to load in the WebView instead of a URL.
 * @param shouldShowCustomLoader Flag to determine whether to display a custom loading indicator.
 * @param jsBridge The generic JavaScript interface for communication between WebView and Android code.
 * @param onShouldOverrideUrlLoading Custom logic for handling URL loading events.
 * @param onWebViewError Callback to handle WebView errors.
 */
@Composable
internal fun <T : Any?> SdkWebView(
    webUrl: String,
    data: String? = null,
    shouldShowCustomLoader: Boolean = true,
    jsBridge: SdkJSBridge<T>? = null,
    onShouldOverrideUrlLoading: ((request: WebResourceRequest?) -> Boolean)? = null,
    onWebViewError: (Int, String) -> Unit,
) {
    // Get the current context
    val context = LocalContext.current

    // Remember the state of the WebView
    val state = if (!data.isNullOrBlank()) {
        rememberWebViewStateWithHTMLData(baseUrl = webUrl, data = data, mimeType = "text/html")
    } else {
        rememberWebViewState(url = webUrl)
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
            modifier = Modifier.fillMaxSize().background(Theme.colors.background),
            navigator = rememberWebViewNavigator(),
            client = remember {
                SdkWebViewClient(
                    onShouldOverrideUrlLoading = onShouldOverrideUrlLoading,
                    onWebViewError = onWebViewError
                )
            },
            chromeClient = remember {
                SdkWebChromeClient(
                    context = context,
                    onOpenWebView = { showWindowLoader = true },
                    onPageFinished = { showWindowLoader = false },
                    onWebViewError = onWebViewError,
                    openExternalLink = { open(context, it) }
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

        // Display a loading indicator if the WebView is still loading content
        if (shouldShowCustomLoader && (state.isLoading || showWindowLoader)) {
            state.safeCastAs<LoadingState.Loading>()?.let {
                CircularProgressIndicator(progress = { (state.loadingState as LoadingState.Loading).progress })
            } ?: CircularProgressIndicator()
        }
    }
}

/**
 * Opens a given URL in the user's default web browser or an appropriate app that can handle the intent.
 * If no app is found to handle the intent, the method silently catches the exception.
 *
 * @param context The context from which the intent is launched.
 * @param url The URL to be opened in the web browser.
 */
private fun open(context: Context, url: Uri) {
    val intent = Intent(Intent.ACTION_VIEW, url)
    try {
        context.startActivity(intent) // Attempt to open the URL
    } catch (_: ActivityNotFoundException) {
        // Silently catch the exception if no activity can handle the intent
    }
}
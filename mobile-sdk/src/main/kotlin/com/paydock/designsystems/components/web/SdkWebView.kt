package com.paydock.designsystems.components.web

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Message
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
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
import com.kevinnzou.web.AccompanistWebChromeClient
import com.kevinnzou.web.AccompanistWebViewClient
import com.kevinnzou.web.LoadingState
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberWebViewNavigator
import com.kevinnzou.web.rememberWebViewState
import com.kevinnzou.web.rememberWebViewStateWithHTMLData
import com.paydock.BuildConfig
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.injection.modules.provideJson
import com.paydock.designsystems.components.web.utils.SdkJSBridge

/**
 * Composable function to display a WebView with customizable settings and event handling.
 *
 * @param webUrl The URL to load in the WebView.
 * @param data Optional HTML data to load in the WebView instead of a URL.
 * @param showLoader Flag to determine whether to display a loading indicator.
 * @param jsBridge The generic JavaScript interface for communication between WebView and Android code.
 * @param onShouldOverrideUrlLoading Custom logic for handling URL loading events.
 * @param onWebViewError Callback to handle WebView errors.
 */
@Suppress("LongMethod")
@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun <T : Any?> SdkWebView(
    webUrl: String,
    data: String? = null,
    showLoader: Boolean = true,
    jsBridge: SdkJSBridge<T>? = null,
    onShouldOverrideUrlLoading: ((request: WebResourceRequest?) -> Boolean?)? = null,
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

    // Remember the navigator for the WebView
    val navigator = rememberWebViewNavigator()

    // Get the loading state of the WebView
    val loadingState = state.loadingState

    // Display a WebView in a Box composable
    Box(
        contentAlignment = Alignment.Center,
    ) {
        // A custom WebViewClient and WebChromeClient can be provided via subclassing
        val webClient = remember {
            object : AccompanistWebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return onShouldOverrideUrlLoading?.let {
                        it(request)
                    } ?: super.shouldOverrideUrlLoading(view, request)
                }

                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    // Handle error here\
                    if (error != null) {
                        // TODO - Perhaps look if there is a way to better handle WebView errors.
                        // Determine what is a critical vs non-fatal error
                        // ie. Some instances will throw errors (eg. Mastercard SRC) but it is not critical.
                        val errorMessage = getWebViewErrorMessage(error.errorCode)
                        Log.d(MobileSDKConstants.MOBILE_SDK_TAG, errorMessage)
                        // This is a fallback case in the event that cookies are causing the issue
                        if (CookieManager.getInstance().hasCookies()) {
                            CookieManager.getInstance().removeAllCookies { cleared ->
                                if (cleared) {
                                    view.reload()
                                }
                            }
                        } else {
                            onWebViewError(error.errorCode, errorMessage)
                        }
                    }
                }
            }
        }

        val webChromeClient = remember {
            object : AccompanistWebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                    return if (BuildConfig.DEV_BUILD) {
                        val message = consoleMessage.message()
                        val formattedMessage = formatMessage(message)
                        Log.d(MobileSDKConstants.MOBILE_SDK_TAG, formattedMessage)
                        true
                    } else super.onConsoleMessage(consoleMessage)
                }

                override fun onCloseWindow(window: WebView?) {
                    super.onCloseWindow(window)
                }

                override fun onCreateWindow(
                    view: WebView?,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message?
                ): Boolean {
                    showWindowLoader = true
                    val newWebView = createNewWebView(context)
                    newWebView.webViewClient = object : WebViewClient() {

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            showWindowLoader = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            showWindowLoader = false
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            // Handle navigation back to the original WebView
                            view?.loadUrl(request?.url.toString())
                            return true
                        }
                    }
                    newWebView.webChromeClient = object : WebChromeClient() {
                        override fun onCloseWindow(window: WebView) {
                            super.onCloseWindow(window)
                            view?.removeView(window)
                        }
                    }
                    view?.addView(newWebView)
                    val transport = resultMsg?.obj as WebView.WebViewTransport
                    transport.webView = newWebView
                    resultMsg.sendToTarget()
                    return true
                }
            }
        }

        // Display the WebView
        WebView(
            state = state,
            modifier = Modifier.fillMaxSize(),
            navigator = navigator,
            client = webClient,
            chromeClient = webChromeClient,
            factory = { context ->
                createNewWebView(context).apply {
                    // Add JavaScript interface for communication between WebView and Android code
                    jsBridge?.let { jsInterface ->
                        addJavascriptInterface(
                            jsInterface,
                            MobileSDKConstants.JS_BRIDGE_NAME
                        )
                    }
                }
            }
        )

        // Display a loading indicator if the WebView is still loading content
        if (showLoader && (loadingState is LoadingState.Loading || showWindowLoader)) {
            if (loadingState is LoadingState.Loading) {
                CircularProgressIndicator(progress = { loadingState.progress })
            } else {
                CircularProgressIndicator()
            }
        }
    }
}

/**
 * Formats the console message to provide better readability for object-based messages.
 * If the message contains a JSON-like object, it attempts to parse and format it as a JSON string.
 * Otherwise, it leaves the message as is.
 *
 * @param message The original console message string.
 * @return A formatted string with JSON objects prettified if applicable.
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
private fun formatMessage(message: String): String {
    // Split the message into two parts using the delimiter ":::"
    val parts = message.split(":::")

    // Check if the message was successfully split into exactly two parts
    return if (parts.size == 2) {
        val prefix = parts[0] // The part before ":::"
        val possibleJsonString = parts[1].trim() // The part after ":::", trimmed of any surrounding whitespace

        // Try to parse the possible JSON string and format it
        val formattedJson = try {
            // Use kotlinx.serialization to parse the string as a JSON element
            provideJson().parseToJsonElement(possibleJsonString).toString()
        } catch (e: Exception) {
            // If parsing fails, leave the string as is
            possibleJsonString
        }

        // Return the combined formatted message
        "$prefix ::: $formattedJson"
    } else {
        // If the message doesn't contain ":::", return it as is
        message
    }
}

/**
 * Provides user-friendly error messages based on the WebView error code.
 *
 * @param errorCode The error code returned by the WebView.
 * @return A user-friendly error message corresponding to the error code.
 */
@Suppress("CyclomaticComplexMethod")
private fun getWebViewErrorMessage(errorCode: Int): String = when (errorCode) {
    WebViewClient.ERROR_AUTHENTICATION -> "User authentication failed. Please check your credentials and try again."
    WebViewClient.ERROR_TIMEOUT -> "The server is taking too much time to respond. Please try again later."
    WebViewClient.ERROR_TOO_MANY_REQUESTS -> "Too many requests. Please try again later."
    WebViewClient.ERROR_UNKNOWN -> "An unknown error occurred. Please try again later."
    WebViewClient.ERROR_BAD_URL -> "The URL you entered is not valid. Please check the URL and try again."
    WebViewClient.ERROR_CONNECT -> "Failed to connect to the server. Please check your internet connection and try again."
    WebViewClient.ERROR_FAILED_SSL_HANDSHAKE -> "Failed to establish a secure connection to the server."
    WebViewClient.ERROR_HOST_LOOKUP -> "Failed to lookup server hostname. Please check your internet connection and try again."
    WebViewClient.ERROR_PROXY_AUTHENTICATION -> "Proxy authentication failed. Please check your proxy credentials and try again."
    WebViewClient.ERROR_REDIRECT_LOOP -> "Too many redirects. Please try again later."
    WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME -> "Unsupported authentication scheme. Please try again later."
    WebViewClient.ERROR_UNSUPPORTED_SCHEME -> "Unsupported URL scheme. Please try again later."
    WebViewClient.ERROR_FILE -> "File-related error. Please try again later."
    WebViewClient.ERROR_FILE_NOT_FOUND -> "File not found. Please try again later."
    WebViewClient.ERROR_IO -> "The server failed to communicate. Please try again later."
    else -> "An unknown error occurred. Please try again later."
}

/**
 * Creates a new WebView instance with required settings.
 *
 * @param context The context to create the WebView.
 * @return New WebView instance.
 */
@SuppressLint("SetJavaScriptEnabled")
private fun createNewWebView(context: Context): WebView {
    return WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
        }
        // Set this to true to enable debugging of Javascript SDK inside Webview.
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEV_BUILD)
        // Enable third-party cookies for faster checkout of returning users
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
    }
}
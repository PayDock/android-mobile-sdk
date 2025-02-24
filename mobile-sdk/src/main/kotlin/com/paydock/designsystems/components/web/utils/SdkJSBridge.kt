package com.paydock.designsystems.components.web.utils

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.paydock.core.MobileSDKConstants

/**
 * Abstract class representing a JavaScript bridge for communication between WebView and Android code.
 * Child classes must implement the [postMessage] method to handle JavaScript messages.
 *
 * @param T The type of the result expected from JavaScript messages.
 * @property eventCallback Callback function to be invoked with the result from JavaScript.
 */
internal abstract class SdkJSBridge<T>(protected val eventCallback: (T) -> Unit) {

    private var webView: WebView? = null
    protected var isEnabled = true

    /**
     * Abstract method to be implemented by child classes for handling JavaScript messages.
     *
     * @param eventJson The JSON string representing an event.
     */
    @JavascriptInterface
    abstract fun postMessage(eventJson: String)

    /**
     * Sets the WebView reference for this bridge.
     *
     * @param webView The WebView instance to be used for communication.
     */
    fun setWebViewReference(webView: WebView) {
        this.webView = webView
    }

    /**
     * Removes the JavaScript interface from the WebView.
     *
     * This method is called when an error occurs during event processing to prevent further
     * communication attempts. It ensures that the bridge is properly disconnected from the WebView.
     */
    protected fun removeBridgeFromWebView() {
        webView?.let {
            it.removeJavascriptInterface(MobileSDKConstants.JS_BRIDGE_NAME)
            this.webView = null
        }
    }
}

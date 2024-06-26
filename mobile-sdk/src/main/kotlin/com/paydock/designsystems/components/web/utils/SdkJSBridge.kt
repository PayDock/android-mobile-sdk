package com.paydock.designsystems.components.web.utils

import android.webkit.JavascriptInterface

/**
 * Abstract class representing a JavaScript bridge for communication between WebView and Android code.
 * Child classes must implement the [postMessage] method to handle JavaScript messages.
 *
 * @param T The type of the result expected from JavaScript messages.
 * @property onResultCallback Callback function to be invoked with the result from JavaScript.
 */
internal abstract class SdkJSBridge<T>(protected val onResultCallback: (T) -> Unit) {

    /**
     * Abstract method to be implemented by child classes for handling JavaScript messages.
     *
     * @param eventJson The JSON string representing an event.
     */
    @JavascriptInterface
    abstract fun postMessage(eventJson: String)
}

package com.paydock.feature.colespay.presentation.utils

import android.webkit.JavascriptInterface
import com.paydock.designsystems.components.web.utils.SdkJSBridge

/**
 * Bridge class responsible for handling JavaScript messages sent to the Coles Pay widget.
 *
 * @param eventCallback Callback function to pass back Coles Pay events.
 */
internal class ColesPayJSBridge(
    eventCallback: (String) -> Unit
) : SdkJSBridge<String>(eventCallback) {

    /**
     * Receives and processes messages sent from JavaScript to the Android WebView.
     *
     * @param eventJson The JSON string representing the JavaScript event.
     */
    @JavascriptInterface
    override fun postMessage(eventJson: String) {
        eventCallback(eventJson)
    }
}
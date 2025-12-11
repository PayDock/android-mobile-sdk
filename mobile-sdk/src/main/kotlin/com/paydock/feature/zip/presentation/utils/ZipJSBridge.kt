package com.paydock.feature.zip.presentation.utils

import android.webkit.JavascriptInterface
import com.paydock.designsystems.components.web.utils.SdkJSBridge

/**
 * Bridge class responsible for handling JavaScript messages sent from the Zip checkout WebView.
 *
 * This bridge intercepts URL changes and navigation events from the Zip checkout flow,
 * allowing the native code to detect when the user has completed or cancelled the checkout.
 *
 * @param eventCallback Callback function to pass back URL change events.
 */
internal class ZipJSBridge(
    eventCallback: (String) -> Unit
) : SdkJSBridge<String>(eventCallback) {

    /**
     * Receives and processes messages sent from JavaScript to the Android WebView.
     *
     * This method is called when the injected JavaScript detects a URL change
     * (via history API or other navigation).
     *
     * @param eventJson The URL string representing the current page location.
     */
    @JavascriptInterface
    override fun postMessage(eventJson: String) {
        eventCallback(eventJson)
    }
}

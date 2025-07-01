package com.paydock.designsystems.components.web.extensions

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import com.paydock.MobileSDK

/**
 * Extension function to set up the WebView with the necessary configurations for SDK's web-based operations.
 * This method applies various settings to ensure the WebView supports JavaScript, multiple windows,
 * and enables debugging for development builds. Additionally, it allows third-party cookies to support faster
 * checkout for returning users.
 *
 * @receiver WebView The WebView instance to be configured.
 */
@SuppressLint("SetJavaScriptEnabled")
internal fun WebView.setup() {
    // Set the layout parameters to match the parent view's size
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    // Configure WebView settings
    settings.apply {
        javaScriptEnabled = true // Enable JavaScript execution
        domStorageEnabled = true // Enable DOM storage API
        javaScriptCanOpenWindowsAutomatically = true // Allow JavaScript to open new windows
        setSupportMultipleWindows(true) // Support multiple windows in the WebView
    }

    // Enable debugging in development builds to assist with JavaScript debugging
    WebView.setWebContentsDebuggingEnabled(MobileSDK.getInstance().enableTestMode)

    // Enable third-party cookies to improve the user experience, allowing faster checkout for returning users
    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
}
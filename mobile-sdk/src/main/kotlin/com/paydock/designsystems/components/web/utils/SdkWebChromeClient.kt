package com.paydock.designsystems.components.web.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.kevinnzou.web.AccompanistWebChromeClient
import com.paydock.MobileSDK
import com.paydock.core.MobileSDKConstants
import com.paydock.core.network.NetworkClientBuilder
import com.paydock.designsystems.components.web.extensions.setup

/**
 * Custom WebChromeClient for handling various WebView-related actions, including opening new windows,
 * handling console messages, and managing external link redirection. This class extends AccompanistWebChromeClient
 * and provides additional functionality for managing WebView creation, errors, and external links.
 *
 * @param context The context in which the WebView is operating.
 * @param onOpenWebView Callback triggered when a new WebView is created and opened.
 * @param onPageFinished Callback triggered when a page finishes loading in the WebView.
 * @param onWebViewError Callback triggered when a WebView encounters an error, providing the error code and message.
 * @param openExternalLink Callback triggered when an external link is encountered and needs to be opened outside of the WebView.
 */
internal class SdkWebChromeClient(
    private val context: Context,
    private val onOpenWebView: (WebView) -> Unit,
    private val onPageFinished: (WebView) -> Unit,
    private val onWebViewError: (Int, String) -> Unit,
    private val openExternalLink: (Uri) -> Unit
) : AccompanistWebChromeClient() {

    companion object {
        const val URL_KEY = "url"
    }

    /**
     * Handles console messages from the WebView.
     *
     * This method logs messages to the console with a custom tag and formatting.
     * It also handles different message levels (ERROR, WARNING, LOG, DEBUG, TIP)
     * and provides custom error handling.
     *
     * @param consoleMessage The message object from the WebView console.
     * @return True if the message was handled by this method, false otherwise.
     */
    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        val messageLevel = consoleMessage.messageLevel()
        val message = formatMessage(consoleMessage.message())
        val sourceId = consoleMessage.sourceId()
        val lineNumber = consoleMessage.lineNumber()

        // Handle specific message levels
        return if (MobileSDK.getInstance().enableTestMode) {
            // Log the message with appropriate level and details
            logConsoleMessage(messageLevel, message, sourceId, lineNumber)
            true
        } else {
            // In non-development builds, use the default behavior
            super.onConsoleMessage(consoleMessage)
        }
    }

    /**
     * Logs a console message with the appropriate log level and details.
     *
     * @param messageLevel The level of the console message.
     * @param message The message content.
     * @param sourceId The source ID of the message.
     * @param lineNumber The line number of the message.
     */
    private fun logConsoleMessage(
        messageLevel: ConsoleMessage.MessageLevel,
        message: String,
        sourceId: String?,
        lineNumber: Int
    ) {
        val logMessage = "$messageLevel: $message at $sourceId:$lineNumber"
        when (messageLevel) {
            ConsoleMessage.MessageLevel.ERROR -> Log.e(
                MobileSDKConstants.MOBILE_SDK_TAG,
                logMessage
            )

            ConsoleMessage.MessageLevel.WARNING -> Log.w(
                MobileSDKConstants.MOBILE_SDK_TAG,
                logMessage
            )

            ConsoleMessage.MessageLevel.LOG -> Log.i(MobileSDKConstants.MOBILE_SDK_TAG, logMessage)
            ConsoleMessage.MessageLevel.DEBUG -> Log.d(
                MobileSDKConstants.MOBILE_SDK_TAG,
                logMessage
            )

            ConsoleMessage.MessageLevel.TIP -> Log.i(MobileSDKConstants.MOBILE_SDK_TAG, logMessage)
            else -> Log.v(
                MobileSDKConstants.MOBILE_SDK_TAG,
                logMessage
            ) // Verbose for unknown levels
        }
    }

    /**
     * Called when a request is made to open a new window within the WebView. It creates a new WebView
     * and sets up its client and chrome client. If an external link is encountered, it triggers the
     * [openExternalLink] callback to open the link outside the WebView.
     *
     * @param view The WebView that initiated the request for a new window.
     * @param isDialog Whether the new window is a dialog.
     * @param isUserGesture Whether the new window request was initiated by a user gesture.
     * @param resultMsg A Message to be sent when the new window is created.
     * @return True if the new window is successfully created, false otherwise.
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?,
    ): Boolean {
        // Create a new WebView instance for the new window.
        val webView = WebView(context).apply {
            visibility = View.INVISIBLE
            setup() // Setup method configures WebView settings (e.g., enabling JavaScript).
        }

        // Set WebViewClient to handle page load and error events.
        webView.webViewClient = SdkWebViewClient(onPageFinished = {
            webView.visibility = View.VISIBLE
            onPageFinished(webView)
        }, onWebViewError = onWebViewError)

        // Set WebChromeClient to manage window operations.
        webView.webChromeClient = object : WebChromeClient() {
            /**
             * Handles the creation of a new window within the WebView.
             * If a URL is provided, it opens the external link outside the WebView.
             */
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?,
            ): Boolean {
                // Retrieve the URL associated with the new window request.
                val hrefMessage = view?.handler?.obtainMessage()
                view?.requestFocusNodeHref(hrefMessage)

                val url = hrefMessage?.data?.getString(URL_KEY)
                url?.let { openExternalLink(Uri.parse(it)) }

                return false
            }

            override fun onCloseWindow(window: WebView?) {
                super.onCloseWindow(window)
            }
        }

        // Add the new WebView to the parent WebView's view hierarchy.
        view?.addView(webView)

        // Set the newly created WebView to be the target of the result message.
        val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
        transport.webView = webView
        resultMsg.sendToTarget()

        // Trigger the callback indicating that a new WebView has been opened.
        onOpenWebView(webView)

        return true
    }

    /**
     * Handles the closing of a window within the WebView. Calls the super implementation
     * of [onCloseWindow] and performs any additional cleanup if needed.
     *
     * @param window The WebView that is being closed.
     */
    override fun onCloseWindow(window: WebView?) {
        super.onCloseWindow(window)
    }

    /**
     * Formats the console message to provide better readability for object-based messages.
     * If the message contains a JSON-like object, it attempts to parse and format it as a JSON string.
     * Otherwise, it leaves the message as is.
     *
     * @param message The original console message string.
     * @return A formatted string with JSON objects prettified if applicable.
     */
    @Suppress("TooGenericExceptionCaught")
    private fun formatMessage(message: String): String {
        // Split the message into two parts using the delimiter ":::"
        val parts = message.split(":::")

        // Check if the message was successfully split into exactly two parts
        return if (parts.size == 2) {
            val prefix = parts[0] // The part before ":::"
            val possibleJsonString =
                parts[1].trim() // The part after ":::", trimmed of any surrounding whitespace

            // Try to parse the possible JSON string and format it
            val formattedJson = try {
                // Use kotlinx.serialization to parse the string as a JSON element
                NetworkClientBuilder.getNetworkJson().parseToJsonElement(possibleJsonString)
                    .toString()
            } catch (_: Exception) {
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
}
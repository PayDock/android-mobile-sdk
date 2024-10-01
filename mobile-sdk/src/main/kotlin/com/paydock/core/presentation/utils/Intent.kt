package com.paydock.core.presentation.utils

import android.content.Intent

/**
 * This object defines common constants used as keys for extras in an Intent.
 */
private object CoreIntent {
    const val WEBVIEW_STATUS = "WEBVIEW_STATUS" // Key for the WebView status code.
    const val WEBVIEW_MESSAGE = "WEBVIEW_MESSAGE" // Key for the WebView failure message.
}

/**
 * Extension function to add the WebView status code to an Intent.
 *
 * @param status The status code to be added, or null to skip adding.
 * @return The updated Intent with the status code, or the same Intent if status is null.
 */
internal fun Intent.putWebViewStatusExtra(status: Int?): Intent =
    status?.let { putExtra(CoreIntent.WEBVIEW_STATUS, status) } ?: this

/**
 * Extension function to retrieve the WebView status code from an Intent.
 *
 * @return The WebView status code, or -1 if not present.
 */
internal fun Intent.getWebViewStatusExtra(): Int =
    getIntExtra(CoreIntent.WEBVIEW_STATUS, -1)

/**
 * Extension function to add the WebView failure message to an Intent.
 *
 * @param message The failure message to be added.
 * @return The updated Intent with the failure message included.
 */
internal fun Intent.putWebViewMessageExtra(message: String): Intent =
    putExtra(CoreIntent.WEBVIEW_MESSAGE, message)

/**
 * Extension function to retrieve the WebView failure message from an Intent.
 *
 * @return The failure message if present, or a default error message if not present.
 */
internal fun Intent.getWebViewMessageExtra(defaultErrorMessage: String): String =
    getStringExtra(CoreIntent.WEBVIEW_MESSAGE) ?: defaultErrorMessage

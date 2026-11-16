package com.paydock.designsystems.components.web.utils

import android.webkit.JavascriptInterface
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * The JavaScript interface registered for widget pages built by [HtmlWidgetBuilder]: it routes the page's
 * [HtmlWidgetBuilder.CLIENT_SDK_LOAD_FAILED_EVENT] report to [onClientSdkLoadFailed] and forwards every other
 * message, unchanged, to the widget's own [delegate] bridge. The widget bridges therefore never see the guard's
 * event.
 *
 * @param delegate The widget's bridge, which handles the widget's events.
 * @param onClientSdkLoadFailed Called on the JavaScript bridge thread when the page reports that the Client SDK
 *   failed to load. The caller moves it to the main thread and ends the session at most once per page.
 */
internal class ClientSdkLoadGuardBridge(
    private val delegate: SdkJSBridge<*>,
    private val onClientSdkLoadFailed: () -> Unit,
) {

    /**
     * Receives every message the page posts over the bridge.
     *
     * @param eventJson The JSON string the page posted.
     */
    @JavascriptInterface
    fun postMessage(eventJson: String) {
        if (isClientSdkLoadFailed(eventJson)) {
            onClientSdkLoadFailed()
        } else {
            delegate.postMessage(eventJson)
        }
    }

    companion object {
        /**
         * Whether [eventJson] is the page's `{"event":"clientSdkLoadFailed"}` report. Anything that is not a JSON
         * object with that event name, including malformed JSON, is not.
         */
        fun isClientSdkLoadFailed(eventJson: String): Boolean = runCatching {
            val event = (Json.parseToJsonElement(eventJson) as? JsonObject)?.get("event") as? JsonPrimitive
            event?.isString == true && event.content == HtmlWidgetBuilder.CLIENT_SDK_LOAD_FAILED_EVENT
        }.getOrDefault(false)
    }
}

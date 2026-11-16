package com.paydock.designsystems.components.web.utils

import android.util.Log
import android.webkit.WebView
import com.paydock.MobileSDK
import com.paydock.core.MobileSDKConstants
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

/**
 * Reads the Client SDK version a widget page loaded.
 *
 * Widgets load an alias of the supported line (or a pinned release), so the version is known only once the script
 * has run: the Client SDK reports it on its global as `window.paydock.version`. After each widget page loads, a
 * reported version is kept on the SDK as [MobileSDK.loadedClientSdkVersion] and logged at debug level.
 */
internal object ClientSdkVersionReader {

    /**
     * The Client SDK's version accessor. It yields `null` or `undefined` when the global is missing (the load
     * guard fired) or the loaded release has no accessor.
     */
    const val VERSION_SCRIPT = "window.paydock && window.paydock.version"

    /**
     * Evaluates [VERSION_SCRIPT] in [webView] and records the version it reports. Call it once the page has
     * finished loading.
     *
     * @param webView The widget's WebView.
     */
    fun read(webView: WebView) {
        webView.evaluateJavascript(VERSION_SCRIPT) { result -> record(parse(result)) }
    }

    /**
     * Decodes an `evaluateJavascript` result, the JSON encoding of the value: only a non-empty JSON string is a
     * version; anything else (`null`, a number, an object, malformed JSON) gives `null`.
     *
     * @param result The JSON-encoded result of [VERSION_SCRIPT].
     * @return The reported version, or `null`.
     */
    fun parse(result: String?): String? = runCatching {
        (Json.parseToJsonElement(result.orEmpty()) as? JsonPrimitive)
            ?.takeIf { it.isString }
            ?.content
            ?.takeIf { it.isNotEmpty() }
    }.getOrNull()

    /**
     * Keeps [version] as the SDK's loaded Client SDK version and logs it at debug level. A `null` version, or an SDK
     * that is not initialised, leaves the last kept value unchanged.
     *
     * @param version The reported version, or `null`.
     */
    fun record(version: String?) {
        if (version == null || !MobileSDK.isInitialised()) return
        MobileSDK.getInstance().loadedClientSdkVersion = version
        Log.d(MobileSDKConstants.MOBILE_SDK_TAG, "Client SDK version loaded: $version")
    }
}

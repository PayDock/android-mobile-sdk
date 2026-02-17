package com.paydock.feature.threeDS.integrated.presentation.utils

import android.util.Log
import android.webkit.JavascriptInterface
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.MPGS3dsException
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.designsystems.components.web.utils.SdkJSBridge
import com.paydock.feature.threeDS.integrated.domain.model.ui.MPGS3dsEvent

/**
 * JavaScript bridge for handling MPGS 3D Secure (3DS) events in a WebView flow.
 *
 * This class facilitates communication between the JavaScript running in a WebView and the native Android app.
 * It listens for messages from JavaScript, parses the JSON payload into a [MPGS3dsEvent], and then
 * passes it to the provided [eventCallback] for further handling.
 *
 * This bridge supports event handling for MPGS 3DS flows, including authentication success,
 * rejection, and additional data collection results.
 *
 * @param eventCallback A callback function that receives and processes the parsed [MPGS3dsEvent].
 */
internal class MPGS3dsJSBridge(eventCallback: (Result<MPGS3dsEvent>) -> Unit) :
    SdkJSBridge<Result<MPGS3dsEvent>>(eventCallback) {

    /**
     * Receives messages from the JavaScript side (webview) representing 3DS events.
     *
     * This function is annotated with `@JavascriptInterface`, allowing it to be called directly
     * from JavaScript code within the WebView. It handles incoming JSON strings representing
     * 3DS events and dispatches them to the provided callback.
     *
     * @param eventJson The JSON string representing the 3DS event. This string must conform to the
     *                  structure expected by the [MPGS3dsEvent] data class. Any
     *                  deviation from this structure will result in an error.
     *
     * **Functionality:**
     *
     * 1. **Bridge State Check:**
     *    - Before processing, it checks if the communication bridge is enabled (`isEnabled`).
     *    - If disabled, it logs a warning and exits immediately, preventing further processing. This
     *      prevents the processing of stale or irrelevant events.
     *
     * 2. **JSON Deserialization:**
     *    - Attempts to convert the `eventJson` string into a [MPGS3dsEvent] object.
     *    - It uses the `convertToDataClass` extension function for this deserialization.
     *    - Employs `runCatching` to encapsulate the deserialization process and handle potential
     *      exceptions gracefully.
     *
     * 3. **Successful Event Handling:**
     *    - If deserialization succeeds, the resulting [MPGS3dsEvent] object is
     *      wrapped in a `Result.success` and then passed to the `eventCallback` function. This indicates
     *      successful event processing.
     *
     * 4. **Failed Event Handling:**
     *    - If deserialization fails (e.g., due to invalid JSON format), the following steps occur:
     *      - **Bridge Deactivation:** The `isEnabled` flag is set to `false`, effectively disabling the
     *        bridge. This prevents subsequent events from being processed, as an error state has been
     *        detected.
     *      - **Error Message Construction:** An informative error message is created, detailing the
     *        failure type (the exception class name) and the specific error message from the exception.
     *      - **Error Reporting:** The error is packaged within a `Result.failure
     **/
    @JavascriptInterface
    override fun postMessage(eventJson: String) {
        if (!isEnabled) {
            Log.w(MobileSDKConstants.MOBILE_SDK_TAG, "Ignoring event because bridge is disabled")
            return
        }
        runCatching {
            eventJson.convertToDataClass<MPGS3dsEvent>()
        }.onSuccess { event ->
            eventCallback(Result.success(event))
        }.onFailure { e ->
            isEnabled = false
            val errorMessage =
                "MPGS3dsEvent Mapping Failure [${e::class.simpleName}]: ${e.message}"
            Log.e(MobileSDKConstants.MOBILE_SDK_TAG, errorMessage, e)
            eventCallback(Result.failure(MPGS3dsException.EventMappingException(errorMessage)))
            removeBridgeFromWebView()
        }
    }

}
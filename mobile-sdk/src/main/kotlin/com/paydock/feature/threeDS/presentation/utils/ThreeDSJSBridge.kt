package com.paydock.feature.threeDS.presentation.utils

import android.webkit.JavascriptInterface
import com.paydock.core.MobileSDKConstants
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.designsystems.components.web.utils.SdkJSBridge
import com.paydock.feature.threeDS.presentation.model.Charge3dsEvent
import com.paydock.feature.threeDS.presentation.model.ChargeError
import com.paydock.feature.threeDS.presentation.model.ChargeErrorEventData
import com.paydock.feature.threeDS.presentation.model.ThreeDSEvent
import kotlinx.serialization.SerializationException

/**
 * Represents a JavaScript bridge for handling 3D Secure (3DS) events in a WebView.
 *
 * This class is responsible for receiving messages from JavaScript code running in a WebView,
 * parsing the JSON payload into a [Charge3dsEvent], and then converting it into a [ThreeDSEvent].
 * The converted result is passed to the provided [onResultCallback].
 *
 * @param onResultCallback A callback function to handle the resulting [ThreeDSEvent].
 */
internal class ThreeDSJSBridge(onResultCallback: (ThreeDSEvent) -> Unit) :
    SdkJSBridge<ThreeDSEvent>(onResultCallback) {

    /**
     * Callback method invoked by JavaScript code to post a 3DS event message.
     *
     * This method is annotated with [JavascriptInterface] to make it accessible from JavaScript code.
     * It receives a JSON string representing a [Charge3dsEvent], converts it to the corresponding
     * data class, and then constructs a [ThreeDSEvent] to be passed to the [onResultCallback].
     *
     * @param eventJson The JSON string representing a 3DS event.
     */
    @JavascriptInterface
    override fun postMessage(eventJson: String) {
        try {
            val event = eventJson.convertToDataClass<ThreeDSEvent>()
            onResultCallback(event)
        } catch (e: SerializationException) {
            // Handle decoding-specific errors by creating a CheckoutErrorEvent with ChargeError
            onResultCallback(
                ThreeDSEvent.ChargeErrorEvent(
                    data = ChargeErrorEventData(
                        error = ChargeError(
                            message = e.message ?: MobileSDKConstants.Errors.THREE_DS_ERROR
                        )
                    )
                )
            )
        } catch (e: IllegalArgumentException) {
            // Handle invalid input errors by creating a CheckoutErrorEvent with ChargeError
            onResultCallback(
                ThreeDSEvent.ChargeErrorEvent(
                    data = ChargeErrorEventData(
                        error = ChargeError(
                            message = e.message ?: MobileSDKConstants.Errors.THREE_DS_ERROR
                        )
                    )
                )
            )
        }
    }
}

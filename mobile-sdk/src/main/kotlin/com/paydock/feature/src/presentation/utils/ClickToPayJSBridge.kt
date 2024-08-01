package com.paydock.feature.src.presentation.utils

import android.webkit.JavascriptInterface
import com.paydock.core.MobileSDKConstants
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.designsystems.components.web.utils.SdkJSBridge
import com.paydock.feature.src.presentation.model.ClickToPayEvent
import com.paydock.feature.src.presentation.model.ErrorData
import com.paydock.feature.src.presentation.model.enum.EventDataType
import kotlinx.serialization.SerializationException

/**
 * Bridge class responsible for handling JavaScript messages sent to the Click to Pay widget.
 *
 * @param onResultCallback Callback function to handle the processed Click to Pay events.
 */
internal class ClickToPayJSBridge(
    onResultCallback: (ClickToPayEvent) -> Unit
) : SdkJSBridge<ClickToPayEvent>(onResultCallback) {

    /**
     * Receives and processes messages sent from JavaScript to the Android WebView.
     *
     * @param eventJson The JSON string representing the JavaScript event.
     */
    @JavascriptInterface
    override fun postMessage(eventJson: String) {
        try {
            val event = eventJson.convertToDataClass<ClickToPayEvent>()
            onResultCallback(event)
        } catch (e: SerializationException) {
            // Handle decoding-specific errors by creating a CheckoutErrorEvent with CriticalErrorData
            onResultCallback(
                ClickToPayEvent.CheckoutErrorEvent(
                    data = ErrorData.CriticalErrorData(
                        type = EventDataType.CRITICAL_ERROR,
                        data = e.message ?: MobileSDKConstants.Errors.SERIALIZATION_ERROR
                    )
                )
            )
        } catch (e: IllegalArgumentException) {
            // Handle invalid input errors by creating a CheckoutErrorEvent with CriticalErrorData
            onResultCallback(
                ClickToPayEvent.CheckoutErrorEvent(
                    data = ErrorData.CriticalErrorData(
                        type = EventDataType.CRITICAL_ERROR,
                        data = e.message ?: MobileSDKConstants.Errors.CLICK_TO_PAY_ERROR
                    )
                )
            )
        }
    }
}

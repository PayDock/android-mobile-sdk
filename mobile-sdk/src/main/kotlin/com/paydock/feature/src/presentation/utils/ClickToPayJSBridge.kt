package com.paydock.feature.src.presentation.utils

import android.webkit.JavascriptInterface
import com.paydock.core.MobileSDKConstants
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.designsystems.components.web.utils.SdkJSBridge
import com.paydock.feature.src.domain.model.ui.ClickToPayEvent
import com.paydock.feature.src.domain.model.ui.ErrorData
import com.paydock.feature.src.domain.model.ui.enums.EventDataType
import kotlinx.serialization.SerializationException

/**
 * Bridge class responsible for handling JavaScript messages sent to the Click to Pay widget.
 *
 * @param eventCallback Callback function to handle the processed Click to Pay events.
 */
internal class ClickToPayJSBridge(
    eventCallback: (ClickToPayEvent) -> Unit
) : SdkJSBridge<ClickToPayEvent>(eventCallback) {

    /**
     * Receives and processes messages sent from JavaScript to the Android WebView.
     *
     * @param eventJson The JSON string representing the JavaScript event.
     */
    @JavascriptInterface
    override fun postMessage(eventJson: String) {
        try {
            val event = eventJson.convertToDataClass<ClickToPayEvent>()
            eventCallback(event)
        } catch (e: SerializationException) {
            // Handle decoding-specific errors by creating a CheckoutErrorEvent with CriticalErrorData
            eventCallback(
                ClickToPayEvent.CheckoutErrorEvent(
                    data = ErrorData.CriticalErrorData(
                        type = EventDataType.CRITICAL_ERROR,
                        data = e.message ?: MobileSDKConstants.General.Errors.SERIALIZATION_ERROR
                    )
                )
            )
        } catch (e: IllegalArgumentException) {
            // Handle invalid input errors by creating a CheckoutErrorEvent with CriticalErrorData
            eventCallback(
                ClickToPayEvent.CheckoutErrorEvent(
                    data = ErrorData.CriticalErrorData(
                        type = EventDataType.CRITICAL_ERROR,
                        data = e.message ?: MobileSDKConstants.ClickToPayConfig.Errors.CLICK_TO_PAY_ERROR
                    )
                )
            )
        }
    }
}

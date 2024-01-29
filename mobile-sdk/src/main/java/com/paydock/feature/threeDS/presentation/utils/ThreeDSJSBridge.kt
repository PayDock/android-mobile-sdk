/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 4:15 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paydock.feature.threeDS.presentation.utils

import android.webkit.JavascriptInterface
import com.paydock.core.extensions.convertToDataClass
import com.paydock.designsystems.components.web.utils.SdkJSBridge
import com.paydock.feature.threeDS.domain.model.EventType
import com.paydock.feature.threeDS.domain.model.ThreeDSResult
import com.paydock.feature.threeDS.presentation.model.Charge3dsEvent

/**
 * Represents a JavaScript bridge for handling 3D Secure (3DS) events in a WebView.
 *
 * This class is responsible for receiving messages from JavaScript code running in a WebView,
 * parsing the JSON payload into a [Charge3dsEvent], and then converting it into a [ThreeDSResult].
 * The converted result is passed to the provided [onResultCallback].
 *
 * @param onResultCallback A callback function to handle the resulting [ThreeDSResult].
 */
internal class ThreeDSJSBridge(onResultCallback: (ThreeDSResult) -> Unit) :
    SdkJSBridge<ThreeDSResult>(onResultCallback) {

    /**
     * Callback method invoked by JavaScript code to post a 3DS event message.
     *
     * This method is annotated with [JavascriptInterface] to make it accessible from JavaScript code.
     * It receives a JSON string representing a [Charge3dsEvent], converts it to the corresponding
     * data class, and then constructs a [ThreeDSResult] to be passed to the [onResultCallback].
     *
     * @param eventJson The JSON string representing a 3DS event.
     */
    @JavascriptInterface
    override fun postMessage(eventJson: String) {
        // Convert the JSON string to a Charge3dsEvent data class
        val chargeEvent = eventJson.convertToDataClass<Charge3dsEvent>()

        // Get the EventType enum based on the event name
        val eventType = EventType.byNameIgnoreCaseOrNull(chargeEvent.event)

        // Create a ThreeDSResult and invoke the callback on all events
        onResultCallback(
            ThreeDSResult(
                event = eventType,
                charge3dsId = chargeEvent.charge3dsId
            )
        )
    }
}

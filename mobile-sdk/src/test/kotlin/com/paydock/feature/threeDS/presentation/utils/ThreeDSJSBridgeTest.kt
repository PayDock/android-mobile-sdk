package com.paydock.feature.threeDS.presentation.utils

import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.threeDS.presentation.model.ChargeError
import com.paydock.feature.threeDS.presentation.model.ChargeErrorEventData
import com.paydock.feature.threeDS.presentation.model.ThreeDSEvent
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class ThreeDSJSBridgeTest {

    private lateinit var mockCallback: (ThreeDSEvent) -> Unit
    private lateinit var jsBridge: ThreeDSJSBridge

    @Before
    fun setup() {
        mockCallback = mock()
        jsBridge = ThreeDSJSBridge(mockCallback)
    }

    @Suppress("MaxLineLength")
    @Test
    fun `postMessage with valid chargeAuthSuccess JSON should call callback with parsed event`() {
        val eventJson =
            "{\"event\":\"chargeAuthSuccess\",\"data\":{\"event\":\"chargeAuthSuccess\",\"purpose\":\"secure3d\",\"message_source\":\"widget.paydock\",\"ref_id\":\"\",\"widget_id\":\"ba7d839a-a868-4bb0-9763-015451fea9fb\",\"charge_3ds_id\":\"ba7d839a-a868-4bb0-9763-015451fea9fb\",\"status\":\"authenticated\"}}"

        val expectedEvent = eventJson.convertToDataClass<ThreeDSEvent>()

        jsBridge.postMessage(eventJson)

        verify(mockCallback).invoke(expectedEvent)
    }

    @Suppress("MaxLineLength")
    @Test
    fun `postMessage with valid chargeAuthReject JSON should call callback with parsed event`() {
        val eventJson =
            "{\"event\":\"chargeAuthReject\",\"data\":{\"event\":\"chargeAuthReject\",\"purpose\":\"secure3d\",\"message_source\":\"widget.paydock\",\"ref_id\":\"\",\"widget_id\":\"d089f658-e52a-4427-9b29-023b4f38334e\",\"charge_3ds_id\":\"d089f658-e52a-4427-9b29-023b4f38334e\",\"status\":\"not_authenticated\"}}"

        val expectedEvent = eventJson.convertToDataClass<ThreeDSEvent>()

        jsBridge.postMessage(eventJson)

        verify(mockCallback).invoke(expectedEvent)
    }

    @Suppress("MaxLineLength")
    @Test
    fun `postMessage with valid chargeAuthChallenge JSON should call callback with parsed event`() {
        val eventJson =
            "{\"event\":\"chargeAuthChallenge\",\"data\":{\"status\":\"pending\",\"charge_3ds_id\":\"7a9aded4-0439-4505-aa88-302fbf29d303\",\"result\":{}}}"

        val expectedEvent = eventJson.convertToDataClass<ThreeDSEvent>()

        jsBridge.postMessage(eventJson)

        verify(mockCallback).invoke(expectedEvent)
    }

    @Test
    fun `postMessage with IllegalArgumentException should call callback with ChargeErrorEvent`() {
        val eventJson = "invalid_argument"

        val expectedEvent = ThreeDSEvent.ChargeErrorEvent(
            data = ChargeErrorEventData(
                error = ChargeError(
                    message = "Element class kotlinx.serialization.json.JsonLiteral is not a JsonObject"
                ),
                charge3dsId = null
            )
        )

        jsBridge.postMessage(eventJson)

        verify(mockCallback).invoke(expectedEvent)
    }
}
package com.paydock.feature.threeDS.integrated.presentation.utils

import com.paydock.core.domain.error.exceptions.Integrated3DSException
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.threeDS.integrated.domain.model.ui.Integrated3DSEvent
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.argThat

@Suppress("MaxLineLength")
internal class Integrated3DSJSBridgeTest {

    private lateinit var mockCallback: (Result<Integrated3DSEvent>) -> Unit
    private lateinit var jsBridge: Integrated3DSJSBridge

    @Before
    fun setup() {
        mockCallback = mock()
        jsBridge = Integrated3DSJSBridge(mockCallback)
    }

    @Test
    fun `postMessage with valid chargeAuthSuccess JSON should call callback with parsed event`() {
        val eventJson =
            "{\"event\":\"chargeAuthSuccess\",\"data\":{\"event\":\"chargeAuthSuccess\",\"purpose\":\"secure3d\",\"message_source\":\"widget.paydock\",\"ref_id\":\"\",\"widget_id\":\"ba7d839a-a868-4bb0-9763-015451fea9fb\",\"charge_3ds_id\":\"ba7d839a-a868-4bb0-9763-015451fea9fb\",\"status\":\"authenticated\"}}"

        val expectedEvent = Result.success(eventJson.convertToDataClass<Integrated3DSEvent>())

        jsBridge.postMessage(eventJson)

        verify(mockCallback).invoke(expectedEvent)
    }

    @Test
    fun `postMessage with valid chargeAuthReject JSON should call callback with parsed event`() {
        val eventJson =
            "{\"event\":\"chargeAuthReject\",\"data\":{\"event\":\"chargeAuthReject\",\"purpose\":\"secure3d\",\"message_source\":\"widget.paydock\",\"ref_id\":\"\",\"widget_id\":\"d089f658-e52a-4427-9b29-023b4f38334e\",\"charge_3ds_id\":\"d089f658-e52a-4427-9b29-023b4f38334e\",\"status\":\"not_authenticated\"}}"

        val expectedEvent = Result.success(eventJson.convertToDataClass<Integrated3DSEvent>())

        jsBridge.postMessage(eventJson)

        verify(mockCallback).invoke(expectedEvent)
    }

    @Test
    fun `postMessage with valid chargeAuth JSON should call callback with parsed event`() {
        val eventJson =
            "{\"event\":\"chargeAuth\",\"data\":{\"event\":\"chargeAuth\",\"purpose\":\"secure3d\",\"message_source\":\"widget.paydock\",\"ref_id\":\"\",\"widget_id\":\"59dc8e83-ebd4-4da4-8952-0cbc9f384d41\",\"charge_3ds_id\":\"59dc8e83-ebd4-4da4-8952-0cbc9f384d41\",\"status\":\"not_authenticated\"}}"

        val expectedEvent = Result.success(eventJson.convertToDataClass<Integrated3DSEvent>())

        jsBridge.postMessage(eventJson)

        verify(mockCallback).invoke(expectedEvent)
    }

    @Test
    fun `postMessage with invalid JSON should call callback with EventMappingException and disable bridge`() {
        val eventJson = "invalid json"
        val message =
            "Integrated3DSEvent Mapping Failure [IllegalArgumentException]: Element class kotlinx.serialization.json.JsonLiteral is not a JsonObject"

        jsBridge.postMessage(eventJson)

        verify(mockCallback).invoke(
            argThat {
                this.isFailure && this.exceptionOrNull() is Integrated3DSException.EventMappingException && this.exceptionOrNull()?.message == message
            }
        )
    }

    @Test
    fun `postMessage with unknown event type should call callback with EventMappingException and disable bridge`() {
        val eventJson =
            "{\"event\":\"unknownEvent\",\"data\":{\"event\":\"unknownEvent\",\"purpose\":\"secure3d\",\"message_source\":\"widget.paydock\",\"ref_id\":\"\",\"widget_id\":\"59dc8e83-ebd4-4da4-8952-0cbc9f384d41\",\"charge_3ds_id\":\"59dc8e83-ebd4-4da4-8952-0cbc9f384d41\",\"status\":\"not_authenticated\"}}"
        val message =
            "Integrated3DSEvent Mapping Failure [SerializationException]: Unknown 3DS event type: [unknownEvent]"

        jsBridge.postMessage(eventJson)

        verify(mockCallback).invoke(
            argThat {
                this.isFailure && this.exceptionOrNull() is Integrated3DSException.EventMappingException && this.exceptionOrNull()?.message == message
            }
        )
    }
}
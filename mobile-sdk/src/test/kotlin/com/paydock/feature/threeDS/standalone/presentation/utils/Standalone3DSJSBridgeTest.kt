package com.paydock.feature.threeDS.standalone.presentation.utils

import com.paydock.core.BaseUnitTest
import com.paydock.core.domain.error.exceptions.Standalone3DSException
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.threeDS.standalone.domain.model.ui.Standalone3DSEvent
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.argThat

@Suppress("MaxLineLength")
internal class Standalone3DSJSBridgeTest : BaseUnitTest() {

    private lateinit var mockCallback: (Result<Standalone3DSEvent>) -> Unit
    private lateinit var jsBridge: Standalone3DSJSBridge

    @Before
    fun setup() {
        mockCallback = mock()
        jsBridge = Standalone3DSJSBridge(mockCallback)
    }

    @Test
    fun `postMessage with valid chargeAuthSuccess JSON should call callback with parsed event`() {
        val eventJson =
            "{\"event\":\"chargeAuthSuccess\",\"data\":{\"status\":\"success\",\"charge_3ds_id\":\"3e07e004-71af-44dc-a9f4-e59520b3e64f\"}}"

        val expectedEvent = Result.success(eventJson.convertToDataClass<Standalone3DSEvent>())

        jsBridge.postMessage(eventJson)

        verify(mockCallback).invoke(expectedEvent)
    }

    @Test
    fun `postMessage with valid chargeAuthReject JSON should call callback with parsed event`() {
        val eventJson =
            "{\"event\":\"chargeAuthReject\",\"data\":{\"status\":\"rejected\",\"charge_3ds_id\":\"b66dc074-5def-4f2a-99f6-58c54c55aabd\",\"result\":{}}}"

        val expectedEvent = Result.success(eventJson.convertToDataClass<Standalone3DSEvent>())

        jsBridge.postMessage(eventJson)

        verify(mockCallback).invoke(expectedEvent)
    }

    @Test
    fun `postMessage with valid chargeAuthChallenge JSON should call callback with parsed event`() {
        val eventJson =
            "{\"event\":\"chargeAuthChallenge\",\"data\":{\"status\":\"pending\",\"charge_3ds_id\":\"3e07e004-71af-44dc-a9f4-e59520b3e64f\",\"result\":{}}}"

        val expectedEvent = Result.success(eventJson.convertToDataClass<Standalone3DSEvent>())

        jsBridge.postMessage(eventJson)

        verify(mockCallback).invoke(expectedEvent)
    }

    @Test
    fun `postMessage with invalid JSON should call callback with EventMappingException and disable bridge`() {
        val eventJson = "invalid json"
        val message =
            "Standalone3DSEvent Mapping Failure [IllegalArgumentException]: Element class kotlinx.serialization.json.JsonLiteral is not a JsonObject"

        jsBridge.postMessage(eventJson)

        verify(mockCallback).invoke(
            argThat {
                this.isFailure && this.exceptionOrNull() is Standalone3DSException.EventMappingException && this.exceptionOrNull()?.message == message
            }
        )
    }

    @Test
    fun `postMessage with unknown event type should call callback with EventMappingException and disable bridge`() {
        val eventJson =
            "{\"event\":\"unknownEvent\",\"data\":{\"status\":\"pending\",\"charge_3ds_id\":\"3e07e004-71af-44dc-a9f4-e59520b3e64f\",\"result\":{}}}"
        val message =
            "Standalone3DSEvent Mapping Failure [SerializationException]: Unknown 3DS event type"

        jsBridge.postMessage(eventJson)

        verify(mockCallback).invoke(
            argThat {
                this.isFailure && this.exceptionOrNull() is Standalone3DSException.EventMappingException && this.exceptionOrNull()?.message == message
            }
        )
    }
}
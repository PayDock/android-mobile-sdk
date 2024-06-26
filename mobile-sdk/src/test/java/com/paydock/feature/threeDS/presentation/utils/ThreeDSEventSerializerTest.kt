package com.paydock.feature.threeDS.presentation.utils

import com.paydock.core.extensions.convertToDataClass
import com.paydock.feature.threeDS.presentation.model.ThreeDSEvent
import com.paydock.feature.threeDS.presentation.model.enum.Event
import kotlinx.serialization.SerializationException
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@Suppress("MaxLineLength")
class ThreeDSEventSerializerTest {

    @Test
    fun `test 3ds data serializer with chargeAuthSuccess`() {
        // Arrange
        val eventJson =
            "{\"event\":\"chargeAuthSuccess\",\"data\":{\"event\":\"chargeAuthSuccess\",\"purpose\":\"secure3d\",\"message_source\":\"widget.paydock\",\"ref_id\":\"\",\"widget_id\":\"ba7d839a-a868-4bb0-9763-015451fea9fb\",\"charge_3ds_id\":\"ba7d839a-a868-4bb0-9763-015451fea9fb\",\"status\":\"authenticated\"}}"

        // Act
        val result = eventJson.convertToDataClass<ThreeDSEvent>()

        // Assert
        assertIs<ThreeDSEvent.ChargeAuthSuccessEvent>(result)
        assertEquals(Event.CHARGE_AUTH_SUCCESS, result.event)
        assertEquals("authenticated", result.data.status)
        assertEquals("ba7d839a-a868-4bb0-9763-015451fea9fb", result.data.charge3dsId)
    }

    @Test
    fun `test 3ds data serializer with chargeAuthReject`() {
        // Arrange
        val eventJson =
            "{\"event\":\"chargeAuthReject\",\"data\":{\"event\":\"chargeAuthReject\",\"purpose\":\"secure3d\",\"message_source\":\"widget.paydock\",\"ref_id\":\"\",\"widget_id\":\"d089f658-e52a-4427-9b29-023b4f38334e\",\"charge_3ds_id\":\"d089f658-e52a-4427-9b29-023b4f38334e\",\"status\":\"not_authenticated\"}}"

        // Act
        val result = eventJson.convertToDataClass<ThreeDSEvent>()

        // Assert
        assertIs<ThreeDSEvent.ChargeAuthRejectEvent>(result)
        assertEquals(Event.CHARGE_AUTH_REJECT, result.event)
        assertEquals("not_authenticated", result.data.status)
        assertEquals("d089f658-e52a-4427-9b29-023b4f38334e", result.data.charge3dsId)
    }

    @Test
    fun `test 3ds data serializer with chargeAuthChallenge`() {
        // Arrange
        val eventJson =
            "{\"event\":\"chargeAuthChallenge\",\"data\":{\"status\":\"pending\",\"charge_3ds_id\":\"7a9aded4-0439-4505-aa88-302fbf29d303\",\"result\":{}}}"

        // Act
        val result = eventJson.convertToDataClass<ThreeDSEvent>()

        // Assert
        assertIs<ThreeDSEvent.ChargeAuthChallengeEvent>(result)
        assertEquals(Event.CHARGE_AUTH_CHALLENGE, result.event)
        assertEquals("pending", result.data.status)
        assertEquals("7a9aded4-0439-4505-aa88-302fbf29d303", result.data.charge3dsId)
    }

    @Test
    fun `test 3ds data serializer with chargeAuthInfo`() {
        // Arrange (not official event json)
        val eventJson =
            "{\"event\":\"chargeAuthInfo\",\"data\":{\"status\":\"auth\",\"charge_3ds_id\":\"7a9aded4-0439-4505-aa88-302fbf29d303\",\"info\":\"authentication info example\"}}"

        // Act
        val result = eventJson.convertToDataClass<ThreeDSEvent>()

        // Assert
        assertIs<ThreeDSEvent.ChargeAuthInfoEvent>(result)
        assertEquals(Event.CHARGE_AUTH_INFO, result.event)
        assertEquals("auth", result.data.status)
        assertEquals("7a9aded4-0439-4505-aa88-302fbf29d303", result.data.charge3dsId)
        assertEquals("authentication info example", result.data.info)
    }

    @Test
    fun `test 3ds data serializer with chargeAuthDecoupled`() {
        // Arrange (not official event json)
        val eventJson =
            "{\"event\":\"chargeAuthDecoupled\",\"data\":{\"status\":\"decoupled\",\"charge_3ds_id\":\"7a9aded4-0439-4505-aa88-302fbf29d303\",\"result\":{\"description\":\"test description example\"}}}"

        // Act
        val result = eventJson.convertToDataClass<ThreeDSEvent>()

        // Assert
        assertIs<ThreeDSEvent.ChargeAuthDecoupledEvent>(result)
        assertEquals(Event.CHARGE_AUTH_DECOUPLED, result.event)
        assertEquals("decoupled", result.data.status)
        assertEquals("7a9aded4-0439-4505-aa88-302fbf29d303", result.data.charge3dsId)
        assertEquals("test description example", result.data.result?.description)
    }

    @Test
    fun `test error data serializer with error serializer`() {
        // Arrange (not official event json)
        val eventJson =
            "{\"event\":\"error\",\"data\":{\"charge_3ds_id\":\"7a9aded4-0439-4505-aa88-302fbf29d303\",\"error\":{\"message\":\"test message example\"}}}"

        val result = eventJson.convertToDataClass<ThreeDSEvent>()

        assertIs<ThreeDSEvent.ChargeErrorEvent>(result)
        assertEquals(Event.CHARGE_ERROR, result.event)
        assertEquals("7a9aded4-0439-4505-aa88-302fbf29d303", result.data.charge3dsId)
        assertEquals("test message example", result.data.error.message)
    }

    @Test(expected = SerializationException::class)
    fun `test unknown data serializer with unknown error serializer`() {
        // Arrange (not official event json)
        val eventJson =
            "{\"event\":\"unknown\",\"data\":{\"charge_3ds_id\":\"7a9aded4-0439-4505-aa88-302fbf29d303\"}"

        eventJson.convertToDataClass<ThreeDSEvent>()
    }
}

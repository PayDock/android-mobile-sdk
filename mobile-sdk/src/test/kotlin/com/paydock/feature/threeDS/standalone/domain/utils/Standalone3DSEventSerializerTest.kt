package com.paydock.feature.threeDS.standalone.domain.utils

import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.threeDS.standalone.domain.model.ui.Standalone3DSEvent
import com.paydock.feature.threeDS.standalone.domain.model.ui.enums.StandaloneEvent
import com.paydock.feature.threeDS.standalone.domain.model.ui.enums.StandaloneStatus
import kotlinx.serialization.SerializationException
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@Suppress("MaxLineLength")
internal class Standalone3DSEventSerializerTest {

    @Test
    fun `test 3ds data serializer with chargeAuthSuccess`() {
        // Arrange
        val eventJson =
            "{\"event\":\"chargeAuthSuccess\",\"data\":{\"status\":\"success\",\"charge_3ds_id\":\"3e07e004-71af-44dc-a9f4-e59520b3e64f\"}}"

        // Act
        val result = eventJson.convertToDataClass<Standalone3DSEvent>()

        // Assert
        assertIs<Standalone3DSEvent.ChargeAuthSuccessEvent>(result)
        assertEquals(StandaloneEvent.CHARGE_AUTH_SUCCESS, result.event)
        assertEquals(StandaloneStatus.SUCCESS, result.data.status)
        assertEquals("3e07e004-71af-44dc-a9f4-e59520b3e64f", result.data.charge3dsId)
    }

    @Test
    fun `test 3ds data serializer with chargeAuthReject`() {
        // Arrange
        val eventJson =
            "{\"event\":\"chargeAuthReject\",\"data\":{\"status\":\"error\",\"charge_3ds_id\":\"b66dc074-5def-4f2a-99f6-58c54c55aabd\",\"result\":{}}}"

        // Act
        val result = eventJson.convertToDataClass<Standalone3DSEvent>()

        // Assert
        assertIs<Standalone3DSEvent.ChargeAuthRejectEvent>(result)
        assertEquals(StandaloneEvent.CHARGE_AUTH_REJECT, result.event)
        assertEquals(StandaloneStatus.ERROR, result.data.status)
        assertEquals("b66dc074-5def-4f2a-99f6-58c54c55aabd", result.data.charge3dsId)
    }

    @Test
    fun `test 3ds data serializer with chargeAuthChallenge`() {
        // Arrange
        val eventJson =
            "{\"event\":\"chargeAuthChallenge\",\"data\":{\"status\":\"pending\",\"charge_3ds_id\":\"3e07e004-71af-44dc-a9f4-e59520b3e64f\",\"result\":{}}}"

        // Act
        val result = eventJson.convertToDataClass<Standalone3DSEvent>()

        // Assert
        assertIs<Standalone3DSEvent.ChargeAuthChallengeEvent>(result)
        assertEquals(StandaloneEvent.CHARGE_AUTH_CHALLENGE, result.event)
        assertEquals(StandaloneStatus.PENDING, result.data.status)
        assertEquals("3e07e004-71af-44dc-a9f4-e59520b3e64f", result.data.charge3dsId)
    }

    @Test
    fun `test error data serializer with error serializer`() {
        // Arrange (not official event json)
        val eventJson =
            "{\"event\":\"error\",\"data\":{\"charge_3ds_id\":\"7a9aded4-0439-4505-aa88-302fbf29d303\",\"error\":{\"message\":\"test message example\"}}}"

        val result = eventJson.convertToDataClass<Standalone3DSEvent>()

        assertIs<Standalone3DSEvent.ChargeErrorEvent>(result)
        assertEquals(StandaloneEvent.CHARGE_ERROR, result.event)
        assertEquals("7a9aded4-0439-4505-aa88-302fbf29d303", result.data.charge3dsId)
        assertEquals("test message example", result.data.error.message)
    }

    @Test(expected = SerializationException::class)
    fun `test unknown data serializer with unknown error serializer`() {
        // Arrange (not official event json)
        val eventJson =
            "{\"event\":\"unknown\",\"data\":{\"charge_3ds_id\":\"7a9aded4-0439-4505-aa88-302fbf29d303\"}"

        eventJson.convertToDataClass<Standalone3DSEvent>()
    }
}

package com.paydock.feature.threeDS.standalone.domain.utils

import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.threeDS.standalone.domain.model.ui.Standalone3DSEvent
import com.paydock.feature.threeDS.standalone.domain.model.ui.enums.StandaloneEvent
import com.paydock.feature.threeDS.standalone.domain.model.ui.enums.StandaloneStatus
import kotlinx.serialization.SerializationException
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

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
    fun `test 3ds data serializer with chargeAuthChallengeLoaded`() {
        // Arrange
        val eventJson =
            "{\"event\":\"chargeAuthChallengeLoaded\",\"data\":{\"charge_3ds_id\":\"3e07e004-71af-44dc-a9f4-e59520b3e64f\",\"reason\":\"timeout\"}}"

        // Act
        val result = eventJson.convertToDataClass<Standalone3DSEvent>()

        // Assert
        assertIs<Standalone3DSEvent.ChargeAuthChallengeLoadedEvent>(result)
        assertEquals(StandaloneEvent.CHARGE_AUTH_CHALLENGE_LOADED, result.event)
        assertEquals("3e07e004-71af-44dc-a9f4-e59520b3e64f", result.data.charge3dsId)
        assertEquals("timeout", result.data.reason)
        assertNull(result.data.status)
    }

    @Test
    fun `test 3ds data serializer with chargeAuthChallengeCompleted`() {
        // Arrange
        val eventJson =
            "{\"event\":\"chargeAuthChallengeCompleted\",\"data\":{\"charge_3ds_id\":\"3e07e004-71af-44dc-a9f4-e59520b3e64f\",\"source\":\"poll\"}}"

        // Act
        val result = eventJson.convertToDataClass<Standalone3DSEvent>()

        // Assert
        assertIs<Standalone3DSEvent.ChargeAuthChallengeCompletedEvent>(result)
        assertEquals(StandaloneEvent.CHARGE_AUTH_CHALLENGE_COMPLETED, result.event)
        assertEquals("3e07e004-71af-44dc-a9f4-e59520b3e64f", result.data.charge3dsId)
        assertEquals("poll", result.data.source)
    }

    @Test
    fun `test 3ds data serializer with sanitised payload containing nulls`() {
        // Arrange (payload shape produced by the page's event transform when fields are missing)
        val eventJson =
            "{\"event\":\"chargeAuthDecoupled\",\"data\":{\"charge_3ds_id\":null,\"status\":null,\"result\":{\"description\":null},\"reason\":null,\"source\":null}}"

        // Act
        val result = eventJson.convertToDataClass<Standalone3DSEvent>()

        // Assert
        assertIs<Standalone3DSEvent.ChargeAuthDecoupledEvent>(result)
        assertNull(result.data.charge3dsId)
        assertNull(result.data.status)
        assertNull(result.data.result?.description)
    }

    @Test
    fun `test 3ds data serializer with chargeAuthInfo without status`() {
        // Arrange
        val eventJson = "{\"event\":\"chargeAuthInfo\",\"data\":{\"info\":\"some info\"}}"

        // Act
        val result = eventJson.convertToDataClass<Standalone3DSEvent>()

        // Assert
        assertIs<Standalone3DSEvent.ChargeAuthInfoEvent>(result)
        assertNull(result.data.status)
        assertNull(result.data.charge3dsId)
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

    @Test
    fun `test unknown event name throws UnknownStandalone3DSEventException with event name`() {
        // Arrange
        val eventJson =
            "{\"event\":\"chargeAuthSomethingNew\",\"data\":{\"charge_3ds_id\":\"7a9aded4-0439-4505-aa88-302fbf29d303\"}}"

        // Act
        val exception = assertFailsWith<UnknownStandalone3DSEventException> {
            eventJson.convertToDataClass<Standalone3DSEvent>()
        }

        // Assert
        assertEquals("chargeAuthSomethingNew", exception.eventName)
    }

    @Test
    fun `test 3ds data serializer keeps raw status and description`() {
        // Arrange
        val eventJson =
            "{\"event\":\"chargeAuthReject\",\"data\":{\"status\":\"rejected\",\"raw_status\":\"rejected\",\"charge_3ds_id\":\"b66dc074-5def-4f2a-99f6-58c54c55aabd\",\"result\":{\"description\":\"frictionless\"}}}"

        // Act
        val result = eventJson.convertToDataClass<Standalone3DSEvent>()

        // Assert
        assertIs<Standalone3DSEvent.ChargeAuthRejectEvent>(result)
        assertNull(result.data.status)
        assertEquals("rejected", result.data.rawStatus)
        assertEquals("frictionless", result.data.result?.description)
    }
}

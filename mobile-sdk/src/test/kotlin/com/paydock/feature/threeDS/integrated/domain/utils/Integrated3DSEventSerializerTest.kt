package com.paydock.feature.threeDS.integrated.domain.utils

import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.threeDS.integrated.domain.model.ui.Integrated3DSEvent
import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.IntegratedEvent
import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.IntegratedStatus
import kotlinx.serialization.SerializationException
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@Suppress("MaxLineLength")
internal class Integrated3DSEventSerializerTest {

    @Test
    fun `test 3ds data serializer with chargeAuthSuccess`() {
        // Arrange
        val eventJson =
            "{\"event\":\"chargeAuthSuccess\",\"data\":{\"event\":\"chargeAuthSuccess\",\"purpose\":\"secure3d\",\"message_source\":\"widget.paydock\",\"ref_id\":\"\",\"widget_id\":\"ba7d839a-a868-4bb0-9763-015451fea9fb\",\"charge_3ds_id\":\"ba7d839a-a868-4bb0-9763-015451fea9fb\",\"status\":\"authenticated\"}}"

        // Act
        val result = eventJson.convertToDataClass<Integrated3DSEvent>()

        // Assert
        assertIs<Integrated3DSEvent.ChargeAuthSuccessEvent>(result)
        assertEquals(IntegratedEvent.CHARGE_AUTH_SUCCESS, result.event)
        assertEquals(IntegratedStatus.AUTHENTICATED, result.data.status)
        assertEquals("ba7d839a-a868-4bb0-9763-015451fea9fb", result.data.charge3dsId)
    }

    @Test
    fun `test 3ds data serializer with chargeAuthReject`() {
        // Arrange
        val eventJson =
            "{\"event\":\"chargeAuthReject\",\"data\":{\"event\":\"chargeAuthReject\",\"purpose\":\"secure3d\",\"message_source\":\"widget.paydock\",\"ref_id\":\"\",\"widget_id\":\"d089f658-e52a-4427-9b29-023b4f38334e\",\"charge_3ds_id\":\"d089f658-e52a-4427-9b29-023b4f38334e\",\"status\":\"not_authenticated\"}}"

        // Act
        val result = eventJson.convertToDataClass<Integrated3DSEvent>()

        // Assert
        assertIs<Integrated3DSEvent.ChargeAuthRejectEvent>(result)
        assertEquals(IntegratedEvent.CHARGE_AUTH_REJECT, result.event)
        assertEquals(IntegratedStatus.NOT_AUTHENTICATED, result.data.status)
        assertEquals("d089f658-e52a-4427-9b29-023b4f38334e", result.data.charge3dsId)
    }

    @Test
    fun `test 3ds data serializer with chargeAuth`() {
        // Arrange
        val eventJson =
            "{\"event\":\"chargeAuth\",\"data\":{\"event\":\"chargeAuth\",\"purpose\":\"secure3d\",\"message_source\":\"widget.paydock\",\"ref_id\":\"\",\"widget_id\":\"59dc8e83-ebd4-4da4-8952-0cbc9f384d41\",\"charge_3ds_id\":\"59dc8e83-ebd4-4da4-8952-0cbc9f384d41\",\"status\":\"not_authenticated\"}}"

        // Act
        val result = eventJson.convertToDataClass<Integrated3DSEvent>()

        // Assert
        assertIs<Integrated3DSEvent.ChargeAuthEvent>(result)
        assertEquals(IntegratedEvent.CHARGE_AUTH, result.event)
        assertEquals(IntegratedStatus.NOT_AUTHENTICATED, result.data.status)
        assertEquals("59dc8e83-ebd4-4da4-8952-0cbc9f384d41", result.data.charge3dsId)
    }

    @Test(expected = SerializationException::class)
    fun `test error data serializer with error serializer`() {
        // Arrange (not official event json)
        val eventJson =
            "{\"event\":\"error\",\"data\":{\"charge_3ds_id\":\"7a9aded4-0439-4505-aa88-302fbf29d303\",\"error\":{\"message\":\"test message example\"}}}"

        eventJson.convertToDataClass<Integrated3DSEvent>()
        // throws exception as no mapping found
    }

    @Test(expected = SerializationException::class)
    fun `test unknown data serializer with unknown error serializer`() {
        // Arrange (not official event json)
        val eventJson =
            "{\"event\":\"unknown\",\"data\":{\"charge_3ds_id\":\"7a9aded4-0439-4505-aa88-302fbf29d303\"}"

        eventJson.convertToDataClass<Integrated3DSEvent>()
    }
}

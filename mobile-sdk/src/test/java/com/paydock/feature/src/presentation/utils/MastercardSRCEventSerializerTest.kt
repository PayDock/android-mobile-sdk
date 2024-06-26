package com.paydock.feature.src.presentation.utils

import com.paydock.core.extensions.convertToDataClass
import com.paydock.feature.src.presentation.model.ErrorData
import com.paydock.feature.src.presentation.model.MastercardSRCEvent
import com.paydock.feature.src.presentation.model.SuccessEventData
import com.paydock.feature.src.presentation.model.enum.Event
import com.paydock.feature.src.presentation.model.enum.EventDataType
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class MastercardSRCEventSerializerTest {

    @Test
    fun `test mastercard src data serializer with IframeLoadedEvent`() {
        // Arrange
        val eventJson =
            "{\"event\":\"iframeLoaded\",\"data\":{}}"

        // Act
        val result = eventJson.convertToDataClass<MastercardSRCEvent>()

        // Assert
        assertIs<MastercardSRCEvent.IframeLoadedEvent>(result)
        assertEquals(Event.IFRAME_LOADED, result.event)

    }

    @Test
    fun `test mastercard src data serializer with CheckoutReadyEvent`() {
        // Arrange
        val eventJson =
            "{\"event\":\"checkoutReady\",\"data\":{\"type\":\"Success\"}}"

        // Act
        val result = eventJson.convertToDataClass<MastercardSRCEvent>()

        // Assert
        assertIs<MastercardSRCEvent.CheckoutReadyEvent>(result)
        assertEquals(Event.CHECKOUT_READY, result.event)
        assertEquals("Success", result.data?.type)
    }

    @Test
    fun `test mastercard src data serializer with CheckoutPopupOpenEvent`() {
        // Arrange
        val eventJson =
            "{\"event\":\"checkoutPopupOpen\",\"data\":{\"type\":\"Success\"}}"

        // Act
        val result = eventJson.convertToDataClass<MastercardSRCEvent>()

        // Assert
        assertIs<MastercardSRCEvent.CheckoutPopupOpenEvent>(result)
        assertEquals(Event.CHECKOUT_POPUP_OPEN, result.event)
        assertEquals("Success", result.data?.type)
    }

    @Test
    fun `test mastercard src data serializer with CheckoutPopupClosedEvent`() {
        // Arrange
        val eventJson =
            "{\"event\":\"checkoutPopupClose\",\"data\":{\"type\":\"Success\"}}"

        // Act
        val result = eventJson.convertToDataClass<MastercardSRCEvent>()

        // Assert
        assertIs<MastercardSRCEvent.CheckoutPopupClosedEvent>(result)
        assertEquals(Event.CHECKOUT_POPUP_CLOSE, result.event)
        assertEquals("Success", result.data?.type)
    }

    @Suppress("MaxLineLength")
    @Test
    fun `test mastercard src data serializer with CheckoutErrorEvent - UserError`() {
        // Arrange
        val eventJson =
            "{\"event\":\"checkoutError\",\"data\":{\"type\":\"UserError\",\"data\":\"Request failed due to CARD_MISSING\"}}"

        // Act
        val result = eventJson.convertToDataClass<MastercardSRCEvent>()

        // Assert
        assertIs<MastercardSRCEvent.CheckoutErrorEvent>(result)
        assertEquals(Event.CHECKOUT_ERROR, result.event)
        assertIs<ErrorData.UserErrorData>(result.data)
        assertEquals(EventDataType.USER_ERROR, result.data.type)
        assertTrue(result.data.data.isNotEmpty())
        assertEquals("Request failed due to CARD_MISSING", result.data.data)
    }

    @Suppress("MaxLineLength")
    @Test
    fun `test mastercard src data serializer with CheckoutErrorEvent - CriticalError`() {
        // Arrange
        val eventJson =
            "{\"event\":\"checkoutError\",\"data\":{\"type\":\"CriticalError\",\"data\":\"403 - OK [object Object]\"}}"

        // Act
        val result = eventJson.convertToDataClass<MastercardSRCEvent>()

        // Assert
        assertIs<MastercardSRCEvent.CheckoutErrorEvent>(result)
        assertEquals(Event.CHECKOUT_ERROR, result.event)
        assertIs<ErrorData.CriticalErrorData>(result.data)
        assertEquals(EventDataType.CRITICAL_ERROR, result.data.type)
        assertTrue(result.data.data.isNotEmpty())
        assertEquals("403 - OK [object Object]", result.data.data)
    }

    @Suppress("MaxLineLength")
    @Test
    fun `test mastercard src data serializer with CheckoutCompletedEvent`() {
        // Arrange
        val eventJson =
            "{\"event\":\"checkoutCompleted\",\"data\":{\"type\":\"Success\",\"data\":{\"token\":\"4da9bee6-b2bc-45cb-994a-1715cf015b5a\",\"type\":\"src\",\"checkoutData\":{\"card_number_bin\":\"512035\",\"card_number_last4\":\"4537\",\"card_scheme\":\"mastercard\",\"card_type\":\"credit\"}}}}"

        // Act
        val result = eventJson.convertToDataClass<MastercardSRCEvent>()

        // Assert
        assertIs<MastercardSRCEvent.CheckoutCompletedEvent>(result)
        assertEquals(Event.CHECKOUT_COMPLETED, result.event)
        assertIs<SuccessEventData>(result.data)
        assertEquals(EventDataType.SUCCESS, result.data.type)
        assertEquals("4da9bee6-b2bc-45cb-994a-1715cf015b5a", result.data.data.token)
        assertEquals("src", result.data.data.type)
        assertEquals("512035", result.data.data.checkoutData?.cardNumberBin)
        assertEquals("4537", result.data.data.checkoutData?.cardNumberLast4)
        assertEquals("mastercard", result.data.data.checkoutData?.cardScheme)
        assertEquals("credit", result.data.data.checkoutData?.cardType)
    }

    @Suppress("MaxLineLength")
    @Test
    fun `test mastercard src manual card entry data serializer with CheckoutCompletedEvent`() {
        // Arrange
        val eventJson =
            "{\"event\":\"checkoutCompleted\",\"data\":{\"type\":\"Success\",\"data\":{\"token\":\"7a70398f-8460-426c-b494-27e736474da1\",\"type\":\"manual\"}}}"

        // Act
        val result = eventJson.convertToDataClass<MastercardSRCEvent>()

        // Assert
        assertIs<MastercardSRCEvent.CheckoutCompletedEvent>(result)
        assertEquals(Event.CHECKOUT_COMPLETED, result.event)
        assertIs<SuccessEventData>(result.data)
        assertEquals(EventDataType.SUCCESS, result.data.type)
        assertEquals("7a70398f-8460-426c-b494-27e736474da1", result.data.data.token)
        assertEquals("manual", result.data.data.type)
        assertNull(result.data.data.checkoutData)
    }

    @Test(expected = SerializationException::class)
    fun `test error data serializer with unknown error serializer`() {
        // Arrange
        val eventJson =
            "{\"event\":\"chargeAuthSuccess\",\"data\":{\"type\":\"Auth\"}}"

        eventJson.convertToDataClass<ErrorData>()
    }
}

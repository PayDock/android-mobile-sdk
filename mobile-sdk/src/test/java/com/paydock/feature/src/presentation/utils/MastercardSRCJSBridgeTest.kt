package com.paydock.feature.src.presentation.utils

import com.paydock.core.extensions.convertToDataClass
import com.paydock.feature.src.presentation.model.ErrorData
import com.paydock.feature.src.presentation.model.MastercardSRCEvent
import com.paydock.feature.src.presentation.model.enum.EventDataType
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class MastercardSRCJSBridgeTest {

    private lateinit var mockCallback: (MastercardSRCEvent) -> Unit
    private lateinit var jsBridge: MastercardSRCJSBridge

    @Before
    fun setup() {
        mockCallback = mock()
        jsBridge = MastercardSRCJSBridge(mockCallback)
    }

    @Suppress("MaxLineLength")
    @Test
    fun `postMessage with checkoutCompleted (new user) JSON should call callback with parsed CheckoutCompletedEvent`() {
        val eventJson =
            "{\"event\":\"checkoutCompleted\",\"data\":{\"type\":\"Success\",\"data\":{\"token\":\"afb339cc-4e8c-4766-b624-2a72379d71db\",\"type\":\"src\",\"checkoutData\":{\"card_scheme\":null}}}}"

        val expectedEvent = eventJson.convertToDataClass<MastercardSRCEvent>()

        jsBridge.postMessage(eventJson)

        verify(mockCallback).invoke(expectedEvent)
    }

    @Suppress("MaxLineLength")
    @Test
    fun `postMessage with checkoutCompleted (existing user) JSON should call callback with parsed CheckoutCompletedEvent`() {
        val eventJson =
            "{\"event\":\"checkoutCompleted\",\"data\":{\"type\":\"Success\",\"data\":{\"token\":\"token\",\"type\":\"type\",\"checkoutData\":{\"card_number_bin\":\"1234\",\"card_number_last4\":\"5678\",\"card_scheme\":\"mastercard\",\"card_type\":\"credit\"}}}}"

        val expectedEvent = eventJson.convertToDataClass<MastercardSRCEvent>()

        jsBridge.postMessage(eventJson)

        verify(mockCallback).invoke(expectedEvent)
    }

    @Suppress("MaxLineLength")
    @Test
    fun `postMessage with checkoutError JSON should call callback with parsed CheckoutErrorEvent - CriticalError`() {
        val eventJson =
            "{\"event\":\"checkoutError\",\"data\":{\"type\":\"CriticalError\",\"data\":\"403 - OK [object Object]\"}}"

        val expectedEvent = eventJson.convertToDataClass<MastercardSRCEvent>()

        jsBridge.postMessage(eventJson)

        verify(mockCallback).invoke(expectedEvent)
    }

    @Suppress("MaxLineLength")
    @Test
    fun `postMessage with checkoutError JSON should call callback with parsed CheckoutErrorEvent - UserError`() {
        val eventJson =
            "{\"event\":\"checkoutError\",\"data\":{\"type\":\"UserError\",\"data\":\"Request failed due to CARD_MISSING\"}}"

        val expectedEvent = eventJson.convertToDataClass<MastercardSRCEvent>()

        jsBridge.postMessage(eventJson)

        verify(mockCallback).invoke(expectedEvent)
    }

    @Test
    fun `postMessage with invalid json event should throw IllegalArgumentException and call callback with parsed CheckoutErrorEvent`() {
        val eventJson = "invalid_argument"

        val expectedEvent = MastercardSRCEvent.CheckoutErrorEvent(
            data = ErrorData.CriticalErrorData(
                type = EventDataType.CRITICAL_ERROR,
                data = "Element class kotlinx.serialization.json.JsonLiteral is not a JsonObject"
            )
        )

        jsBridge.postMessage(eventJson)

        verify(mockCallback).invoke(expectedEvent)
    }
}
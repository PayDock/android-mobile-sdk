package com.paydock.feature.zip.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ZipCallbackDataTest {

    @Test
    fun `test initialization with all values`() {
        val callbackData = ZipCallbackData(
            status = ZipStatus.APPROVED,
            checkoutId = "checkout_123",
            orderId = "order_456"
        )

        assertEquals(ZipStatus.APPROVED, callbackData.status)
        assertEquals("checkout_123", callbackData.checkoutId)
        assertEquals("order_456", callbackData.orderId)
    }

    @Test
    fun `test identifier prefers checkoutId`() {
        val callbackData = ZipCallbackData(
            status = ZipStatus.APPROVED,
            checkoutId = "checkout_123",
            orderId = "order_456"
        )

        assertEquals("checkout_123", callbackData.identifier)
    }

    @Test
    fun `test identifier falls back to orderId when checkoutId is null`() {
        val callbackData = ZipCallbackData(
            status = ZipStatus.APPROVED,
            checkoutId = null,
            orderId = "order_456"
        )

        assertEquals("order_456", callbackData.identifier)
    }

    @Test
    fun `test identifier is null when both checkoutId and orderId are null`() {
        val callbackData = ZipCallbackData(
            status = ZipStatus.DECLINED,
            checkoutId = null,
            orderId = null
        )

        assertNull(callbackData.identifier)
    }

    @Test
    fun `test initialization with optional values`() {
        val callbackData = ZipCallbackData(
            status = ZipStatus.CANCELLED,
            checkoutId = null,
            orderId = null
        )

        assertEquals(ZipStatus.CANCELLED, callbackData.status)
        assertNull(callbackData.checkoutId)
        assertNull(callbackData.orderId)
    }

    @Test
    fun `test initialization with default values`() {
        val callbackData = ZipCallbackData(status = ZipStatus.REFERRED)

        assertEquals(ZipStatus.REFERRED, callbackData.status)
        assertNull(callbackData.checkoutId)
        assertNull(callbackData.orderId)
        assertNull(callbackData.identifier)
    }
}

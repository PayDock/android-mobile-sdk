package com.paydock.feature.zip.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ZipStatusTest {

    // MARK: - Raw Value Tests

    @Test
    fun `test approved value`() {
        assertEquals("approved", ZipStatus.APPROVED.value)
    }

    @Test
    fun `test declined value`() {
        assertEquals("declined", ZipStatus.DECLINED.value)
    }

    @Test
    fun `test cancelled value`() {
        assertEquals("cancelled", ZipStatus.CANCELLED.value)
    }

    @Test
    fun `test referred value`() {
        assertEquals("referred", ZipStatus.REFERRED.value)
    }

    @Test
    fun `test unexpected value`() {
        assertEquals("unexpected", ZipStatus.UNEXPECTED.value)
    }

    @Test
    fun `test unexpected_error value`() {
        assertEquals("unexpected_error", ZipStatus.UNEXPECTED_ERROR.value)
    }

    // MARK: - fromValue Tests

    @Test
    fun `test fromValue with approved returns APPROVED`() {
        val status = ZipStatus.fromValue("approved")
        assertEquals(ZipStatus.APPROVED, status)
    }

    @Test
    fun `test fromValue with declined returns DECLINED`() {
        val status = ZipStatus.fromValue("declined")
        assertEquals(ZipStatus.DECLINED, status)
    }

    @Test
    fun `test fromValue with cancelled returns CANCELLED`() {
        val status = ZipStatus.fromValue("cancelled")
        assertEquals(ZipStatus.CANCELLED, status)
    }

    @Test
    fun `test fromValue with referred returns REFERRED`() {
        val status = ZipStatus.fromValue("referred")
        assertEquals(ZipStatus.REFERRED, status)
    }

    @Test
    fun `test fromValue with unexpected returns UNEXPECTED`() {
        val status = ZipStatus.fromValue("unexpected")
        assertEquals(ZipStatus.UNEXPECTED, status)
    }

    @Test
    fun `test fromValue with unexpected_error returns UNEXPECTED_ERROR`() {
        val status = ZipStatus.fromValue("unexpected_error")
        assertEquals(ZipStatus.UNEXPECTED_ERROR, status)
    }

    @Test
    fun `test fromValue with invalid value returns null`() {
        val status = ZipStatus.fromValue("invalid_status")
        assertNull(status)
    }

    @Test
    fun `test fromValue with null returns null`() {
        val status = ZipStatus.fromValue(null)
        assertNull(status)
    }

    @Test
    fun `test fromValue is case insensitive`() {
        assertEquals(ZipStatus.APPROVED, ZipStatus.fromValue("APPROVED"))
        assertEquals(ZipStatus.APPROVED, ZipStatus.fromValue("Approved"))
        assertEquals(ZipStatus.DECLINED, ZipStatus.fromValue("DECLINED"))
    }

    // MARK: - isSuccess Property Tests

    @Test
    fun `test approved isSuccess returns true`() {
        assertTrue(ZipStatus.APPROVED.isSuccess)
    }

    @Test
    fun `test declined isSuccess returns false`() {
        assertFalse(ZipStatus.DECLINED.isSuccess)
    }

    @Test
    fun `test cancelled isSuccess returns false`() {
        assertFalse(ZipStatus.CANCELLED.isSuccess)
    }

    @Test
    fun `test referred isSuccess returns false`() {
        assertFalse(ZipStatus.REFERRED.isSuccess)
    }

    @Test
    fun `test unexpected isSuccess returns false`() {
        assertFalse(ZipStatus.UNEXPECTED.isSuccess)
    }

    @Test
    fun `test unexpectedError isSuccess returns false`() {
        assertFalse(ZipStatus.UNEXPECTED_ERROR.isSuccess)
    }

    // MARK: - isError Property Tests

    @Test
    fun `test declined isError returns true`() {
        assertTrue(ZipStatus.DECLINED.isError)
    }

    @Test
    fun `test referred isError returns true`() {
        assertTrue(ZipStatus.REFERRED.isError)
    }

    @Test
    fun `test unexpectedError isError returns true`() {
        assertTrue(ZipStatus.UNEXPECTED_ERROR.isError)
    }

    @Test
    fun `test unexpected isError returns true`() {
        assertTrue(ZipStatus.UNEXPECTED.isError)
    }

    @Test
    fun `test approved isError returns false`() {
        assertFalse(ZipStatus.APPROVED.isError)
    }

    @Test
    fun `test cancelled isError returns false`() {
        assertFalse(ZipStatus.CANCELLED.isError)
    }

    // MARK: - isCancellation Property Tests

    @Test
    fun `test cancelled isCancellation returns true`() {
        assertTrue(ZipStatus.CANCELLED.isCancellation)
    }

    @Test
    fun `test approved isCancellation returns false`() {
        assertFalse(ZipStatus.APPROVED.isCancellation)
    }

    @Test
    fun `test declined isCancellation returns false`() {
        assertFalse(ZipStatus.DECLINED.isCancellation)
    }

    @Test
    fun `test referred isCancellation returns false`() {
        assertFalse(ZipStatus.REFERRED.isCancellation)
    }

    @Test
    fun `test unexpected isCancellation returns false`() {
        assertFalse(ZipStatus.UNEXPECTED.isCancellation)
    }

    @Test
    fun `test unexpectedError isCancellation returns false`() {
        assertFalse(ZipStatus.UNEXPECTED_ERROR.isCancellation)
    }
}

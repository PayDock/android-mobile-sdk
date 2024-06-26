package com.paydock.feature.card.presentation.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GiftCardInputValidatorTest {
    @Test
    fun `test parseNumber - valid input`() {
        val validNumber = "1234567890123456"
        val result = GiftCardInputValidator.parseNumber(validNumber)
        assertEquals(validNumber, result)
    }

    @Test
    fun `test parseNumber - empty input`() {
        val emptyNumber = ""
        val result = GiftCardInputValidator.parseNumber(emptyNumber)
        assertEquals("", result)
    }

    @Test
    fun `test parseNumber - invalid input with letters`() {
        val invalidNumber = "1234abcd56789012"
        val result = GiftCardInputValidator.parseNumber(invalidNumber)
        assertNull(result)
    }

    @Test
    fun `test parseNumber - invalid input exceeding maximum length`() {
        val invalidNumber = "123456789012345678901234567890" // Exceeds the maximum allowed length
        val result = GiftCardInputValidator.parseNumber(invalidNumber)
        assertNull(result)
    }

    @Test
    fun `test parseCardPin with valid code`() {
        val pin = "123"
        val result = GiftCardInputValidator.parseCardPin(pin)
        assertEquals(pin, result)
    }

    @Test
    fun `test parseCardPin with empty code`() {
        val pin = ""
        val result = GiftCardInputValidator.parseCardPin(pin)
        assertEquals("", result)
    }

    @Test
    fun `test parseCardPin with invalid code`() {
        val pin = "abc"
        val result = GiftCardInputValidator.parseCardPin(pin)
        assertEquals(null, result)
    }
}
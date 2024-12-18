package com.paydock.feature.card.presentation.utils.validators

import org.junit.Assert.assertEquals
import org.junit.Test

internal class LuhnValidatorTest {

    @Test
    fun testValidCardNumber() {
        val validCardNumber = "4532015112830366"
        val result = LuhnValidator.isLuhnValid(validCardNumber)
        assertEquals(true, result)
    }

    @Test
    fun testInvalidCardNumber() {
        val invalidCardNumber = "4532015112830367"
        val result = LuhnValidator.isLuhnValid(invalidCardNumber)
        assertEquals(false, result)
    }

    @Test
    fun testValidCardNumberWithSpaces() {
        val cardNumberWithSpaces = "4 5320 1511 2830 366"
        val result = LuhnValidator.isLuhnValid(cardNumberWithSpaces)
        assertEquals(true, result)
    }

    @Test
    fun testInvalidCardNumberWithNonDigits() {
        val cardNumberWithNonDigits = "453201511283036a"
        val result = LuhnValidator.isLuhnValid(cardNumberWithNonDigits)
        assertEquals(false, result)
    }

    @Test
    fun testEmptyString() {
        val emptyString = ""
        val result = LuhnValidator.isLuhnValid(emptyString)
        assertEquals(false, result)
    }

    @Test
    fun testShortCardNumber() {
        val shortCardNumber = "123"
        val result = LuhnValidator.isLuhnValid(shortCardNumber)
        assertEquals(false, result)
    }

    @Test
    fun testCardHolderName() {
        val cardHolderName = "John Doe"
        val result = LuhnValidator.isLuhnValid(cardHolderName)
        assertEquals(false, result)
    }
}
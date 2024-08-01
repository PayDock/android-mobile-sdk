package com.paydock.feature.card.presentation.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class LuhnAlgorithmTest {

    @Test
    fun testValidCardNumber() {
        val validCardNumber = "4532015112830366"
        val result = CreditCardInputValidator.isLuhnValid(validCardNumber)
        assertEquals(true, result)
    }

    @Test
    fun testInvalidCardNumber() {
        val invalidCardNumber = "4532015112830367"
        val result = CreditCardInputValidator.isLuhnValid(invalidCardNumber)
        assertEquals(false, result)
    }

    @Test
    fun testValidCardNumberWithSpaces() {
        val cardNumberWithSpaces = "4 5320 1511 2830 366"
        val result = CreditCardInputValidator.isLuhnValid(cardNumberWithSpaces)
        assertEquals(true, result)
    }

    @Test
    fun testInvalidCardNumberWithNonDigits() {
        val cardNumberWithNonDigits = "453201511283036a"
        val result = CreditCardInputValidator.isLuhnValid(cardNumberWithNonDigits)
        assertEquals(false, result)
    }

    @Test
    fun testEmptyString() {
        val emptyString = ""
        val result = CreditCardInputValidator.isLuhnValid(emptyString)
        assertEquals(false, result)
    }

    @Test
    fun testShortCardNumber() {
        val shortCardNumber = "123"
        val result = CreditCardInputValidator.isLuhnValid(shortCardNumber)
        assertEquals(false, result)
    }

    @Test
    fun testCardHolderName() {
        val cardHolderName = "John Doe"
        val result = CreditCardInputValidator.isLuhnValid(cardHolderName)
        assertEquals(false, result)
    }
}
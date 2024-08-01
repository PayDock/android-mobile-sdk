package com.paydock.feature.card.presentation.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CreditCardNumberValidatorTest {

    @Test
    fun testCheckNumber_ValidNumber() {
        assertTrue(CreditCardNumberValidator.checkNumber("4111111111111111"))
        assertTrue(CreditCardNumberValidator.checkNumber("1234567890123456"))
        assertTrue(CreditCardNumberValidator.checkNumber("5555555555554444"))
    }

    @Test
    fun testCheckNumber_BlankNumber() {
        assertFalse(CreditCardNumberValidator.checkNumber(""))
        assertFalse(CreditCardNumberValidator.checkNumber("  "))
    }

    @Test
    fun testCheckNumber_NonDigitNumber() {
        assertFalse(CreditCardNumberValidator.checkNumber("4111-1111-1111-1111"))
        assertFalse(CreditCardNumberValidator.checkNumber("4111 abc 1111 1111"))
    }

    @Test
    fun testCheckNumber_ExceedsMaxLength() {
        assertFalse(CreditCardNumberValidator.checkNumber("41111111111111112222"))
    }
}

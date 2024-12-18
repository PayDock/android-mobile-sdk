package com.paydock.feature.card.presentation.utils

import com.paydock.feature.card.presentation.utils.validators.CreditCardNumberValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class CreditCardNumberValidatorTest {

    @Test
    fun testIsValidNumber_ValidNumberFormat() {
        assertTrue(CreditCardNumberValidator.isValidNumberFormat("4111111111111111"))
        assertTrue(CreditCardNumberValidator.isValidNumberFormat("1234567890123456"))
        assertTrue(CreditCardNumberValidator.isValidNumberFormat("5555555555554444"))
    }

    @Test
    fun testIsValidNumber_BlankNumberFormat() {
        assertFalse(CreditCardNumberValidator.isValidNumberFormat(""))
        assertFalse(CreditCardNumberValidator.isValidNumberFormat("  "))
    }

    @Test
    fun testIsValidNumber_NonDigitNumberFormat() {
        assertFalse(CreditCardNumberValidator.isValidNumberFormat("4111-1111-1111-1111"))
        assertFalse(CreditCardNumberValidator.isValidNumberFormat("4111 abc 1111 1111"))
    }

    @Test
    fun testIsValidNumber_Format_ExceedsMaxLength() {
        assertFalse(CreditCardNumberValidator.isValidNumberFormat("41111111111111112222"))
    }
}

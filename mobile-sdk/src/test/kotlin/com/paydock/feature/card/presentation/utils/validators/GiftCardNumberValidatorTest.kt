package com.paydock.feature.card.presentation.utils.validators

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class GiftCardNumberValidatorTest {

    @Test
    fun testIsValidNumber_ValidNumberFormat() {
        assertTrue(GiftCardNumberValidator.isValidNumberFormat("62734010001104878"))
        assertTrue(GiftCardNumberValidator.isValidNumberFormat("12345678901234567891234"))
        assertTrue(GiftCardNumberValidator.isValidNumberFormat("55555555555544"))
    }

    @Test
    fun testIsValidNumber_BlankNumberFormat() {
        assertFalse(GiftCardNumberValidator.isValidNumberFormat(""))
        assertFalse(GiftCardNumberValidator.isValidNumberFormat("  "))
    }

    @Test
    fun testIsValidNumber_NonDigitNumberFormat() {
        assertFalse(GiftCardNumberValidator.isValidNumberFormat("4111-1111-1111-1111"))
        assertFalse(GiftCardNumberValidator.isValidNumberFormat("4111 abc 1111 1111"))
    }

    @Test
    fun testIsValidNumber_Format_ExceedsMaxLength() {
        assertFalse(GiftCardNumberValidator.isValidNumberFormat("41111222233334444555566667777"))
    }

    @Test
    fun testIsValidNumberFormatValid_FailMinLength() {
        assertFalse(GiftCardNumberValidator.isCardNumberValid("411112222"))
    }

    @Test
    fun testIsValidNumberFormatValid_ExceedsMaxLength() {
        assertFalse(GiftCardNumberValidator.isCardNumberValid("41111222233334444555566667777"))
    }
}

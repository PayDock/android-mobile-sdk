package com.paydock.feature.card.presentation.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GiftCardNumberValidatorTest {

    @Test
    fun testCheckNumber_ValidNumber() {
        assertTrue(GiftCardNumberValidator.checkNumber("62734010001104878"))
        assertTrue(GiftCardNumberValidator.checkNumber("12345678901234567891234"))
        assertTrue(GiftCardNumberValidator.checkNumber("55555555555544"))
    }

    @Test
    fun testCheckNumber_BlankNumber() {
        assertFalse(GiftCardNumberValidator.checkNumber(""))
        assertFalse(GiftCardNumberValidator.checkNumber("  "))
    }

    @Test
    fun testCheckNumber_NonDigitNumber() {
        assertFalse(GiftCardNumberValidator.checkNumber("4111-1111-1111-1111"))
        assertFalse(GiftCardNumberValidator.checkNumber("4111 abc 1111 1111"))
    }

    @Test
    fun testCheckNumber_ExceedsMaxLength() {
        assertFalse(GiftCardNumberValidator.checkNumber("41111222233334444555566667777"))
    }

    @Test
    fun testCheckNumberValid_FailMinLength() {
        assertFalse(GiftCardNumberValidator.isCardNumberValid("411112222"))
    }

    @Test
    fun testCheckNumberValid_ExceedsMaxLength() {
        assertFalse(GiftCardNumberValidator.isCardNumberValid("41111222233334444555566667777"))
    }
}

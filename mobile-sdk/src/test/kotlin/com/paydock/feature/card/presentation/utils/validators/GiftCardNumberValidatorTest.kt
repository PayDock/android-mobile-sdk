package com.paydock.feature.card.presentation.utils.validators

import com.paydock.feature.card.presentation.utils.errors.GiftCardNumberError
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertEquals

internal class GiftCardNumberValidatorTest {

    @Test
    fun testIsValidNumber_ValidNumberFormat() {
        assertTrue(GiftCardNumberValidator.isCardNumberValid("62734010001104878"))
        assertTrue(GiftCardNumberValidator.isCardNumberValid("12345678901234567891234"))
        assertTrue(GiftCardNumberValidator.isCardNumberValid("55555555555544"))
    }

    @Test
    fun testIsValidNumber_BlankNumberFormat() {
        assertFalse(GiftCardNumberValidator.isCardNumberValid(""))
        assertFalse(GiftCardNumberValidator.isCardNumberValid("  "))
    }

    @Test
    fun testIsValidNumber_NonDigitNumberFormat() {
        assertFalse(GiftCardNumberValidator.isCardNumberValid("4111-1111-1111-1111"))
        assertFalse(GiftCardNumberValidator.isCardNumberValid("4111 abc 1111 1111"))
    }

    @Test
    fun testIsValidNumber_Format_ExceedsMaxLength() {
        assertFalse(GiftCardNumberValidator.isCardNumberValid("41111222233334444555566667777"))
    }

    @Test
    fun testIsValidNumberFormatValid_FailMinLength() {
        assertFalse(GiftCardNumberValidator.isCardNumberValid("411112222"))
    }

    @Test
    fun testIsValidNumberFormatValid_ExceedsMaxLength() {
        assertFalse(GiftCardNumberValidator.isCardNumberValid("41111222233334444555566667777"))
    }

    @Test
    fun validateGiftCardNumberInput_emptyInput_userInteracted_returnsEmptyError() {
        val cardNumber = ""
        val hasUserInteracted = true
        val expected = GiftCardNumberError.Empty
        val actual = GiftCardNumberValidator.validateCardNumberInput(cardNumber, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun validateGiftCardNumberInput_emptyInput_userNotInteracted_returnsNoneError() {
        val cardNumber = ""
        val hasUserInteracted = false
        val expected = GiftCardNumberError.None
        val actual = GiftCardNumberValidator.validateCardNumberInput(cardNumber, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun validateGiftCardNumberInput_invalid_returnsInvalidError() {
        val cardNumber = "411112222" // fails min-length
        val hasUserInteracted = true
        val expected = GiftCardNumberError.Invalid
        val actual = GiftCardNumberValidator.validateCardNumberInput(cardNumber, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun validateGiftCardNumberInput_valid_returnsNoneError() {
        val cardNumber = "62734010001104878"
        val hasUserInteracted = true
        val expected = GiftCardNumberError.None
        val actual = GiftCardNumberValidator.validateCardNumberInput(cardNumber, hasUserInteracted)
        assertEquals(expected, actual)
    }
}

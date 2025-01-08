package com.paydock.feature.card.presentation.utils.validators

import com.paydock.feature.card.domain.model.integration.enums.CardScheme
import com.paydock.feature.card.presentation.utils.errors.CardNumberError
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertEquals

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

    @Test
    fun validateCardNumberInput_emptyInput_userInteracted_returnsEmptyError() {
        val cardNumber = ""
        val hasUserInteracted = true
        val expected = CardNumberError.Empty
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_emptyInput_userNotInteracted_returnsNoneError() {
        val cardNumber = ""
        val hasUserInteracted = false
        val expected = CardNumberError.None
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_invalidLuhn_returnsInvalidLuhnError() {
        val cardNumber = "49927398717"
        val hasUserInteracted = true
        val expected = CardNumberError.InvalidLuhn
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_unsupportedCardScheme_returnsUnsupportedCardSchemeError() {
        val cardNumber = "49927398716" // Valid Luhn but unsupported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme.VISA // Assume detected as Visa
        val supportedCardSchemes = setOf(CardScheme.MASTERCARD) // Only Mastercard supported
        val expected = CardNumberError.UnsupportedCardScheme
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber,
            hasUserInteracted,
            cardScheme,
            supportedCardSchemes
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_validInput_returnsNoError() {
        val cardNumber = "49927398716" // Valid Luhn and supported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme.VISA // Assume detected as Visa
        val supportedCardSchemes = setOf(CardScheme.VISA) // Visa supported
        val expected = CardNumberError.None
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber,
            hasUserInteracted,
            cardScheme,
            supportedCardSchemes
        )
        assertEquals(expected, actual)
    }
}

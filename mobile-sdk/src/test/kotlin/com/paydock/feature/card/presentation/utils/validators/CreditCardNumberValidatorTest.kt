package com.paydock.feature.card.presentation.utils.validators

import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
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
        val supportedSchemeConfig = SupportedSchemeConfig()
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            schemeConfig = supportedSchemeConfig
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_emptyInput_userNotInteracted_returnsNoneError() {
        val cardNumber = ""
        val hasUserInteracted = false
        val supportedSchemeConfig = SupportedSchemeConfig()
        val expected = CardNumberError.None
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            schemeConfig = supportedSchemeConfig
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_invalidLuhn_returnsInvalidLuhnError() {
        val cardNumber = "49927398717"
        val hasUserInteracted = true
        val supportedSchemeConfig = SupportedSchemeConfig()
        val expected = CardNumberError.InvalidLuhn
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            schemeConfig = supportedSchemeConfig
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_visa_minInvalidLength_returnsInvalidLengthError() {
        val cardNumber = "41111" // Valid Luhn and supported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme.VISA
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardScheme.VISA),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_visa_maxInvalidLength_returnsInvalidLengthError() {
        val cardNumber = "4111111111111111111111111" // Valid Luhn and supported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme.VISA
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardScheme.VISA),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_mastercard_invalidLength_returnsInvalidLengthError() {
        val cardNumber = "5570239"
        val hasUserInteracted = true
        val cardScheme = CardScheme.MASTERCARD
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardScheme.MASTERCARD),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_amex_invalidLength_returnsInvalidLengthError() {
        val cardNumber = "37140019"
        val hasUserInteracted = true
        val cardScheme = CardScheme.AMEX
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardScheme.AMEX),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_diners_invalidLength_returnsInvalidLengthError() {
        val cardNumber = "362400"
        val hasUserInteracted = true
        val cardScheme = CardScheme.DINERS
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardScheme.DINERS),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_discover_minInvalidLength_returnsInvalidLengthError() {
        val cardNumber = "60117090" // Valid Luhn and supported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme.DISCOVER
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardScheme.DISCOVER),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_discover_maxInvalidLength_returnsInvalidLengthError() {
        val cardNumber = "6011709089999627124446" // Valid Luhn and supported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme.DISCOVER
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardScheme.DISCOVER),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_japcb_invalidLength_returnsInvalidLengthError() {
        val cardNumber = "356"
        val hasUserInteracted = true
        val cardScheme = CardScheme.JAPCB
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardScheme.JAPCB),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_solo_minInvalidLength_returnsInvalidLengthError() {
        val cardNumber = "63347819" // Valid Luhn and supported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme.SOLO
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardScheme.SOLO),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_solo_maxInvalidLength_returnsInvalidLengthError() {
        val cardNumber = "676756789012345686971247271461787474" // Valid Luhn and supported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme.SOLO
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardScheme.SOLO),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_ausbc_minInvalidLength_returnsInvalidLengthError() {
        val cardNumber = "56102545698" // Valid Luhn and supported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme.AUSBC
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardScheme.AUSBC),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_ausbc_maxInvalidLength_returnsInvalidLengthError() {
        val cardNumber = "56102545698712342124626321" // Valid Luhn and supported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme.AUSBC
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardScheme.AUSBC),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_default_minInvalidLength_returnsInvalidLengthError() {
        val cardNumber = "11256377" // Valid Luhn and no scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme.AUSBC
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardScheme.AUSBC),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_default_maxInvalidLength_returnsInvalidLengthError() {
        val cardNumber = "1125637784888444276257975182" // Valid Luhn and no scheme
        val hasUserInteracted = true
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            cardScheme = null,
            schemeConfig = SupportedSchemeConfig()
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_unsupportedCardScheme_returnsUnsupportedCardSchemeError() {
        val cardNumber = "4111111111111111" // Valid Luhn but unsupported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme.VISA // Assume detected as Visa
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardScheme.MASTERCARD),
            enableValidation = true
        ) // Only Mastercard supported)
        val expected = CardNumberError.UnsupportedCardScheme
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_validInput_returnsNoError() {
        val cardNumber = "4111111111111111" // Valid Luhn and supported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme.VISA // Assume detected as Visa
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardScheme.VISA),
            enableValidation = true
        ) // Visa supported
        val expected = CardNumberError.None
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_validInputWithDisabledSchemeValidation_returnsNoError() {
        val cardNumber = "4111111111111111" // Valid Luhn and supported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme.VISA
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardScheme.MASTERCARD),
            enableValidation = false
        )
        val expected = CardNumberError.None
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig
        )
        assertEquals(expected, actual)
    }
}

package com.paydock.feature.card.presentation.utils.validators

import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
import com.paydock.feature.card.domain.model.integration.enums.CardType
import com.paydock.feature.card.domain.model.ui.CardCode
import com.paydock.feature.card.domain.model.ui.CardScheme
import com.paydock.feature.card.domain.model.ui.enums.CodeType
import com.paydock.feature.card.presentation.utils.errors.CardNumberError
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CreditCardNumberValidatorTest {

    @Test
    fun testIsValidNumber_ValidNumberFormat() {
        assertTrue(CreditCardNumberValidator.isCardNumberValid("4111111111111111", null, SupportedSchemeConfig()))
        assertTrue(CreditCardNumberValidator.isCardNumberValid("1466366215019142", null, SupportedSchemeConfig()))
        assertTrue(CreditCardNumberValidator.isCardNumberValid("5555555555554444", null, SupportedSchemeConfig()))
    }

    @Test
    fun testIsValidNumber_BlankNumberFormat() {
        assertFalse(CreditCardNumberValidator.isCardNumberValid("", null, SupportedSchemeConfig()))
        assertFalse(CreditCardNumberValidator.isCardNumberValid("  ", null, SupportedSchemeConfig()))
    }

    @Test
    fun testIsValidNumber_NonDigitNumberFormat() {
        assertFalse(CreditCardNumberValidator.isCardNumberValid("4111-1111-1111-1111", null, SupportedSchemeConfig()))
        assertFalse(CreditCardNumberValidator.isCardNumberValid("4111 abc 1111 1111", null, SupportedSchemeConfig()))
    }

    @Test
    fun testIsValidNumber_Format_ExceedsMaxLength() {
        assertFalse(CreditCardNumberValidator.isCardNumberValid("41111111111111112222", null, SupportedSchemeConfig()))
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
            cardScheme = null,
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
            cardScheme = null,
            schemeConfig = supportedSchemeConfig,
            hasUserInteracted = hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_invalidLuhn_returnsInvalidLuhnError() {
        // 16 digits in default range [12,19]; invalid check digit so Luhn fails
        val cardNumber = "4111111111111112"
        val hasUserInteracted = true
        val supportedSchemeConfig = SupportedSchemeConfig()
        val expected = CardNumberError.InvalidLuhn
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = null,
            hasUserInteracted = hasUserInteracted,
            schemeConfig = supportedSchemeConfig
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_belowMinDigits_invalidLuhn_returnsInvalidLengthWhenBlurred() {
        // 11 digits: below default min (12), so InvalidLength not InvalidLuhn (Luhn only runs in [min,max])
        val cardNumber = "49927398717"
        val hasUserInteracted = true
        val supportedSchemeConfig = SupportedSchemeConfig()
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = null,
            hasUserInteracted = hasUserInteracted,
            schemeConfig = supportedSchemeConfig,
            isCardNumberFocused = false
        )
        assertEquals(CardNumberError.InvalidLength, actual)
    }

    @Test
    fun validateCardNumberInput_visa_minInvalidLength_whenBlurred_returnsInvalidLengthError() {
        val cardNumber = "41111" // Below Visa min (16)
        val cardScheme = CardScheme(type = CardType.VISA, code = CardCode(CodeType.CVV, 3))
        val hasUserInteracted = true
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.VISA),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig,
            isCardNumberFocused = false // defocus: min length enforced
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_visa_belowMinLength_whenFocused_returnsNoneError() {
        val cardNumber = "41111" // Below Visa min; while typing min not enforced
        val cardScheme = CardScheme(type = CardType.VISA, code = CardCode(CodeType.CVV, 3))
        val hasUserInteracted = true
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.VISA),
            enableValidation = false
        )
        val expected = CardNumberError.None
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            hasUserInteracted = hasUserInteracted,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig,
            isCardNumberFocused = true
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_visa_maxInvalidLength_returnsInvalidLengthError() {
        val cardNumber = "4111111111111111111111111" // Valid Luhn and supported scheme
        val cardScheme = CardScheme(type = CardType.VISA, code = CardCode(CodeType.CVV, 3))
        val hasUserInteracted = true
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.VISA),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig,
            hasUserInteracted = hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_mastercard_invalidLength_returnsInvalidLengthError() {
        val cardNumber = "5570239"
        val cardScheme = CardScheme(type = CardType.MASTERCARD, code = CardCode(CodeType.CVC, 3))
        val hasUserInteracted = true
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.MASTERCARD),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig,
            hasUserInteracted = hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_amex_invalidLength_returnsInvalidLengthError() {
        val cardNumber = "37140019"
        val cardScheme = CardScheme(type = CardType.AMEX, code = CardCode(CodeType.CVV, 3))
        val hasUserInteracted = true
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.AMEX),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig,
            hasUserInteracted = hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_diners_invalidLength_returnsInvalidLengthError() {
        val cardNumber = "362400"
        val cardScheme = CardScheme(type = CardType.DINERS, code = CardCode(CodeType.CVV, 3))
        val hasUserInteracted = true
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.DINERS),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig,
            hasUserInteracted = hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_discover_minInvalidLength_returnsInvalidLengthError() {
        val cardNumber = "60117090" // Valid Luhn and supported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme(type = CardType.DISCOVER, code = CardCode(CodeType.CID, 3))
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.DISCOVER),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig,
            hasUserInteracted = hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_discover_maxInvalidLength_returnsInvalidLengthError() {
        val cardNumber = "6011709089999627124446" // Valid Luhn and supported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme(type = CardType.DISCOVER, code = CardCode(CodeType.CID, 3))
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.DISCOVER),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig,
            hasUserInteracted = hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_japcb_invalidLength_returnsInvalidLengthError() {
        val cardNumber = "356"
        val hasUserInteracted = true
        val cardScheme = CardScheme(type = CardType.JAPCB, code = CardCode(CodeType.CVV, 3))
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.JAPCB),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig,
            hasUserInteracted = hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_default_minInvalidLength_returnsInvalidLengthError() {
        val cardNumber = "11256377" // Valid Luhn and no scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme(type = CardType.VISA, code = CardCode(CodeType.CVC, 3))
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.VISA),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig,
            hasUserInteracted = hasUserInteracted
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
        val cardScheme = CardScheme(type = CardType.VISA, code = CardCode(CodeType.CVV, 3))
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.MASTERCARD),
            enableValidation = true
        ) // Only Mastercard supported)
        val expected = CardNumberError.UnsupportedCardScheme
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig,
            hasUserInteracted = hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_validInput_returnsNoError() {
        val cardNumber = "4111111111111111" // Valid Luhn and supported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme(type = CardType.VISA, code = CardCode(CodeType.CVV, 3))
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.VISA),
            enableValidation = true
        ) // Visa supported
        val expected = CardNumberError.None
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig,
            hasUserInteracted = hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_validInputWithDisabledSchemeValidation_returnsNoError() {
        val cardNumber = "4111111111111111" // Valid Luhn and supported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme(type = CardType.VISA, code = CardCode(CodeType.CVV, 3))
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.MASTERCARD),
            enableValidation = false
        )
        val expected = CardNumberError.None
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = cardScheme,
            schemeConfig = supportedSchemeConfig,
            hasUserInteracted = hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_unrecognizedCard_withSchemeValidationEnabled_returnsUnsupportedScheme() {
        // When scheme validation is enabled and no scheme is detected after 8 digits,
        // return UnsupportedCardScheme ("Card type not accepted")
        val cardNumber = "1466366215019142" // Valid 16-digit number with valid Luhn, but unrecognized scheme
        val hasUserInteracted = true
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.VISA, CardType.MASTERCARD),
            enableValidation = true
        )
        val expected = CardNumberError.UnsupportedCardScheme
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = null, // No scheme detected (unrecognized)
            schemeConfig = supportedSchemeConfig,
            hasUserInteracted = hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_unrecognizedCard_withSchemeValidationDisabled_allowsGenericValidation() {
        // When scheme validation is disabled, unrecognized cards should pass generic validation (length + Luhn)
        val cardNumber = "1466366215019142" // Valid 16-digit number with valid Luhn, but unrecognized scheme
        val hasUserInteracted = true
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.VISA, CardType.MASTERCARD),
            enableValidation = false // Scheme validation disabled
        )
        val expected = CardNumberError.None
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = null, // No scheme detected (unrecognized)
            schemeConfig = supportedSchemeConfig,
            hasUserInteracted = hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_unrecognizedCard_8digits_withSchemeValidationEnabled_returnsUnsupportedScheme() {
        // Early detection: at exactly 8 digits with no scheme detected and validation enabled,
        // return UnsupportedCardScheme
        val cardNumber = "04824724" // 8 digits, no scheme detected
        val hasUserInteracted = true
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.VISA, CardType.MASTERCARD),
            enableValidation = true
        )
        val expected = CardNumberError.UnsupportedCardScheme
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = null, // No scheme detected
            schemeConfig = supportedSchemeConfig,
            hasUserInteracted = hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_unrecognizedCard_7digits_noSchemeCheck() {
        // Before 8 digits, no scheme check is performed (BIN detection not complete)
        val cardNumber = "0482472" // 7 digits
        val hasUserInteracted = true
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.VISA, CardType.MASTERCARD),
            enableValidation = true
        )
        val expected = CardNumberError.None // No error while typing (below min length, focused)
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = null,
            schemeConfig = supportedSchemeConfig,
            hasUserInteracted = hasUserInteracted,
            isCardNumberFocused = true
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_unrecognizedCard_invalidLuhn_withValidationEnabled_returnsUnsupportedScheme() {
        // When scheme validation is enabled and no scheme detected, UnsupportedCardScheme takes priority
        // (checked before Luhn since we detect early at 8 digits)
        val cardNumber = "1466366215019143" // 16 digits but invalid Luhn check digit
        val hasUserInteracted = true
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.VISA, CardType.MASTERCARD),
            enableValidation = true
        )
        val expected = CardNumberError.UnsupportedCardScheme
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = null, // No scheme detected
            schemeConfig = supportedSchemeConfig,
            hasUserInteracted = hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_unrecognizedCard_invalidLuhn_withValidationDisabled_returnsInvalidLuhn() {
        // When scheme validation is disabled, unrecognized card should fail Luhn validation
        val cardNumber = "1466366215019143" // 16 digits but invalid Luhn check digit
        val hasUserInteracted = true
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.VISA, CardType.MASTERCARD),
            enableValidation = false
        )
        val expected = CardNumberError.InvalidLuhn
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = null, // No scheme detected
            schemeConfig = supportedSchemeConfig,
            hasUserInteracted = hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_unrecognizedCard_tooShort_returnsInvalidLength() {
        // Unrecognized card with fewer than 8 digits should not trigger scheme check
        // Below min length (12 for default) and defocused should return InvalidLength
        val cardNumber = "12345" // Only 5 digits
        val hasUserInteracted = true
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.VISA, CardType.MASTERCARD),
            enableValidation = true
        )
        val expected = CardNumberError.InvalidLength
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = null, // No scheme detected
            schemeConfig = supportedSchemeConfig,
            hasUserInteracted = hasUserInteracted,
            isCardNumberFocused = false // Blurred/defocused
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardNumberInput_emptySupportedSchemes_allowsAnyScheme() {
        // When supportedSchemes is empty, no scheme validation is performed
        val cardNumber = "1466366215019142" // Valid 16-digit unrecognized card
        val hasUserInteracted = true
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = emptySet(), // No specific schemes configured
            enableValidation = true
        )
        val expected = CardNumberError.None
        val actual = CreditCardNumberValidator.validateCardNumberInput(
            cardNumber = cardNumber,
            cardScheme = null,
            schemeConfig = supportedSchemeConfig,
            hasUserInteracted = hasUserInteracted
        )
        assertEquals(expected, actual)
    }
}

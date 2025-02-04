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
        val cardNumber = "49927398717"
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
    fun validateCardNumberInput_visa_minInvalidLength_returnsInvalidLengthError() {
        val cardNumber = "41111" // Valid Luhn and supported scheme
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
            schemeConfig = supportedSchemeConfig
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
    fun validateCardNumberInput_solo_minInvalidLength_returnsInvalidLengthError() {
        val cardNumber = "63347819" // Valid Luhn and supported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme(type = CardType.SOLO, code = CardCode(CodeType.CVC, 3))
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.SOLO),
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
    fun validateCardNumberInput_solo_maxInvalidLength_returnsInvalidLengthError() {
        val cardNumber = "676756789012345686971247271461787474" // Valid Luhn and supported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme(type = CardType.SOLO, code = CardCode(CodeType.CVC, 3))
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.SOLO),
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
    fun validateCardNumberInput_ausbc_minInvalidLength_returnsInvalidLengthError() {
        val cardNumber = "56102545698" // Valid Luhn and supported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme(type = CardType.AUSBC, code = CardCode(CodeType.CVC, 3))
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.AUSBC),
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
    fun validateCardNumberInput_ausbc_maxInvalidLength_returnsInvalidLengthError() {
        val cardNumber = "56102545698712342124626321" // Valid Luhn and supported scheme
        val hasUserInteracted = true
        val cardScheme = CardScheme(type = CardType.AUSBC, code = CardCode(CodeType.CVC, 3))
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.AUSBC),
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
        val cardScheme = CardScheme(type = CardType.AUSBC, code = CardCode(CodeType.CVC, 3))
        val supportedSchemeConfig = SupportedSchemeConfig(
            supportedSchemes = setOf(CardType.AUSBC),
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
}

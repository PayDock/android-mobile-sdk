package com.paydock.feature.card.presentation.utils.validators

import com.paydock.feature.card.domain.model.ui.CardCode
import com.paydock.feature.card.domain.model.ui.enums.CodeType
import com.paydock.feature.card.presentation.utils.errors.SecurityCodeError
import org.junit.Assert.assertEquals
import org.junit.Test

internal class CardSecurityCodeValidatorTest {

    @Test
    fun validateSecurityCodeInput_focused_returnsNoneEvenForInvalidInput() {
        val securityCode = "12" // Invalid for CVV (requires 3 digits)
        val cardCode = CardCode(CodeType.CVV, 3)
        val hasUserInteracted = true
        val isSecurityCodeFocused = true
        val expected = SecurityCodeError.None
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted,
            isSecurityCodeFocused
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateSecurityCodeInput_emptyInput_userInteracted_returnsEmptyError() {
        val securityCode = ""
        val cardCode = CardCode(CodeType.CVV, 3)
        val hasUserInteracted = true
        val expected = SecurityCodeError.Empty
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateSecurityCodeInput_emptyInput_userNotInteracted_returnsNoneError() {
        val securityCode = ""
        val cardCode = CardCode(CodeType.CVV, 3)
        val hasUserInteracted = false
        val expected = SecurityCodeError.None
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateSecurityCodeInput_invalidCVVInput_returnsInvalidError() {
        val securityCode = "12" // Invalid for CVV (requires 3 digits)
        val cardCode = CardCode(CodeType.CVV, 3)
        val hasUserInteracted = true
        val expected = SecurityCodeError.Invalid
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateSecurityCodeInput_invalidCVCInput_returnsInvalidError() {
        val securityCode = "12" // Invalid for CVC (requires 3 digits)
        val cardCode = CardCode(CodeType.CVC, 3)
        val hasUserInteracted = true
        val expected = SecurityCodeError.Invalid
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateAmexSecurityCodeInput_invalidCIDInput_returnsInvalidError() {
        val securityCode = "123" // Invalid for CID (requires 4 digits)
        val cardCode = CardCode(CodeType.CID, 4)
        val hasUserInteracted = true
        val expected = SecurityCodeError.Invalid
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateDiscoverSecurityCodeInput_invalidCIDInput_returnsInvalidError() {
        val securityCode = "12" // Invalid for CID (requires 4 digits)
        val cardCode = CardCode(CodeType.CID, 3)
        val hasUserInteracted = true
        val expected = SecurityCodeError.Invalid
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateSecurityCodeInput_validCVVInput_returnsNoneError() {
        val securityCode = "123" // Valid for CVV
        val cardCode = CardCode(CodeType.CVV, 3)
        val hasUserInteracted = true
        val expected = SecurityCodeError.None
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateSecurityCodeInput_validCVCInput_returnsNoneError() {
        val securityCode = "123" // Valid for CVC
        val cardCode = CardCode(CodeType.CVC, 3)
        val hasUserInteracted = true
        val expected = SecurityCodeError.None
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateAmexSecurityCodeInput_validCIDInput_returnsNoneError() {
        val securityCode = "1234" // Valid for Amex CID
        val cardCode = CardCode(CodeType.CID, 4)
        val hasUserInteracted = true
        val expected = SecurityCodeError.None
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    /**
     * Edge case: Security code validation after changing card type.
     * 4 digits valid for Amex CID becomes invalid when card scheme changes to Visa (3-digit CVV).
     */
    @Test
    fun validateSecurityCodeInput_fourDigitsInvalidForVisaCvv_returnsInvalidError() {
        val securityCode = "1234" // Was valid for Amex CID, now invalid for Visa CVV (3 digits)
        val cardCode = CardCode(CodeType.CVV, 3)
        val hasUserInteracted = true
        val expected = SecurityCodeError.Invalid
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateDiscoverSecurityCodeInput_validCIDInput_returnsNoneError() {
        val securityCode = "123" // Valid for Discover CID
        val cardCode = CardCode(CodeType.CID, 3)
        val hasUserInteracted = true
        val expected = SecurityCodeError.None
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateUnionPaySecurityCodeInput_invalidCVNInput_returnsInvalidError() {
        val securityCode = "12" // Invalid for CVN (requires 3 digits)
        val cardCode = CardCode(CodeType.CVN, 3)
        val hasUserInteracted = true
        val expected = SecurityCodeError.Invalid
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateUnionPaySecurityCodeInput_validCVNInput_returnsNoneError() {
        val securityCode = "123" // Valid for UnionPay CVN
        val cardCode = CardCode(CodeType.CVN, 3)
        val hasUserInteracted = true
        val expected = SecurityCodeError.None
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateUnionPaySecurityCodeInput_emptyInput_userInteracted_returnsEmptyError() {
        val securityCode = ""
        val cardCode = CardCode(CodeType.CVN, 3)
        val hasUserInteracted = true
        val expected = SecurityCodeError.Empty
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateUnionPaySecurityCodeInput_emptyInput_userNotInteracted_returnsNoneError() {
        val securityCode = ""
        val cardCode = CardCode(CodeType.CVN, 3)
        val hasUserInteracted = false
        val expected = SecurityCodeError.None
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    // Default validation tests (when no cardCode is provided)
    @Test
    fun validateSecurityCodeInput_defaultValidation_valid3Digits_returnsNoneError() {
        val securityCode = "123" // Valid for default validation (3-4 digits)
        val cardCode: CardCode? = null
        val hasUserInteracted = true
        val expected = SecurityCodeError.None
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateSecurityCodeInput_defaultValidation_valid4Digits_returnsNoneError() {
        val securityCode = "1234" // Valid for default validation (3-4 digits)
        val cardCode: CardCode? = null
        val hasUserInteracted = true
        val expected = SecurityCodeError.None
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateSecurityCodeInput_defaultValidation_invalid2Digits_returnsInvalidError() {
        val securityCode = "12" // Invalid for default validation (requires 3-4 digits)
        val cardCode: CardCode? = null
        val hasUserInteracted = true
        val expected = SecurityCodeError.Invalid
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateSecurityCodeInput_defaultValidation_invalid5Digits_returnsInvalidError() {
        val securityCode = "12345" // Invalid for default validation (requires 3-4 digits)
        val cardCode: CardCode? = null
        val hasUserInteracted = true
        val expected = SecurityCodeError.Invalid
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateSecurityCodeInput_defaultValidation_emptyInput_userInteracted_returnsEmptyError() {
        val securityCode = ""
        val cardCode: CardCode? = null
        val hasUserInteracted = true
        val expected = SecurityCodeError.Empty
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateSecurityCodeInput_defaultValidation_emptyInput_userNotInteracted_returnsNoneError() {
        val securityCode = ""
        val cardCode: CardCode? = null
        val hasUserInteracted = false
        val expected = SecurityCodeError.None
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateSecurityCodeInput_invalidCVV_CVCInput_returnsInvalidError() {
        val securityCode = "12" // Invalid for CVV_CVC (requires 3 digits)
        val cardCode = CardCode(CodeType.CVV_CVC, 3)
        val hasUserInteracted = true
        val expected = SecurityCodeError.Invalid
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateSecurityCodeInput_validCVV_CVCInput_returnsNoneError() {
        val securityCode = "123" // Valid for CVV_CVC
        val cardCode = CardCode(CodeType.CVV_CVC, 3)
        val hasUserInteracted = true
        val expected = SecurityCodeError.None
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateSecurityCodeInput_CVV_CVC_emptyInput_userInteracted_returnsEmptyError() {
        val securityCode = ""
        val cardCode = CardCode(CodeType.CVV_CVC, 3)
        val hasUserInteracted = true
        val expected = SecurityCodeError.Empty
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateSecurityCodeInput_CVV_CVC_emptyInput_userNotInteracted_returnsNoneError() {
        val securityCode = ""
        val cardCode = CardCode(CodeType.CVV_CVC, 3)
        val hasUserInteracted = false
        val expected = SecurityCodeError.None
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            cardCode,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

}

package com.paydock.feature.card.presentation.utils.validators

import com.paydock.feature.card.domain.model.ui.CardCode
import com.paydock.feature.card.domain.model.ui.enums.CodeType
import com.paydock.feature.card.presentation.utils.errors.SecurityCodeError
import org.junit.Assert.assertEquals
import org.junit.Test

internal class CardSecurityCodeValidatorTest {

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

}

package com.paydock.feature.card.presentation.utils.validators

import com.paydock.feature.card.domain.model.integration.enums.CardScheme
import com.paydock.feature.card.domain.model.integration.enums.SecurityCodeType
import com.paydock.feature.card.presentation.utils.errors.SecurityCodeError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class CardSecurityCodeValidatorTest {

    @Test
    fun testIsSecurityCodeValid_ValidCVV() {
        assertTrue(CardSecurityCodeValidator.isSecurityCodeValid("123", SecurityCodeType.CVV, CardScheme.VISA))
    }

    @Test
    fun testIsSecurityCodeValid_ValidCVC() {
        assertTrue(CardSecurityCodeValidator.isSecurityCodeValid("123", SecurityCodeType.CVC, CardScheme.MASTERCARD))
    }

    @Test
    fun testIsSecurityCodeValid_ValidCID() {
        assertTrue(CardSecurityCodeValidator.isSecurityCodeValid("1234", SecurityCodeType.CID, CardScheme.AMEX))
    }

    @Test
    fun testIsSecurityCodeValid_BlankCode() {
        assertFalse(CardSecurityCodeValidator.isSecurityCodeValid("", SecurityCodeType.CVV, CardScheme.VISA))
    }

    @Test
    fun testIsSecurityCodeValid_NonDigitCode() {
        assertFalse(CardSecurityCodeValidator.isSecurityCodeValid("12a", SecurityCodeType.CID, CardScheme.AMEX))
    }

    @Test
    fun testIsSecurityCodeValid_ExceedsMaxLength() {
        assertFalse(CardSecurityCodeValidator.isSecurityCodeValid("12345", SecurityCodeType.CVV, CardScheme.VISA))
    }

    @Test
    fun testCheckSecurityCode_ValidCVV() {
        assertTrue(CardSecurityCodeValidator.isSecurityCodeComplete("123", SecurityCodeType.CVV, CardScheme.VISA))
    }

    @Test
    fun testCheckSecurityCode_ValidCVC() {
        assertTrue(CardSecurityCodeValidator.isSecurityCodeComplete("124", SecurityCodeType.CVC, CardScheme.MASTERCARD))
    }

    @Test
    fun testCheckSecurityCode_ValidCID() {
        assertTrue(CardSecurityCodeValidator.isSecurityCodeComplete("1234", SecurityCodeType.CID, CardScheme.AMEX))
    }

    @Test
    fun testCheckSecurityCode_BlankCode() {
        assertFalse(CardSecurityCodeValidator.isSecurityCodeComplete("", SecurityCodeType.CVV, CardScheme.VISA))
    }

    @Test
    fun testCheckSecurityCode_NonDigitCode() {
        assertFalse(CardSecurityCodeValidator.isSecurityCodeComplete("12a", SecurityCodeType.CID, CardScheme.AMEX))
    }

    @Test
    fun testCheckSecurityCode_ExceedsMaxLength() {
        assertFalse(CardSecurityCodeValidator.isSecurityCodeComplete("12345", SecurityCodeType.CVV, CardScheme.VISA))
    }

    @Test
    fun testDetectSecurityCodeType_Visa() {
        assertEquals(
            SecurityCodeType.CVV,
            CardSecurityCodeValidator.detectSecurityCodeType(CardScheme.VISA)
        )
    }

    @Test
    fun testDetectSecurityCodeType_MasterCard() {
        assertEquals(
            SecurityCodeType.CVC,
            CardSecurityCodeValidator.detectSecurityCodeType(CardScheme.MASTERCARD)
        )
    }

    @Test
    fun testDetectSecurityCodeType_AmericanExpress() {
        assertEquals(
            SecurityCodeType.CID,
            CardSecurityCodeValidator.detectSecurityCodeType(CardScheme.AMEX)
        )
    }

    @Test
    fun validateSecurityCodeInput_emptyInput_userInteracted_returnsEmptyError() {
        val securityCode = ""
        val securityCodeType = SecurityCodeType.CVV
        val cardScheme = CardScheme.VISA
        val hasUserInteracted = true
        val expected = SecurityCodeError.Empty
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            securityCodeType,
            cardScheme,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateSecurityCodeInput_emptyInput_userNotInteracted_returnsNoneError() {
        val securityCode = ""
        val securityCodeType = SecurityCodeType.CVV
        val cardScheme = CardScheme.VISA
        val hasUserInteracted = false
        val expected = SecurityCodeError.None
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            securityCodeType,
            cardScheme,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateSecurityCodeInput_invalidCVVInput_returnsInvalidError() {
        val securityCode = "12" // Invalid for CVV (requires 3 digits)
        val securityCodeType = SecurityCodeType.CVV
        val cardScheme = CardScheme.VISA
        val hasUserInteracted = true
        val expected = SecurityCodeError.Invalid
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            securityCodeType,
            cardScheme,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateSecurityCodeInput_invalidCVCInput_returnsInvalidError() {
        val securityCode = "12" // Invalid for CVC (requires 3 digits)
        val securityCodeType = SecurityCodeType.CVC
        val cardScheme = CardScheme.MASTERCARD
        val hasUserInteracted = true
        val expected = SecurityCodeError.Invalid
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            securityCodeType,
            cardScheme,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateAmexSecurityCodeInput_invalidCIDInput_returnsInvalidError() {
        val securityCode = "123" // Invalid for CID (requires 4 digits)
        val securityCodeType = SecurityCodeType.CID
        val cardScheme = CardScheme.AMEX
        val hasUserInteracted = true
        val expected = SecurityCodeError.Invalid
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            securityCodeType,
            cardScheme,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateDiscoverSecurityCodeInput_invalidCIDInput_returnsInvalidError() {
        val securityCode = "12" // Invalid for CID (requires 4 digits)
        val securityCodeType = SecurityCodeType.CID
        val cardScheme = CardScheme.DISCOVER
        val hasUserInteracted = true
        val expected = SecurityCodeError.Invalid
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            securityCodeType,
            cardScheme,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateSecurityCodeInput_validCVVInput_returnsNoneError() {
        val securityCode = "123" // Valid for CVV
        val securityCodeType = SecurityCodeType.CVV
        val cardScheme = CardScheme.VISA
        val hasUserInteracted = true
        val expected = SecurityCodeError.None
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            securityCodeType,
            cardScheme,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateSecurityCodeInput_validCVCInput_returnsNoneError() {
        val securityCode = "123" // Valid for CVC
        val securityCodeType = SecurityCodeType.CVC
        val cardScheme = CardScheme.MASTERCARD
        val hasUserInteracted = true
        val expected = SecurityCodeError.None
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            securityCodeType,
            cardScheme,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateAmexSecurityCodeInput_validCIDInput_returnsNoneError() {
        val securityCode = "1234" // Valid for Amex CID
        val securityCodeType = SecurityCodeType.CID
        val cardScheme = CardScheme.AMEX
        val hasUserInteracted = true
        val expected = SecurityCodeError.None
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            securityCodeType,
            cardScheme,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

    @Test
    fun validateDiscoverSecurityCodeInput_validCIDInput_returnsNoneError() {
        val securityCode = "123" // Valid for Discover CID
        val securityCodeType = SecurityCodeType.CID
        val cardScheme = CardScheme.DISCOVER
        val hasUserInteracted = true
        val expected = SecurityCodeError.None
        val actual = CardSecurityCodeValidator.validateSecurityCodeInput(
            securityCode,
            securityCodeType,
            cardScheme,
            hasUserInteracted
        )
        assertEquals(expected, actual)
    }

}

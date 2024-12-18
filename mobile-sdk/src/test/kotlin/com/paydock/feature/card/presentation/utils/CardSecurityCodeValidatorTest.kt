package com.paydock.feature.card.presentation.utils

import com.paydock.feature.card.domain.model.integration.enums.CardIssuerType
import com.paydock.feature.card.domain.model.integration.enums.SecurityCodeType
import com.paydock.feature.card.presentation.utils.validators.CardSecurityCodeValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class CardSecurityCodeValidatorTest {

    @Test
    fun testIsSecurityCodeValid_ValidCVV() {
        assertTrue(CardSecurityCodeValidator.isSecurityCodeValid("123", SecurityCodeType.CVV))
    }

    @Test
    fun testIsSecurityCodeValid_ValidCVC() {
        assertTrue(CardSecurityCodeValidator.isSecurityCodeValid("123", SecurityCodeType.CVC))
    }

    @Test
    fun testIsSecurityCodeValid_ValidCSC() {
        assertTrue(CardSecurityCodeValidator.isSecurityCodeValid("1234", SecurityCodeType.CSC))
    }

    @Test
    fun testIsSecurityCodeValid_BlankCode() {
        assertFalse(CardSecurityCodeValidator.isSecurityCodeValid("", SecurityCodeType.CVV))
    }

    @Test
    fun testIsSecurityCodeValid_NonDigitCode() {
        assertFalse(CardSecurityCodeValidator.isSecurityCodeValid("12a", SecurityCodeType.CSC))
    }

    @Test
    fun testIsSecurityCodeValid_ExceedsMaxLength() {
        assertFalse(CardSecurityCodeValidator.isSecurityCodeValid("12345", SecurityCodeType.CVV))
    }

    @Test
    fun testCheckSecurityCode_ValidCVV() {
        assertTrue(CardSecurityCodeValidator.isSecurityCodeComplete("123", SecurityCodeType.CVV))
    }

    @Test
    fun testCheckSecurityCode_ValidCVC() {
        assertTrue(CardSecurityCodeValidator.isSecurityCodeComplete("124", SecurityCodeType.CVC))
    }

    @Test
    fun testCheckSecurityCode_ValidCSC() {
        assertTrue(CardSecurityCodeValidator.isSecurityCodeComplete("1234", SecurityCodeType.CSC))
    }

    @Test
    fun testCheckSecurityCode_BlankCode() {
        assertFalse(CardSecurityCodeValidator.isSecurityCodeComplete("", SecurityCodeType.CVV))
    }

    @Test
    fun testCheckSecurityCode_NonDigitCode() {
        assertFalse(CardSecurityCodeValidator.isSecurityCodeComplete("12a", SecurityCodeType.CSC))
    }

    @Test
    fun testCheckSecurityCode_ExceedsMaxLength() {
        assertFalse(CardSecurityCodeValidator.isSecurityCodeComplete("12345", SecurityCodeType.CVV))
    }

    @Test
    fun testDetectSecurityCodeType_Visa() {
        assertEquals(
            SecurityCodeType.CVV,
            CardSecurityCodeValidator.detectSecurityCodeType(CardIssuerType.VISA)
        )
    }

    @Test
    fun testDetectSecurityCodeType_MasterCard() {
        assertEquals(
            SecurityCodeType.CVC,
            CardSecurityCodeValidator.detectSecurityCodeType(CardIssuerType.MASTERCARD)
        )
    }

    @Test
    fun testDetectSecurityCodeType_AmericanExpress() {
        assertEquals(
            SecurityCodeType.CSC,
            CardSecurityCodeValidator.detectSecurityCodeType(CardIssuerType.AMERICAN_EXPRESS)
        )
    }
}

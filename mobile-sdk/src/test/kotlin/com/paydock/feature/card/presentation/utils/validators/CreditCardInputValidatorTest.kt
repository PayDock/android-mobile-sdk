package com.paydock.feature.card.presentation.utils.validators

import com.paydock.feature.card.domain.model.integration.enums.SecurityCodeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class CreditCardInputValidatorTest {

    @Test
    fun `test parseHolderName - valid input`() {
        val validName = "John Doe"
        val result = CreditCardInputValidator.parseHolderName(validName)
        assertEquals(validName, result)
    }

    @Test
    fun `test parseHolderName - test cases from test document`() {
        // Card Holder Name Validation document
        assertEquals("Mr Test Name", CreditCardInputValidator.parseHolderName("Mr Test Name"))
        assertEquals(
            "Mr. Test Name Name-Name",
            CreditCardInputValidator.parseHolderName("Mr. Test Name Name-Name")
        )
        assertEquals("John O'Neil", CreditCardInputValidator.parseHolderName("John O'Neil"))
        assertEquals(
            "^aA!@#\$&()-`.+,'_<>;:*=?[ ]/",
            CreditCardInputValidator.parseHolderName("^aA!@#\$&()-`.+,'_<>;:*=?[ ]/")
        )
        assertEquals("Test", CreditCardInputValidator.parseHolderName("Test"))
        assertEquals("Test The 2nd", CreditCardInputValidator.parseHolderName("Test The 2nd"))
        assertEquals("Test III", CreditCardInputValidator.parseHolderName("Test III"))
    }

    @Test
    fun `test parseHolderName - empty input`() {
        val emptyName = ""
        val result = CreditCardInputValidator.parseHolderName(emptyName)
        assertEquals("", result)
    }

    @Test
    fun `test parseNumber - valid input`() {
        val validNumber = "1234567890123456"
        val result = CreditCardInputValidator.parseNumber(validNumber)
        assertEquals(validNumber, result)
    }

    @Test
    fun `test parseNumber - empty input`() {
        val emptyNumber = ""
        val result = CreditCardInputValidator.parseNumber(emptyNumber)
        assertEquals("", result)
    }

    @Test
    fun `test parseNumber - invalid input with letters`() {
        val invalidNumber = "1234abcd56789012"
        val result = CreditCardInputValidator.parseNumber(invalidNumber)
        assertNull(result)
    }

    @Test
    fun `test parseNumber - invalid input exceeding maximum length`() {
        val invalidNumber = "12345678901234567890" // Exceeds the maximum allowed length
        val result = CreditCardInputValidator.parseNumber(invalidNumber)
        assertNull(result)
    }

    @Test
    fun `test valid expiry parsing`() {
        val validExpiry = "1223"
        val result = CreditCardInputValidator.parseExpiry(validExpiry)
        assertEquals("1223", result)
    }

    @Test
    fun `test empty expiry parsing`() {
        val emptyExpiry = ""
        val result = CreditCardInputValidator.parseExpiry(emptyExpiry)
        assertEquals("", result)
    }

    @Test
    fun `test invalid expiry parsing`() {
        val invalidExpiry = "13216"
        val result = CreditCardInputValidator.parseExpiry(invalidExpiry)
        assertNull(result)
    }

    @Test
    fun `test parseSecurityCode with valid code`() {
        val code = "123"
        val securityCodeType = SecurityCodeType.CVV
        val result = CreditCardInputValidator.parseSecurityCode(code, securityCodeType)
        assertEquals(code, result)
    }

    @Test
    fun `test parseSecurityCode with empty code`() {
        val code = ""
        val securityCodeType = SecurityCodeType.CVV
        val result = CreditCardInputValidator.parseSecurityCode(code, securityCodeType)
        assertEquals("", result)
    }

    @Test
    fun `test parseSecurityCode with invalid code`() {
        val code = "abc"
        val securityCodeType = SecurityCodeType.CVV
        val result = CreditCardInputValidator.parseSecurityCode(code, securityCodeType)
        assertEquals(null, result)
    }

    @Test
    fun testValidCardNumber() {
        val validCardNumber = "4532015112830366"
        val result = CreditCardInputValidator.isLuhnValid(validCardNumber)
        assertEquals(true, result)
    }

    @Test
    fun testInvalidCardNumber() {
        val invalidCardNumber = "4532015112830367"
        val result = CreditCardInputValidator.isLuhnValid(invalidCardNumber)
        assertEquals(false, result)
    }

    @Test
    fun testValidCardNumberWithSpaces() {
        val cardNumberWithSpaces = "4 5320 1511 2830 366"
        val result = CreditCardInputValidator.isLuhnValid(cardNumberWithSpaces)
        assertEquals(true, result)
    }

    @Test
    fun testInvalidCardNumberWithNonDigits() {
        val cardNumberWithNonDigits = "453201511283036a"
        val result = CreditCardInputValidator.isLuhnValid(cardNumberWithNonDigits)
        assertEquals(false, result)
    }

    @Test
    fun testEmptyString() {
        val emptyString = ""
        val result = CreditCardInputValidator.isLuhnValid(emptyString)
        assertEquals(false, result)
    }

    @Test
    fun testShortCardNumber() {
        val shortCardNumber = "123"
        val result = CreditCardInputValidator.isLuhnValid(shortCardNumber)
        assertEquals(false, result)
    }

    @Test
    fun testCardHolderName() {
        val cardHolderName = "John Doe"
        val result = CreditCardInputValidator.isLuhnValid(cardHolderName)
        assertEquals(false, result)
    }
}
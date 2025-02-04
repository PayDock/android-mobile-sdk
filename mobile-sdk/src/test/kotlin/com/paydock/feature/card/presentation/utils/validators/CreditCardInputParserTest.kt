package com.paydock.feature.card.presentation.utils.validators

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class CreditCardInputParserTest {

    @Test
    fun `test parseHolderName - valid input`() {
        val validName = "John Doe"
        val result = CreditCardInputParser.parseHolderName(validName)
        assertEquals(validName, result)
    }

    @Test
    fun `test parseHolderName - test cases from test document`() {
        // Card Holder Name Validation document
        assertEquals("Mr Test Name", CreditCardInputParser.parseHolderName("Mr Test Name"))
        assertEquals(
            "Mr. Test Name Name-Name",
            CreditCardInputParser.parseHolderName("Mr. Test Name Name-Name")
        )
        assertEquals("John O'Neil", CreditCardInputParser.parseHolderName("John O'Neil"))
        assertEquals(
            "^aA!@#\$&()-`.+,'_<>;:*=?[ ]/",
            CreditCardInputParser.parseHolderName("^aA!@#\$&()-`.+,'_<>;:*=?[ ]/")
        )
        assertEquals("Test", CreditCardInputParser.parseHolderName("Test"))
        assertEquals("Test The 2nd", CreditCardInputParser.parseHolderName("Test The 2nd"))
        assertEquals("Test III", CreditCardInputParser.parseHolderName("Test III"))
    }

    @Test
    fun `test parseHolderName - empty input`() {
        val emptyName = ""
        val result = CreditCardInputParser.parseHolderName(emptyName)
        assertEquals("", result)
    }

    @Test
    fun `test parseNumber - valid input`() {
        val validNumber = "1234567890123456"
        val result = CreditCardInputParser.parseNumber(validNumber)
        assertEquals(validNumber, result)
    }

    @Test
    fun `test parseNumber - empty input`() {
        val emptyNumber = ""
        val result = CreditCardInputParser.parseNumber(emptyNumber)
        assertEquals("", result)
    }

    @Test
    fun `test parseNumber - invalid input with letters`() {
        val invalidNumber = "1234abcd56789012"
        val result = CreditCardInputParser.parseNumber(invalidNumber)
        assertNull(result)
    }

    @Test
    fun `test parseNumber - invalid input exceeding maximum length`() {
        val invalidNumber = "12345678901234567890" // Exceeds the maximum allowed length
        val result = CreditCardInputParser.parseNumber(invalidNumber)
        // allows full number, but the transformer will prevent > 19 digits
        assertEquals("12345678901234567890", result)
    }

    @Test
    fun `test valid expiry parsing`() {
        val validExpiry = "1223"
        val result = CreditCardInputParser.parseExpiry(validExpiry)
        assertEquals("1223", result)
    }

    @Test
    fun `test empty expiry parsing`() {
        val emptyExpiry = ""
        val result = CreditCardInputParser.parseExpiry(emptyExpiry)
        assertEquals("", result)
    }

    @Test
    fun `test invalid expiry parsing`() {
        val invalidExpiry = "13216"
        val result = CreditCardInputParser.parseExpiry(invalidExpiry)
        assertNull(result)
    }

    @Test
    fun `test parseSecurityCode with valid code`() {
        val code = "123"
        val result = CreditCardInputParser.parseSecurityCode(code, 3)
        assertEquals(code, result)
    }

    @Test
    fun `test parseSecurityCode with empty code`() {
        val code = ""
        val result = CreditCardInputParser.parseSecurityCode(code, 3)
        assertEquals("", result)
    }

    @Test
    fun `test parseSecurityCode with invalid code`() {
        val code = "abc"
        val result = CreditCardInputParser.parseSecurityCode(code, 3)
        assertEquals(null, result)
    }
}
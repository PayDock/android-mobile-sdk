package com.paydock.feature.card.presentation.utils.validators

import com.paydock.feature.card.presentation.utils.errors.CardExpiryError
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertEquals

internal class CardExpiryValidatorTest {

    @Test
    fun testIsCardExpired_NotExpired() {
        assertFalse(CardExpiryValidator.isCardExpired("1225"))
        assertFalse(CardExpiryValidator.isCardExpired("0127"))
        assertFalse(CardExpiryValidator.isCardExpired("0625"))
    }

    @Test
    fun testIsCardExpired_Expired() {
        assertTrue(CardExpiryValidator.isCardExpired("0621"))
        assertTrue(CardExpiryValidator.isCardExpired("0221"))
        assertTrue(CardExpiryValidator.isCardExpired("1220"))
    }

    @Test
    fun testIsCardExpired_ReturnsFalse() {
        assertFalse(CardExpiryValidator.isCardExpired(""))
    }

    @Test
    fun formatExpiry_removesNonDigits() {
        val input = "12/23"
        val expected = "1223"
        val actual = CardExpiryValidator.formatExpiry(input)
        assertEquals(expected, actual)
    }

    @Test
    fun formatExpiry_handlesEmptyString() {
        val input = ""
        val expected = ""
        val actual = CardExpiryValidator.formatExpiry(input)
        assertEquals(expected, actual)
    }

    @Test
    fun formatExpiry_handlesOnlyDigits() {
        val input = "1223"
        val expected = "1223"
        val actual = CardExpiryValidator.formatExpiry(input)
        assertEquals(expected, actual)
    }

    @Test
    fun formatExpiry_handlesSpecialCharacters() {
        val input = "12-23!"
        val expected = "1223"
        val actual = CardExpiryValidator.formatExpiry(input)
        assertEquals(expected, actual)
    }

    @Test
    fun validateExpiryInput_emptyInput_userInteracted_returnsEmptyError() {
        val expiry = ""
        val hasUserInteracted = true
        val expected = CardExpiryError.Empty
        val actual = CardExpiryValidator.validateExpiryInput(expiry, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun validateExpiryInput_emptyInput_userNotInteracted_returnsNoneError() {
        val expiry = ""
        val hasUserInteracted = false
        val expected = CardExpiryError.None
        val actual = CardExpiryValidator.validateExpiryInput(expiry, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun validateExpiryInput_invalidFormat_returnsInvalidFormatError() {
        val expiry = "123"
        val hasUserInteracted = true
        val expected = CardExpiryError.InvalidFormat
        val actual = CardExpiryValidator.validateExpiryInput(expiry, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun validateExpiryInput_expiredCard_returnsExpiredError() {
        // Assuming current date is after 01/20
        val expiry = "0120"
        val hasUserInteracted = true
        val expected = CardExpiryError.Expired
        val actual = CardExpiryValidator.validateExpiryInput(expiry, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun validateExpiryInput_validInput_returnsNoneError() {
        // Assuming current date is before 12/29
        val expiry = "1229"
        val hasUserInteracted = true
        val expected = CardExpiryError.None
        val actual = CardExpiryValidator.validateExpiryInput(expiry, hasUserInteracted)
        assertEquals(expected, actual)
    }
}
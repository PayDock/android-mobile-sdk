package com.paydock.feature.card.presentation.utils.validators

import com.paydock.feature.card.presentation.utils.errors.CardHolderNameError
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertEquals

internal class CardHolderNameValidatorTest {

    @Test
    fun testIsValidHolderName_ValidNameFormat() {
        assertTrue(CardHolderNameValidator.isValidHolderNameFormat("John Doe"))
        assertTrue(CardHolderNameValidator.isValidHolderNameFormat("Jane Smith"))
        assertTrue(CardHolderNameValidator.isValidHolderNameFormat("Alex Johnson"))

        // Card Holder Name Validation document
        assertTrue(CardHolderNameValidator.isValidHolderNameFormat("Mr Test Name"))
        assertTrue(CardHolderNameValidator.isValidHolderNameFormat("Mr. Test Name Name-Name"))
        assertTrue(CardHolderNameValidator.isValidHolderNameFormat("John O'Neil"))
        assertTrue(CardHolderNameValidator.isValidHolderNameFormat("^aA!@#\$&()-`.+,'_<>;:*=?[ ]/"))
        assertTrue(CardHolderNameValidator.isValidHolderNameFormat("Test"))
        assertTrue(CardHolderNameValidator.isValidHolderNameFormat("Test The 2nd"))
        assertTrue(CardHolderNameValidator.isValidHolderNameFormat("Test III"))
    }

    @Test
    fun testIsValidHolderName_BlankNameFormat() {
        assertFalse(CardHolderNameValidator.isValidHolderNameFormat(""))
        assertFalse(CardHolderNameValidator.isValidHolderNameFormat("  "))
        assertFalse(CardHolderNameValidator.isValidHolderNameFormat(null))
    }

    @Test
    fun validateHolderNameInput_emptyInput_userInteracted_returnsEmptyError() {
        val cardNumber = ""
        val hasUserInteracted = true
        val expected = CardHolderNameError.Empty
        val actual = CardHolderNameValidator.validateHolderNameInput(cardNumber, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun validateHolderNameInput_emptyInput_userNotInteracted_returnsNoneError() {
        val cardNumber = ""
        val hasUserInteracted = false
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(cardNumber, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun validateHolderNameInput_invalidLuhn_returnsInvalidLuhnError() {
        val cardNumber = "4532015112830366"
        val hasUserInteracted = true
        val expected = CardHolderNameError.InvalidLuhn
        val actual = CardHolderNameValidator.validateHolderNameInput(cardNumber, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun validateHolderNameInput_validLuhn_returnsNoneError() {
        val cardNumber = "John Doe"
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(cardNumber, hasUserInteracted)
        assertEquals(expected, actual)
    }
}
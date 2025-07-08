package com.paydock.feature.card.presentation.utils.validators

import com.paydock.feature.card.presentation.utils.errors.CardHolderNameError
import org.junit.Test
import kotlin.test.assertEquals

internal class CardHolderNameValidatorTest {

    // --- Tests for Empty/Blank ---

    @Test
    fun `validateHolderNameInput given empty input and user interacted returns Empty error`() {
        val name = ""
        val hasUserInteracted = true
        val expected = CardHolderNameError.Empty
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given blank input and user interacted returns Empty error`() {
        val name = "   " // Just spaces
        val hasUserInteracted = true
        val expected = CardHolderNameError.Empty
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given empty input and user not interacted returns None error`() {
        val name = ""
        val hasUserInteracted = false
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given blank input and user not interacted returns None error`() {
        val name = "   "
        val hasUserInteracted = false
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    // --- Tests for No Alpha Characters ---

    @Test
    fun `validateHolderNameInput given only numbers and user interacted returns NoAlphaCharacters error`() {
        val name = "12345"
        val hasUserInteracted = true
        val expected = CardHolderNameError.NoAlphaCharacters
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given only allowed symbols and user interacted returns NoAlphaCharacters error`() {
        val name = "-.'"
        val hasUserInteracted = true
        val expected = CardHolderNameError.NoAlphaCharacters
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given numbers and allowed symbols and user interacted returns NoAlphaCharacters error`() {
        val name = "123 -.'"
        val hasUserInteracted = true
        val expected = CardHolderNameError.NoAlphaCharacters
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    // --- Tests for Contains Invalid Characters ---

    @Suppress("MaxLineLength")
    @Test
    fun `validateHolderNameInput given name with forbidden symbol exclamation and user interacted returns ContainsInvalidCharacters error`() {
        val name = "John!"
        val hasUserInteracted = true
        val expected = CardHolderNameError.ContainsInvalidCharacters
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given name with forbidden symbol hash and user interacted returns ContainsInvalidCharacters error`() {
        val name = "Doe #"
        val hasUserInteracted = true
        val expected = CardHolderNameError.ContainsInvalidCharacters
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given name with forbidden symbol dollar and user interacted returns ContainsInvalidCharacters error`() {
        val name = "Agent $"
        val hasUserInteracted = true
        val expected = CardHolderNameError.ContainsInvalidCharacters
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given name with several forbidden symbols and user interacted returns ContainsInvalidCharacters error`() {
        val name = "Oh No @^("
        val hasUserInteracted = true
        val expected = CardHolderNameError.ContainsInvalidCharacters
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given name with mixed valid and one invalid character returns ContainsInvalidCharacters error`() {
        val name = "Valid-Name*"
        val hasUserInteracted = true
        val expected = CardHolderNameError.ContainsInvalidCharacters
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    // --- Tests for Valid Names ---

    @Test
    fun `validateHolderNameInput given valid simple name returns None error`() {
        val name = "John Doe"
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given valid name with hyphen returns None error`() {
        val name = "Mary-Anne Smith"
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given valid name with apostrophe returns None error`() {
        val name = "O'Malley"
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given valid name with period returns None error`() {
        val name = "John H. Doe"
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given valid name with mixed allowed symbols returns None error`() {
        val name = "Dr. Jane O'Reilly-Smith Jr."
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given valid single letter name returns None error`() {
        val name = "X"
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given valid name with leading and trailing spaces returns None error`() {
        // Assuming your regex allows leading/trailing spaces and they are not trimmed before validation.
        // If they are trimmed, this might return Empty or None depending on the core name.
        // The current regex `^[a-zA-Z\\s'\\-.]*$` allows this.
        val name = "  John Doe  "
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    // --- Tests for Luhn Check ---

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
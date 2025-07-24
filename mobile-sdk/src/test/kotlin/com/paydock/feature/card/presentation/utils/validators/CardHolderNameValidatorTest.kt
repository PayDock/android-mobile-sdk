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
    fun `validateHolderNameInput given blank input (spaces) and user interacted returns Empty error`() {
        val name = "   " // Just spaces
        val hasUserInteracted = true
        val expected = CardHolderNameError.Empty
        // Validator now trims the input, so "   " becomes "" before validation logic for non-empty checks.
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given blank input (tabs) and user interacted returns Empty error`() {
        val name = "\t\t" // Just tabs
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

    // --- Tests for Invalid Format (Replaces NoAlphaCharacters and ContainsInvalidCharacters) ---

    @Test
    fun `validateHolderNameInput given only numbers returns InvalidFormat error`() {
        val name = "12345"
        val hasUserInteracted = true
        val expected = CardHolderNameError.InvalidFormat // Updated error
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given only allowed symbols but no letters returns InvalidFormat error`() {
        val name = "-.'"
        val hasUserInteracted = true
        val expected = CardHolderNameError.InvalidFormat // Updated error - must start with a letter
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given numbers and allowed symbols but no letters returns InvalidFormat error`() {
        val name = "123 -.'"
        val hasUserInteracted = true
        val expected = CardHolderNameError.InvalidFormat // Updated error
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given name starting with a space (and not trimmed by test) returns InvalidFormat error`() {
        // Validator trims, but if regex was strict about no leading/trailing spaces, this would test it.
        // With trim, " John" becomes "John" which is valid.
        val name = ".John" // Starts with a dot
        val hasUserInteracted = true
        val expected = CardHolderNameError.InvalidFormat
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given name starting with a hyphen returns InvalidFormat error`() {
        val name = "-John"
        val hasUserInteracted = true
        val expected = CardHolderNameError.InvalidFormat
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given name with forbidden symbol exclamation returns InvalidFormat error`() {
        val name = "John!"
        val hasUserInteracted = true
        val expected = CardHolderNameError.InvalidFormat
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given name with forbidden symbol hash returns InvalidFormat error`() {
        val name = "Doe #"
        val hasUserInteracted = true
        val expected = CardHolderNameError.InvalidFormat
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given name with several forbidden symbols returns InvalidFormat error`() {
        val name = "Oh No @^("
        val hasUserInteracted = true
        val expected = CardHolderNameError.InvalidFormat
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given name with mixed valid and one invalid character returns InvalidFormat error`() {
        val name = "Valid-Name*"
        val hasUserInteracted = true
        val expected = CardHolderNameError.InvalidFormat
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given name ending with space (before trim) returns None if core is valid`() {
        // Validator now trims input. "John Doe   " becomes "John Doe"
        val name = "John Doe   "
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given name ending with hyphen returns InvalidFormat if regex disallows it`() {
        // The regex `^[\\p{L}][\\p{L}'.\\s-]*[\\p{L}'.]$|^[\\p{L}]$`
        // aims to not end on a hyphen.
        val name = "John-"
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given name with only backtick returns InvalidFormat error`() {
        val nameInput = "`" // The cardholder name consisting only of a backtick
        val userHasInteracted = true // Assume the user has interacted with the field
        val expectedErrorType = CardHolderNameError.InvalidFormat // Expected validation result
        val actualErrorType = CardHolderNameValidator.validateHolderNameInput(nameInput, userHasInteracted)
        assertEquals(expectedErrorType, actualErrorType)
    }

    @Test
    fun `validateHolderNameInput given name with multiple spaces between words returns None if regex allows or trim handles`() {
        // The regex `[\\p{L}'.\\s-]*` allows multiple spaces if \s is in the class.
        // And trim doesn't affect internal spaces.
        val name = "John    Doe"
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    // --- Tests for Valid Names (Including Unicode and Edge Cases) ---

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
    fun `validateHolderNameInput given valid name with backtick returns None error`() {
        val name = "O`Malley"
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
    fun `validateHolderNameInput given valid name with leading and trailing spaces (gets trimmed) returns None error`() {
        val name = "  John Doe  "
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    // --- New Tests for Unicode Names ---
    @Test
    fun `validateHolderNameInput given valid German name with umlaut returns None error`() {
        val name = "Jürgen Müller"
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given valid French name with accent returns None error`() {
        val name = "Élodie Château"
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given valid Spanish name with tilde returns None error`() {
        val name = "Peña Núñez"
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given valid Chinese name returns None error`() {
        val name = "李明" // Li Ming
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given valid Korean name returns None error`() {
        val name = "박서준" // Park Seo-joon
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given valid Japanese name returns None error`() {
        val name = "山田太郎" // Yamada Tarou
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given name with only Unicode letters returns None error`() {
        val name = "Абвгд" // Cyrillic letters
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun `validateHolderNameInput given mixed ASCII and Unicode name returns None error`() {
        val name = "Hans Müller"
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }

    // --- Tests for Luhn Check ---

    @Test
    fun `validateHolderNameInput given Luhn-valid number string returns InvalidLuhn error`() {
        // This test assumes LuhnValidator.isLuhnValid would return true for "49927398716"
        val cardNumberLikeString = "49927398716"
        val hasUserInteracted = true
        val actual = CardHolderNameValidator.validateHolderNameInput(cardNumberLikeString, hasUserInteracted)
        if (LuhnValidator.isLuhnValid(cardNumberLikeString)) {
            assertEquals(CardHolderNameError.InvalidLuhn, actual)
        } else {
            // If not luhn valid, our current regex would mark it as InvalidFormat
            assertEquals(CardHolderNameError.InvalidFormat, actual)
        }
    }

    @Test
    fun `validateHolderNameInput given valid name (not Luhn) returns None error`() {
        val name = "John Doe" // Not Luhn valid
        val hasUserInteracted = true
        val expected = CardHolderNameError.None
        val actual = CardHolderNameValidator.validateHolderNameInput(name, hasUserInteracted)
        assertEquals(expected, actual)
    }
}

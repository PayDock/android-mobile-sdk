package com.paydock.feature.card.presentation.utils.validators

import com.paydock.feature.card.presentation.utils.errors.CardPinError
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertEquals

internal class CardPinValidatorTest {

    @Test
    fun testIsValidPin_Format_ValidNumber() {
        assertTrue(CardPinValidator.isCardPinValid("1234"))
    }

    @Test
    fun testIsValidPin_Format_BlankNumber() {
        assertFalse(CardPinValidator.isCardPinValid(""))
        assertFalse(CardPinValidator.isCardPinValid("  "))
    }

    @Test
    fun testIsValidPin_Format_NonDigitNumber() {
        assertFalse(CardPinValidator.isCardPinValid("abcd"))
    }

    @Test
    fun validateCardPinInput_emptyInput_userInteracted_returnsEmptyError() {
        val cardPin = ""
        val hasUserInteracted = true
        val expected = CardPinError.Empty
        val actual = CardPinValidator.validateCardPinInput(cardPin, hasUserInteracted)
        assertEquals(expected, actual)
    }

    @Test
    fun validateCardPinInput_emptyInput_userNotInteracted_returnsNoneError() {
        val cardPin = ""
        val hasUserInteracted = false
        val expected = CardPinError.None
        val actual = CardPinValidator.validateCardPinInput(cardPin, hasUserInteracted)
        assertEquals(expected, actual)
    }
}

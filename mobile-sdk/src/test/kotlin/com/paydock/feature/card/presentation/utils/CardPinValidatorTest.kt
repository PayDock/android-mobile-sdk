package com.paydock.feature.card.presentation.utils

import com.paydock.feature.card.presentation.utils.validators.CardPinValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class CardPinValidatorTest {

    @Test
    fun testIsValidPin_Format_ValidNumber() {
        assertTrue(CardPinValidator.isValidPinFormat("1234"))
    }

    @Test
    fun testIsValidPin_Format_BlankNumber() {
        assertFalse(CardPinValidator.isValidPinFormat(""))
        assertFalse(CardPinValidator.isValidPinFormat("  "))
    }

    @Test
    fun testIsValidPin_Format_NonDigitNumber() {
        assertFalse(CardPinValidator.isValidPinFormat("abcd"))
    }
}

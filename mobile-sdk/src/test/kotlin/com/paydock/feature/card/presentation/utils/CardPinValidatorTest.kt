package com.paydock.feature.card.presentation.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CardPinValidatorTest {

    @Test
    fun testCheckPin_ValidNumber() {
        assertTrue(CardPinValidator.checkPin("1234"))
    }

    @Test
    fun testCheckPin_BlankNumber() {
        assertFalse(CardPinValidator.checkPin(""))
        assertFalse(CardPinValidator.checkPin("  "))
    }

    @Test
    fun testCheckPin_NonDigitNumber() {
        assertFalse(CardPinValidator.checkPin("abcd"))
    }
}

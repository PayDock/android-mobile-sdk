package com.paydock.feature.card.presentation.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CardExpiryValidatorTest {

    @Test
    fun testCheckExpiry_ValidInput() {
        assertTrue(CardExpiryValidator.checkExpiry("0123"))
        assertTrue(CardExpiryValidator.checkExpiry("1123"))
        assertTrue(CardExpiryValidator.checkExpiry("0525"))
    }

    @Test
    fun testCheckExpiry_InvalidInput() {
        assertFalse(CardExpiryValidator.checkExpiry(""))
        assertFalse(CardExpiryValidator.checkExpiry("12345"))
        assertFalse(CardExpiryValidator.checkExpiry("abc"))
    }

    @Test
    fun testIsExpiryValid_ValidInput() {
        assertTrue(CardExpiryValidator.isExpiryValid("0125"))
        assertTrue(CardExpiryValidator.isExpiryValid("1125"))
        assertTrue(CardExpiryValidator.isExpiryValid("0525"))
    }

    @Test
    fun testIsExpiryValid_InvalidInput() {
        assertFalse(CardExpiryValidator.isExpiryValid(""))
        assertFalse(CardExpiryValidator.isExpiryValid("01"))
        assertFalse(CardExpiryValidator.isExpiryValid("12/45"))
        assertFalse(CardExpiryValidator.isExpiryValid("00"))
        assertFalse(CardExpiryValidator.isExpiryValid("1325"))
    }

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
}
package com.paydock.feature.card.presentation.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CardHolderNameValidatorTest {

    @Test
    fun testCheckHolderName_ValidName() {
        assertTrue(CardHolderNameValidator.checkHolderName("John Doe"))
        assertTrue(CardHolderNameValidator.checkHolderName("Jane Smith"))
        assertTrue(CardHolderNameValidator.checkHolderName("Alex Johnson"))

        // Card Holder Name Validation document
        assertTrue(CardHolderNameValidator.checkHolderName("Mr Test Name"))
        assertTrue(CardHolderNameValidator.checkHolderName("Mr. Test Name Name-Name"))
        assertTrue(CardHolderNameValidator.checkHolderName("John O'Neil"))
        assertTrue(CardHolderNameValidator.checkHolderName("^aA!@#\$&()-`.+,'_<>;:*=?[ ]/"))
        assertTrue(CardHolderNameValidator.checkHolderName("Test"))
        assertTrue(CardHolderNameValidator.checkHolderName("Test The 2nd"))
        assertTrue(CardHolderNameValidator.checkHolderName("Test III"))
    }

    @Test
    fun testCheckHolderName_BlankName() {
        assertFalse(CardHolderNameValidator.checkHolderName(""))
        assertFalse(CardHolderNameValidator.checkHolderName("  "))
    }
}
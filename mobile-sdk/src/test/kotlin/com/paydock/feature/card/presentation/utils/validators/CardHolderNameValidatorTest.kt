package com.paydock.feature.card.presentation.utils.validators

import com.paydock.feature.card.presentation.utils.errors.CardHolderNameError
import org.junit.Test
import kotlin.test.assertEquals

internal class CardHolderNameValidatorTest {

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
package com.paydock.feature.card.presentation.utils.validators

import com.paydock.feature.card.presentation.utils.errors.CardHolderNameError

/**
 * A utility object for validating cardholder names.
 *
 * This object provides methods to verify the validity of cardholder name inputs
 * based on specific conditions, such as non-empty strings and optional Luhn validation.
 */
internal object CardHolderNameValidator {

    fun isCardHolderNameValid(cardHolderName: String?): Boolean {
        return cardHolderName?.let { validateHolderNameInput(it, true) == CardHolderNameError.None } ?: true
    }

    /**
     * Validates the cardholder name input and returns an appropriate error state.
     *
     * The validation checks the following conditions:
     * 1. If the name is blank and the user has interacted, it returns a `CardHolderNameError.Empty` error.
     * 2. If the name is not blank but fails the Luhn validation, it returns a `CardHolderNameError.InvalidLuhn` error.
     * 3. If the name passes all checks, it returns `CardHolderNameError.None`.
     *
     * @param name The cardholder name to be validated.
     * @param hasUserInteracted A flag indicating whether the user has interacted with the input field.
     * @return A [CardHolderNameError] representing the validation result.
     */
    fun validateHolderNameInput(name: String, hasUserInteracted: Boolean): CardHolderNameError {
        val isLuhnValid = LuhnValidator.isLuhnValid(name)
        return when {
            name.isBlank() && hasUserInteracted -> CardHolderNameError.Empty
            name.isNotBlank() && isLuhnValid -> CardHolderNameError.InvalidLuhn
            else -> CardHolderNameError.None
        }
    }
}
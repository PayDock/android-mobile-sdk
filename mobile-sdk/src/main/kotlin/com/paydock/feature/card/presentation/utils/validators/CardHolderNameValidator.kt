package com.paydock.feature.card.presentation.utils.validators

import com.paydock.core.MobileSDKConstants
import com.paydock.feature.card.presentation.utils.errors.CardHolderNameError

/**
 * A utility object for validating cardholder names.
 *
 * This object provides methods to verify the validity of cardholder name inputs
 * based on specific conditions, supporting Unicode characters for international names.
 */
internal object CardHolderNameValidator {

    /**
     * Checks if the cardholder name is valid.
     *
     * This function considers a cardholder name valid if it's either null or, if not null,
     * passes the validation defined by [validateHolderNameInput] with `hasUserInteracted`
     * set to `true`. An empty or null input is considered valid
     * until the user has interacted with the field and provided a non-empty value.
     *
     * @param cardHolderName The cardholder name string to validate. Can be null.
     * @return `true` if the cardholder name is considered valid, `false` otherwise.
     */
    fun isCardHolderNameValid(cardHolderName: String?): Boolean {
        return cardHolderName?.let { validateHolderNameInput(it, true) == CardHolderNameError.None } ?: true
    }

    /**
     * Validates the cardholder name input and returns an appropriate error state.
     *
     * The validation checks the following conditions in order:
     * 1. If `name` is blank (empty or whitespace only) and `hasUserInteracted` is true, it returns `CardHolderNameError.Empty`.
     * 2. If `name` (after trimming) does not match the `validCardHolderNameRegex`,
     *    it returns `CardHolderNameError.InvalidFormat`. This single check now covers aspects like:
     *    - Not starting with a letter.
     *    - Containing disallowed characters.
     *    - Invalid structure (e.g. not matching the pattern for single letters or multi-part names).
     * 3. If `name` is not blank and is considered Luhn valid (which is typically an error for names),
     *    it returns `CardHolderNameError.InvalidLuhn`.
     * 4. If none of the above conditions are met, it returns `CardHolderNameError.None`, indicating the name is valid.
     *
     * @param name The cardholder name string to be validated.
     * @param hasUserInteracted A boolean flag indicating whether the user has interacted with the input field.
     *                          This is used to determine if an empty field should be considered an error.
     * @return A [CardHolderNameError] enum value representing the validation result.
     *         `CardHolderNameError.None` indicates a valid name.
     */
    fun validateHolderNameInput(name: String, hasUserInteracted: Boolean): CardHolderNameError {
        val trimmedName = name.trim()
        val isLuhnValid = LuhnValidator.isLuhnValid(name) // Using original name for Luhn as per original code
        return when {
            // 1. Check for blank input if the user has interacted
            trimmedName.isBlank() && hasUserInteracted -> CardHolderNameError.Empty
            // 2. Luhn check (often indicates card number mistakenly entered in name field)
            trimmedName.isNotBlank() && isLuhnValid -> CardHolderNameError.InvalidLuhn
            // 3. Comprehensive format check using the new regex.
            trimmedName.isNotBlank() && !trimmedName.matches(MobileSDKConstants.Regex.CARD_HOLDER_NAME) -> {
                // This single regex now covers:
                // - Must start with a Unicode letter.
                // - Must only contain allowed characters (Unicode letters, space, ', -, .).
                // - Must adhere to the general structure (e.g. not just symbols, specific start/end).
                CardHolderNameError.InvalidFormat
            }
            // 4. If all above checks pass, the name is considered valid.
            else -> CardHolderNameError.None
        }
    }
}
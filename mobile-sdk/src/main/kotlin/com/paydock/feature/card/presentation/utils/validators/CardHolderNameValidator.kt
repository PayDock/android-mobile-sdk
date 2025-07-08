package com.paydock.feature.card.presentation.utils.validators

import com.paydock.feature.card.presentation.utils.errors.CardHolderNameError

/**
 * A utility object for validating cardholder names.
 *
 * This object provides methods to verify the validity of cardholder name inputs
 * based on specific conditions, such as non-empty strings and optional Luhn validation.
 */
internal object CardHolderNameValidator {

    /**
     * Regex for validating allowed characters in a cardholder name.
     *
     * This regex matches strings that ONLY contain:
     * - a-z (lowercase letters)
     * - A-Z (uppercase letters)
     * - \s (whitespace characters, primarily space)
     * - ' (apostrophe)
     * - - (hyphen)
     * - . (period - common in some initials or titles like Jr.)
     *
     * The `^` and `$` anchors ensure the entire string matches this pattern.
     * The character set `[a-zA-Z\\s'\\-.]` defines all allowed characters.
     * The `*` quantifier means the allowed characters can appear zero or more times.
     */
    private val allowedNameCharactersRegex = Regex("^[a-zA-Z\\s'\\-.]*$")

    // Regex to check for at least one alphabetical character
    private val alphaCharacterRegex = Regex(".*[a-zA-Z].*")

    /**
     * Checks if the cardholder name is valid.
     *
     * This function considers a cardholder name valid if it's either null or, if not null,
     * passes the validation defined by [validateHolderNameInput] with `hasUserInteracted`
     * set to `true`. This implies that an empty or null input is considered valid
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
     * 1. If `name` is blank and `hasUserInteracted` is true, it returns `CardHolderNameError.Empty`.
     * 2. If `name` is not blank but does not contain at least one alphabetical character (checked by `alphaCharacterRegex`),
     *    it returns `CardHolderNameError.NoAlphaCharacters`.
     * 3. If `name` is not blank but contains characters not allowed by `allowedNameCharactersRegex`,
     *    it returns `CardHolderNameError.ContainsInvalidCharacters`.
     * 4. If `name` is not blank and is considered Luhn valid (which is typically an error for names),
     *    it returns `CardHolderNameError.InvalidLuhn`. This check might indicate a misconfiguration or
     *    an unusual requirement if Luhn validation is being applied to a name field.
     * 5. If none of the above conditions are met, it returns `CardHolderNameError.None`, indicating the name is valid.
     *
     * @param name The cardholder name string to be validated.
     * @param hasUserInteracted A boolean flag indicating whether the user has interacted with the input field.
     *                          This is used to determine if an empty field should be considered an error.
     * @return A [CardHolderNameError] enum value representing the validation result.
     *         `CardHolderNameError.None` indicates a valid name.
     */
    fun validateHolderNameInput(name: String, hasUserInteracted: Boolean): CardHolderNameError {
        val isLuhnValid = LuhnValidator.isLuhnValid(name)
        return when {
            name.isBlank() && hasUserInteracted -> CardHolderNameError.Empty
            name.isNotBlank() && isLuhnValid -> CardHolderNameError.InvalidLuhn
            name.isNotBlank() && !name.matches(alphaCharacterRegex) -> CardHolderNameError.NoAlphaCharacters
            name.isNotBlank() && !name.matches(allowedNameCharactersRegex) -> CardHolderNameError.ContainsInvalidCharacters
            else -> CardHolderNameError.None
        }
    }
}
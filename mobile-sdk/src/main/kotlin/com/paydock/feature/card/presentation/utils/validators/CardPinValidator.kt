package com.paydock.feature.card.presentation.utils.validators

import com.paydock.core.MobileSDKConstants
import com.paydock.feature.card.presentation.utils.errors.CardPinError

/**
 * A utility object for validating and parsing card pin details.
 */
internal object CardPinValidator {

    /**
     * Validates the provided card PIN to ensure it meets the required criteria.
     * This function delegates the validation to the `validateCardPinInput` function and checks
     * if there are no errors (i.e., `CardPinError.None`).
     *
     * @param number The card PIN to be validated.
     * @return `true` if the card PIN is valid, `false` otherwise.
     */
    fun isCardPinValid(number: String): Boolean =
        validateCardPinInput(number, true) == CardPinError.None

    /**
     * Validates the input for a card PIN and determines the corresponding validation error state.
     *
     * This function checks the card PIN input for common validation scenarios:
     * - If the input is empty and the user has interacted with the field, it returns an [CardPinError.Empty].
     * - Otherwise, it returns [CardPinError.None], indicating no validation error.
     *
     * @param cardPin The card PIN input provided by the user.
     * @param hasUserInteracted A flag indicating whether the user has interacted with the field.
     * @return A [CardPinError] representing the validation state of the card PIN input.
     */
    fun validateCardPinInput(cardPin: String, hasUserInteracted: Boolean): CardPinError {
        val isValidFormat = validateCardPinFormat(cardPin)
        return when {
            cardPin.isBlank() && hasUserInteracted -> CardPinError.Empty
            cardPin.isNotBlank() && !isValidFormat -> CardPinError.Invalid
            else -> CardPinError.None
        }
    }

    /**
     * Validates whether the given gift card pin contains only numeric digits.
     *
     * This function ensures that the card number matches the expected numeric pattern.
     *
     * @param cardPin The gift card pin to check.
     * @return `true` if the card pin consists only of numeric digits, `false` otherwise.
     */
    private fun validateCardPinFormat(cardPin: String): Boolean {
        return cardPin.matches(MobileSDKConstants.Regex.NUMERIC_DIGITS)
    }

}
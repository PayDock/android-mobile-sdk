package com.paydock.feature.card.presentation.utils.validators

import com.paydock.core.MobileSDKConstants
import com.paydock.feature.card.presentation.utils.errors.CardPinError

/**
 * A utility object for validating and parsing card pin details.
 */
internal object CardPinValidator {

    /**
     * Checks if the provided card pin is valid.
     *
     * A valid card pin should meet the following conditions:
     * 1. It should not be blank or empty.
     * 2. It should contain only digits.
     *
     * @param pin The card pin to be validated.
     * @return `true` if the name is valid, `false` otherwise.
     */
    fun isValidPinFormat(pin: String): Boolean =
        pin.isNotBlank() && pin.matches(MobileSDKConstants.Regex.NUMERIC_DIGITS)

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
        return when {
            cardPin.isBlank() && hasUserInteracted -> CardPinError.Empty
            else -> CardPinError.None
        }
    }

}
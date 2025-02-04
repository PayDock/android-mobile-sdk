package com.paydock.feature.card.presentation.utils.validators

import com.paydock.core.MobileSDKConstants
import com.paydock.feature.card.presentation.utils.errors.GiftCardNumberError

/**
 * A utility object for validating gift card number details.
 */
internal object GiftCardNumberValidator {

    /**
     * Checks whether the provided card number meets the required format and length constraints.
     *
     * A card number is considered valid if:
     * 1. It consists only of numeric digits, verified by [validateCardNumberFormat].
     * 2. Its length falls within the defined valid range:
     *    - [MobileSDKConstants.CardDetailsConfig.MIN_GIFT_CARD_LENGTH]
     *    - [MobileSDKConstants.CardDetailsConfig.MAX_GIFT_CARD_LENGTH].
     *
     * This function utilizes [validateCardNumberInput] to determine validity based on these criteria.
     *
     * @param number The card number to be validated.
     * @return `true` if the card number is valid (i.e., no errors), `false` otherwise.
     *
     * @see validateCardNumberInput
     * @see validateCardNumberFormat
     * @see validateCardNumberLength
     *
     * Example usage:
     * ```
     * isCardNumberValid("123456789012") // Returns true if it meets the valid length and format
     * isCardNumberValid("abcd1234")     // Returns false due to invalid format
     * isCardNumberValid("1234")         // Returns false if too short
     * ```
     */
    fun isCardNumberValid(number: String): Boolean =
        validateCardNumberInput(number, true) == GiftCardNumberError.None

    /**
     * Validates the gift card number input and determines the type of validation error.
     *
     * This function checks if the gift card number is:
     * - Empty (and if the user has interacted with the field).
     * - In an incorrect format (non-numeric characters).
     * - Of an invalid length (not within the expected range).
     *
     * @param cardNumber The gift card number to validate.
     * @param hasUserInteracted Flag indicating if the user has interacted with the input field.
     * @return A [GiftCardNumberError] representing the validation result:
     *         - [GiftCardNumberError.Empty]: The input is blank and the user has interacted.
     *         - [GiftCardNumberError.Invalid]: The input fails format or length validation.
     *         - [GiftCardNumberError.None]: The input is valid.
     */
    fun validateCardNumberInput(
        cardNumber: String,
        hasUserInteracted: Boolean
    ): GiftCardNumberError {
        val isValidFormat = validateCardNumberFormat(cardNumber)
        val isValidLength = validateCardNumberLength(cardNumber)
        return when {
            cardNumber.isBlank() && hasUserInteracted -> GiftCardNumberError.Empty
            cardNumber.isNotBlank() && (!isValidFormat || !isValidLength) -> GiftCardNumberError.Invalid
            else -> GiftCardNumberError.None
        }
    }

    /**
     * Validates whether the given gift card number contains only numeric digits.
     *
     * This function ensures that the card number matches the expected numeric pattern.
     *
     * @param cardNumber The gift card number to check.
     * @return `true` if the card number consists only of numeric digits, `false` otherwise.
     */
    private fun validateCardNumberFormat(cardNumber: String): Boolean {
        return cardNumber.matches(MobileSDKConstants.Regex.NUMERIC_DIGITS)
    }

    /**
     * Checks if the given gift card number length falls within the valid range.
     *
     * The valid length range is defined in [MobileSDKConstants.CardDetailsConfig].
     *
     * @param cardNumber The gift card number to check.
     * @return `true` if the card number length is within the allowed range, `false` otherwise.
     */
    private fun validateCardNumberLength(cardNumber: String): Boolean {
        val length = cardNumber.length
        return length in
            MobileSDKConstants.CardDetailsConfig.MIN_GIFT_CARD_LENGTH..MobileSDKConstants.CardDetailsConfig.MAX_GIFT_CARD_LENGTH
    }
}
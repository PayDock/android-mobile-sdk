package com.paydock.feature.card.presentation.utils.validators

import com.paydock.core.MobileSDKConstants

/**
 * A utility object for validating and parsing gift card input details.
 */
internal object GiftCardInputParser {
    /**
     * Parses and validates the provided gift card number.
     *
     * If the gift card number passes the validation, it is returned as is. If the number is empty, an empty string is returned.
     * If the number is invalid, `null` is returned to indicate that it is not a valid credit card number.
     *
     * @param number The gift card number to be parsed and validated.
     * @return The valid gift card number, an empty string if the input is empty, or `null` if the number is invalid.
     */
    fun parseNumber(number: String): String? = when {
        isValidNumberFormat(number) -> number
        number.isEmpty() -> ""
        else -> null
    }

    /**
     * Checks if the provided gift card number is valid.
     *
     * A valid credit card number should meet the following conditions:
     * 1. It should not be blank or empty.
     * 2. It should contain only digits.
     * 3. It should not exceed the maximum allowed card length defined by [MAX_GIFT_CARD_LENGTH].
     *
     * @param number The gift card number to be validated.
     * @return `true` if the number is valid, `false` otherwise.
     */
    private fun isValidNumberFormat(number: String): Boolean =
        number.isNotBlank() &&
            number.matches(MobileSDKConstants.Regex.NUMERIC_DIGITS) &&
            number.length <= MobileSDKConstants.CardDetailsConfig.MAX_GIFT_CARD_LENGTH

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
    private fun isValidPinFormat(pin: String): Boolean =
        pin.isNotBlank() && pin.matches(MobileSDKConstants.Regex.NUMERIC_DIGITS)

    /**
     * Parses and validates the card pin input.
     *
     * @param pin The card pin input string to parse and validate.
     * @return The parsed and validated card pin if valid, an empty string if input is empty, or null if invalid.
     */
    fun parseCardPin(pin: String): String? = when {
        isValidPinFormat(pin) -> pin
        pin.isEmpty() -> ""
        else -> null
    }
}
package com.paydock.feature.card.presentation.utils

/**
 * A utility object for validating and parsing gift card input details.
 */
internal object GiftCardInputValidator {
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
        GiftCardNumberValidator.checkNumber(number) -> number
        number.isEmpty() -> ""
        else -> null
    }

    /**
     * Parses and validates the card pin input.
     *
     * @param pin The card pin input string to parse and validate.
     * @return The parsed and validated card pin if valid, an empty string if input is empty, or null if invalid.
     */
    fun parseCardPin(pin: String): String? = when {
        CardPinValidator.checkPin(pin) -> pin
        pin.isEmpty() -> ""
        else -> null
    }
}
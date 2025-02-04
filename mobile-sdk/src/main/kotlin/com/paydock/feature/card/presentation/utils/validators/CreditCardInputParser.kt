package com.paydock.feature.card.presentation.utils.validators

import com.paydock.core.MobileSDKConstants

/**
 * A utility object for validating and parsing credit card input details.
 */
internal object CreditCardInputParser {

    /**
     * Parses the cardholder name and returns it if it is a valid name.
     *
     * If the cardholder name passes the validation, it is returned as is. If the name is empty, an empty string is returned.
     * If the name is invalid, `null` is returned to indicate that it is not a valid cardholder name.
     *
     * @param name The input cardholder name.
     * @return The cardholder name if it is valid, an empty string if the input is empty, or null if it is invalid.
     */
    fun parseHolderName(name: String): String? = when {
        name.isNotBlank() -> name
        name.isEmpty() -> "" // If the name is empty, return an empty string.
        else -> null // If the name is invalid, return null.
    }

    /**
     * Parses and validates the provided credit card number.
     *
     * If the credit card number passes the validation, it is returned as is. If the number is empty, an empty string is returned.
     * If the number is invalid, `null` is returned to indicate that it is not a valid credit card number.
     *
     * @param number The credit card number to be parsed and validated.
     * @return The valid credit card number, an empty string if the input is empty, or `null` if the number is invalid.
     */
    fun parseNumber(number: String): String? = when {
        number.isNotBlank() && number.matches(MobileSDKConstants.Regex.NUMERIC_DIGITS) -> number
        number.isEmpty() -> ""
        else -> null
    }

    /**
     * Parses and validates the expiry field input.
     *
     * @param expiry The expiry input string to parse and validate.
     * @return The parsed and validated expiry string in the format MM/YY, or null if invalid.
     */
    fun parseExpiry(expiry: String): String? = when {
        expiry.isNotBlank() && expiry.matches(MobileSDKConstants.Regex.NUMERIC_DIGITS) &&
            expiry.length <= MobileSDKConstants.CardDetailsConfig.MAX_EXPIRY_LENGTH -> expiry

        expiry.isEmpty() -> ""
        else -> null
    }

    /**
     * Parses and validates the security code input based on the specified security code type.
     *
     * @param code The security code input string to parse and validate.
     * @param codeLength The expected security code length for the given card scheme.
     * @return The parsed and validated security code if valid, an empty string if input is empty, or null if invalid.
     */
    fun parseSecurityCode(code: String, codeLength: Int): String? = when {
        code.isNotBlank() && code.matches(MobileSDKConstants.Regex.NUMERIC_DIGITS) && code.length <= codeLength -> code
        code.isEmpty() -> ""
        else -> null
    }
}
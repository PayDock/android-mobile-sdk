package com.paydock.feature.card.presentation.utils

import com.paydock.feature.card.presentation.model.SecurityCodeType

/**
 * A utility object for validating and parsing credit card input details.
 */
internal object CreditCardInputValidator {
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
        CardHolderNameValidator.checkHolderName(name) -> name
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
        CreditCardNumberValidator.checkNumber(number) -> number
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
        CardExpiryValidator.checkExpiry(expiry) -> expiry
        expiry.isEmpty() -> ""
        else -> null
    }

    /**
     * Parses and validates the security code input based on the specified security code type.
     *
     * @param code The security code input string to parse and validate.
     * @param securityCodeType The type of security code to validate against.
     * @return The parsed and validated security code if valid, an empty string if input is empty, or null if invalid.
     */
    fun parseSecurityCode(code: String, securityCodeType: SecurityCodeType): String? = when {
        CardSecurityCodeValidator.checkSecurityCode(code, securityCodeType) -> code
        code.isEmpty() -> ""
        else -> null
    }

    /**
     * Checks if a given number is valid according to the Luhn Algorithm.
     *
     * The Luhn Algorithm, also known as the "modulus 10" or "mod 10" algorithm, is a simple checksum formula
     * used to validate a variety of identification numbers, such as credit card numbers.
     *
     * @see https://en.wikipedia.org/wiki/Luhn_algorithm
     * @param number The number to be checked for validity.
     * @return `true` if the number is valid according to the Luhn Algorithm, otherwise `false`.
     */
    @Suppress("MagicNumber")
    fun isLuhnValid(number: String): Boolean {
        // Remove spaces and check for invalid inputs
        val sanitizedNumber = number.replace("\\s+".toRegex(), "") // Remove spaces
        if (sanitizedNumber.length <= 1 || !sanitizedNumber.all { it.isDigit() }) {
            return false
        }

        // Convert the number to a list of digits
        val digits = sanitizedNumber.map { it.toString().toInt() }.toMutableList()

        // Remove and store the last digit
        val lastDigit = digits.removeAt(digits.size - 1)

        // Reverse the list of digits
        digits.reverse()

        // Apply the Luhn Algorithm to the digits
        val sum = digits.mapIndexed { index, digit ->
            if (index % 2 == 0) {
                // This is a step within the Luhn algorithm to handle the doubling of digits that result in values greater than 9
                val doubled = digit * 2
                if (doubled > 9) doubled - 9 else doubled
            } else {
                digit
            }
        }.sum()

        // Calculate the total sum
        val totalSum = sum + lastDigit

        // Check if the total sum is divisible by 10
        return totalSum % 10 == 0
    }
}
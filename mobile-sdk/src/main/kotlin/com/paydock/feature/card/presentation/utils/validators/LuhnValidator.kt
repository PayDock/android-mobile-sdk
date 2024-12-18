package com.paydock.feature.card.presentation.utils.validators

/**
 * Utility object for validating numbers using the Luhn Algorithm.
 *
 * The Luhn Algorithm, also known as the "modulus 10" or "mod 10" algorithm, is a simple checksum formula
 * used to validate identification numbers like credit card numbers.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Luhn_algorithm">Luhn Algorithm (Wikipedia)</a>
 */
internal object LuhnValidator {

    /**
     * Validates a number using the Luhn Algorithm.
     *
     * This method checks whether a given string representing a number is valid according to the Luhn Algorithm.
     * It first sanitizes the input to remove spaces, verifies that the string contains only digits, and then
     * applies the Luhn checksum logic to validate the number.
     *
     * ### Algorithm Steps:
     * 1. Remove spaces from the input and ensure it contains only numeric digits.
     * 2. Extract and remove the last digit (check digit).
     * 3. Reverse the remaining digits.
     * 4. Double every second digit in the reversed list:
     *    - If the doubled value is greater than 9, subtract 9 from it.
     * 5. Sum all the transformed digits and add the check digit.
     * 6. The number is valid if the total sum is divisible by 10.
     *
     * @param number The input string representing the number to validate.
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
                // Double every second digit and subtract 9 if the result exceeds 9
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
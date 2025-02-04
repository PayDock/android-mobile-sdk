package com.paydock.feature.card.presentation.utils.validators

import com.paydock.core.MobileSDKConstants
import com.paydock.feature.card.presentation.utils.errors.CardExpiryError
import java.util.Calendar

/**
 * Utility object for validating and formatting credit card expiry dates.
 *
 * Provides methods to validate the format and expiration status of credit card expiry dates,
 * as well as to extract and format expiry date inputs.
 */
internal object CardExpiryValidator {

    fun isExpiryValid(expiry: String): Boolean = validateExpiryInput(expiry, true) == CardExpiryError.None

    /**
     * Validates the expiry date input and determines the type of validation error.
     *
     * @param expiry The expiry date string to validate.
     * @param hasUserInteracted Flag indicating if the user has interacted with the input field.
     * @return A [CardExpiryError] representing the validation result.
     */
    fun validateExpiryInput(expiry: String, hasUserInteracted: Boolean): CardExpiryError {
        val isValid = validateExpiryFormat(expiry)
        val isExpired = isCardExpired(expiry)
        return when {
            expiry.isBlank() && hasUserInteracted -> CardExpiryError.Empty
            expiry.isNotBlank() && !isValid -> CardExpiryError.InvalidFormat
            expiry.isNotBlank() && isExpired -> CardExpiryError.Expired
            else -> CardExpiryError.None
        }
    }

    /**
     * Extracts the month and year from the expiry date string.
     *
     * @param expiry The expiry date string in "MMYY" or similar format.
     * @return A pair containing the extracted month and year as nullable integers.
     */
    private fun extractMonthAndYear(expiry: String): Pair<Int?, Int?> {
        val month =
            expiry.take(MobileSDKConstants.CardDetailsConfig.EXPIRY_CHUNK_SIZE).toIntOrNull()
        val year = expiry.drop(MobileSDKConstants.CardDetailsConfig.EXPIRY_CHUNK_SIZE)
            .take(MobileSDKConstants.CardDetailsConfig.EXPIRY_CHUNK_SIZE).toIntOrNull()
        return month to year
    }

    /**
     * Checks if the expiry date string represents a valid month and year combination.
     *
     * @param expiry The expiry date string to validate.
     * @return True if the expiry date is valid, otherwise false.
     */
    private fun validateExpiryFormat(expiry: String): Boolean {
        val (month, year) = extractMonthAndYear(expiry)
        return expiry.isNotBlank() && expiry.length <= (MobileSDKConstants.CardDetailsConfig.MAX_EXPIRY_LENGTH + 1) &&
            expiry.matches(MobileSDKConstants.Regex.NUMERIC_DIGITS) && isMonthValid(month) && isYearValid(
                year
            )
    }

    /**
     * Checks if the given month is valid (between 1 and 12).
     *
     * @param inputMonth The month to validate.
     * @return True if the month is valid, otherwise false.
     */
    private fun isMonthValid(inputMonth: Int?): Boolean {
        return inputMonth != null && inputMonth in 1..MobileSDKConstants.CardDetailsConfig.MAX_MONTH_COUNT
    }

    /**
     * Checks if the given year is valid (between 0 and 99).
     *
     * @param inputYear The year to validate.
     * @return True if the year is valid, otherwise false.
     */
    @Suppress("MagicNumber")
    private fun isYearValid(inputYear: Int?): Boolean {
        return inputYear != null && inputYear in 0..99 && inputYear.toString().length == 2
    }

    /**
     * Checks if the given expiry date string represents an already expired card.
     *
     * @param expiry The expiry date string in "MMYY" or similar format.
     * @return True if the card is expired, otherwise false.
     */
    fun isCardExpired(expiry: String): Boolean {
        val (month, year) = extractMonthAndYear(expiry)
        return if (month != null && year != null) {
            isMonthExpired(month, year) || isYearExpired(year)
        } else {
            false
        }
    }

    /**
     * Checks if the given month and year have already expired.
     *
     * @param inputMonth The expiry month.
     * @param inputYear The expiry year in two-digit format (e.g., "23" for 2023).
     * @return True if the month and year combination has expired, otherwise false.
     */
    private fun isMonthExpired(inputMonth: Int, inputYear: Int): Boolean {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val expiryYear = MobileSDKConstants.CardDetailsConfig.EXPIRY_BASE_YEAR + inputYear
        return expiryYear < currentYear || (expiryYear == currentYear && inputMonth < currentMonth)
    }

    /**
     * Checks if the given year has already expired.
     *
     * @param inputYear The expiry year in two-digit format (e.g., "23" for 2023).
     * @return True if the year has expired, otherwise false.
     */
    private fun isYearExpired(inputYear: Int?): Boolean {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val expiryYear = MobileSDKConstants.CardDetailsConfig.EXPIRY_BASE_YEAR + inputYear!!
        return isYearValid(inputYear) && currentYear > expiryYear
    }

    /**
     * Formats the input expiry date string to only include numeric characters.
     *
     * @param input The expiry date string to format.
     * @return A string containing only the numeric characters of the input.
     */
    fun formatExpiry(input: String): String {
        return input.filter { it.isDigit() }
    }
}

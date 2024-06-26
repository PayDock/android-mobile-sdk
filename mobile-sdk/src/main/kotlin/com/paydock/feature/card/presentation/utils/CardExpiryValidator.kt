package com.paydock.feature.card.presentation.utils

import com.paydock.core.MobileSDKConstants
import java.util.Calendar

/**
 * A utility object for validating and parsing card expiry details.
 */
internal object CardExpiryValidator {

    /**
     * Checks if the expiry input is valid.
     *
     * @param expiry The expiry input string to validate.
     * @return True if the expiry input is valid, false otherwise.
     */
    fun checkExpiry(expiry: String): Boolean {
        return expiry.isNotBlank() && expiry.matches(Regex("^[0-9]+$")) &&
            expiry.length <= MobileSDKConstants.CardDetailsConfig.MAX_EXPIRY_LENGTH
    }

    /**
     * Checks if the expiry input is valid.
     *
     * @param expiry The expiry input string to validate.
     * @return True if the expiry input is valid, false otherwise.
     */
    fun isExpiryValid(expiry: String): Boolean {
        return expiry.isNotBlank() && expiry.length <= (MobileSDKConstants.CardDetailsConfig.MAX_EXPIRY_LENGTH + 1) &&
            expiry.matches(Regex("^[0-9]+$")) && !isCardExpired(expiry)
    }

    /**
     * Checks if the card is expired based on the expiry input.
     *
     * @param expiry The expiry input string to check.
     * @return True if the card is expired, false otherwise.
     */
    fun isCardExpired(expiry: String): Boolean {
        val month = expiry.take(MobileSDKConstants.CardDetailsConfig.EXPIRY_CHUNK_SIZE)
            .toIntOrNull()
        val year = expiry.drop(MobileSDKConstants.CardDetailsConfig.EXPIRY_CHUNK_SIZE)
            .take(MobileSDKConstants.CardDetailsConfig.EXPIRY_CHUNK_SIZE).toIntOrNull()
        return !(isMonthValid(month, year) && isYearValid(year))
    }

    /**
     * Validates the month part of the expiry input.
     *
     * @param inputMonth The month part of the expiry input (MM).
     * @param inputYear The year part of the expiry input (YY).
     * @return True if the month is valid (between 1 and 12 and not in the past), false otherwise.
     */
    private fun isMonthValid(inputMonth: Int?, inputYear: Int?): Boolean {
        val currentMonth =
            Calendar.getInstance().get(Calendar.MONTH) + 1 // Calendar months are zero-based
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        // Cards expire at the end of the month that they expire
        return inputMonth != null && inputMonth in 1..MobileSDKConstants.CardDetailsConfig.MAX_MONTH_COUNT &&
            !(
                inputMonth < currentMonth && currentYear == (
                    MobileSDKConstants.CardDetailsConfig.EXPIRY_BASE_YEAR + (
                        inputYear
                            ?: 0
                        )
                    )
                )
    }

    /**
     * Validates the year part of the expiry input.
     *
     * @param inputYear The year part of the expiry input (YY).
     * @return True if the year is valid (not in the past), false otherwise.
     */
    private fun isYearValid(inputYear: Int?): Boolean {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return inputYear != null && currentYear <= MobileSDKConstants.CardDetailsConfig.EXPIRY_BASE_YEAR + inputYear
    }
}
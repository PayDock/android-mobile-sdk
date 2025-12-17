package com.paydock.sample.core.utils

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Utility object for formatting currency values based on currency codes.
 */
object CurrencyFormatter {
    /**
     * Formats a price value using the specified currency code.
     *
     * @param amount The amount to format
     * @param currencyCode The ISO 4217 currency code (e.g., "USD", "AUD", "EUR")
     * @return Formatted currency string
     */
    fun format(amount: Double, currencyCode: String): String {
        return try {
            val currency = Currency.getInstance(currencyCode.uppercase())
            // Get a locale that uses this currency
            val locale = getLocaleForCurrency(currencyCode)
            val formatter = NumberFormat.getCurrencyInstance(locale)
            formatter.currency = currency
            formatter.format(amount)
        } catch (e: Exception) {
            // Fallback to USD if currency code is invalid
            NumberFormat.getCurrencyInstance(Locale.US).format(amount)
        }
    }

    /**
     * Gets an appropriate Locale for a given currency code.
     * Maps common currency codes to their primary locales.
     */
    private fun getLocaleForCurrency(currencyCode: String): Locale {
        return when (currencyCode.uppercase()) {
            "USD" -> Locale.US
            "AUD" -> Locale("en", "AU")
            "GBP" -> Locale.UK
            "CAD" -> Locale.CANADA
            "NZD" -> Locale("en", "NZ")
            "EUR" -> Locale.GERMANY // Using Germany locale for EUR (common EUR locale)
            "JPY" -> Locale.JAPAN
            "CHF" -> Locale("de", "CH")
            "SEK" -> Locale("sv", "SE")
            "NOK" -> Locale("no", "NO")
            "DKK" -> Locale("da", "DK")
            else -> Locale.US // Default fallback
        }
    }
}


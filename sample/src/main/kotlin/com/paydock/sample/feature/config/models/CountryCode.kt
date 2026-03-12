package com.paydock.sample.feature.config.models

import java.util.Locale

/**
 * Supported country codes for widget configurations.
 * Uses ISO 3166-1 alpha-2 country codes.
 */
enum class CountryCode(val code: String, val displayName: String) {
    UNITED_STATES("US", "United States"),
    AUSTRALIA("AU", "Australia"),
    UNITED_KINGDOM("GB", "United Kingdom"),
    CANADA("CA", "Canada"),
    NEW_ZEALAND("NZ", "New Zealand"),
    FRANCE("FR", "France"),
    SPAIN("ES", "Spain"),
    GERMANY("DE", "Germany"),
    ITALY("IT", "Italy"),
    NETHERLANDS("NL", "Netherlands"),
    JAPAN("JP", "Japan"),
    CHINA("CN", "China"),
    SOUTH_KOREA("KR", "South Korea"),
    BRAZIL("BR", "Brazil"),
    MEXICO("MX", "Mexico"),
    INDIA("IN", "India"),
    SINGAPORE("SG", "Singapore");

    companion object {
        fun fromCode(code: String): CountryCode? {
            return entries.find { it.code == code }
        }

        fun fromCodeOrDefault(code: String): CountryCode {
            return fromCode(code) ?: try {
                // Try to get display name from Locale
                val locale = Locale.Builder().setRegion(code).build()
                val displayName = locale.displayCountry
                // Return default if locale lookup fails
                UNITED_STATES
            } catch (e: Exception) {
                UNITED_STATES
            }
        }
    }
}


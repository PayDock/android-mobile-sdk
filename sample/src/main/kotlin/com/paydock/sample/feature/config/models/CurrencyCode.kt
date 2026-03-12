package com.paydock.sample.feature.config.models

/**
 * Supported currency codes for widget configurations.
 * Uses ISO 4217 currency codes.
 */
enum class CurrencyCode(val code: String) {
    USD("USD"),
    AUD("AUD"),
    GBP("GBP"),
    CAD("CAD"),
    NZD("NZD"),
    EUR("EUR"),
    JPY("JPY"),
    CHF("CHF"),
    SEK("SEK"),
    NOK("NOK"),
    DKK("DKK");

    companion object {
        fun fromCode(code: String): CurrencyCode? {
            return entries.find { it.code == code }
        }
    }
}


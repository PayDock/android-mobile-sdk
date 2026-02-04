package com.paydock.sample.feature.config.models

import java.util.Locale

/**
 * Afterpay locale options for SDK initialisation (testing / override).
 * Matches supported regions: AU, CA, GB, NZ, US.
 * DEFAULT uses the device locale.
 */
enum class AfterpayLocaleOption(val displayName: String) {
    DEFAULT("Default (device)"),
    AU("Australia"),
    CA("Canada"),
    GB("United Kingdom"),
    NZ("New Zealand"),
    US("United States");

    fun toLocale(): Locale? = when (this) {
        DEFAULT -> null
        AU -> Locale("en", "AU")
        CA -> Locale("en", "CA")
        GB -> Locale.UK
        NZ -> Locale("en", "NZ")
        US -> Locale.US
    }

    companion object {
        fun fromLocale(locale: Locale?): AfterpayLocaleOption = when {
            locale == null -> DEFAULT
            locale.country.equals("AU", ignoreCase = true) -> AU
            locale.country.equals("CA", ignoreCase = true) -> CA
            locale.country.equals("GB", ignoreCase = true) -> GB
            locale.country.equals("NZ", ignoreCase = true) -> NZ
            locale == Locale.US -> US
            else -> DEFAULT
        }
    }
}

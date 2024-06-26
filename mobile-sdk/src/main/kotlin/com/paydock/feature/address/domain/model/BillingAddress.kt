package com.paydock.feature.address.domain.model

import java.util.Locale

/**
 * Represents a billing address with its components.
 *
 * @property name The name associated with the billing address (optional).
 * @property addressLine1 The first line of the billing address, typically containing street information.
 * @property addressLine2 The optional second line of the billing address, often used for additional details.
 * @property city The city or locality of the billing address.
 * @property state The state or region of the billing address.
 * @property postalCode The postal or ZIP code of the billing address.
 * @property country The country of the billing address.
 * @property phoneNumber The phone number associated with the billing address (optional).
 */
data class BillingAddress(
    val name: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postalCode: String? = null,
    val country: String? = null,
    val phoneNumber: String? = null
) {
    /**
     * Resolves the ISO country code based on the country name.
     * Returns null if the country name is not recognized.
     */
    val countryCode: String? = Locale.getISOCountries().mapNotNull { countryCode ->
        countryCode
    }.sorted().find { countryCode ->
        val locale = Locale("", countryCode)
        locale.displayCountry == this.country
    }
}

package com.paydock.feature.googlepay.domain.model.integration

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the billing address details returned by the Google Pay API.
 *
 * @property addressLine1 The first line of the street address.
 * @property addressLine2 The second line of the street address.
 * @property addressLine3 The third line of the street address.
 * @property city The city, town, or locality of the address.
 * @property state The administrative area, such as a state, province, or region.
 * @property postcode The postal or ZIP code.
 * @property countryCode The ISO 3166-1 alpha-2 country code.
 */
@Serializable
data class GooglePayBillingAddress(
    @SerialName("address1") val addressLine1: String? = null,
    @SerialName("address2") val addressLine2: String? = null,
    @SerialName("address3") val addressLine3: String? = null,
    @SerialName("locality") val city: String? = null,
    @SerialName("administrativeArea") val state: String? = null,
    @SerialName("postalCode") val postcode: String? = null,
    @SerialName("countryCode") val countryCode: String? = null,
    val name: String? = null,
    val phoneNumber: String? = null
)
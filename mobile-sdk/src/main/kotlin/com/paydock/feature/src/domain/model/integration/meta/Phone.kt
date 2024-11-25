package com.paydock.feature.src.domain.model.integration.meta

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a phone number with a country code in the Mastercard Checkout flow.
 *
 * @property countryCode The country code of the phone number, formatted as a string (e.g., "1" for USA, "44" for UK).
 * @property phone The phone number without the country code.
 */
@Serializable
data class Phone(
    @SerialName("country_code") val countryCode: String? = null,
    val phone: String? = null
)

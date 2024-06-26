package com.paydock.core.domain.model.meta

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a phone number.
 *
 * @property countryCode The country code of the phone number.
 * @property phoneNumber The phone number part of the phone number.
 */
@Serializable
data class PhoneNumber(
    @SerialName("country_code") val countryCode: String,
    @SerialName("phone_number") val phoneNumber: String
)

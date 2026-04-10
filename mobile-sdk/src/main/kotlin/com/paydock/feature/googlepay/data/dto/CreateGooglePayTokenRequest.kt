package com.paydock.feature.googlepay.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the payload data for creating a Google Pay OTT token.
 * This is the data that gets base64 encoded and sent in the payload field.
 *
 * Note: Amount is NOT included in the payload, only in the event response.
 *
 * @property refToken The Google Pay token received from the Google Pay SDK (ref_token in API).
 * @property billing Optional billing address information.
 * @property shipping Optional shipping address information.
 * @property cardInfo Optional card information (scheme and last 4 digits).
 */
@Serializable
internal data class GooglePayPayloadData(
    @SerialName("ref_token") val refToken: String,
    @SerialName("billing") val billing: BillingAddress? = null,
    @SerialName("shipping") val shipping: ShippingAddress? = null,
    @SerialName("card_info") val cardInfo: CardInfo? = null
) {
    @Serializable
    data class BillingAddress(
        @SerialName("address_line1") val addressLine1: String? = null,
        @SerialName("address_line2") val addressLine2: String? = null,
        @SerialName("address_country") val addressCountry: String? = null,
        @SerialName("address_city") val addressCity: String? = null,
        @SerialName("address_postcode") val addressPostcode: String? = null,
        @SerialName("address_state") val addressState: String? = null
    )

    @Serializable
    data class ShippingAddress(
        @SerialName("address_line1") val addressLine1: String? = null,
        @SerialName("address_line2") val addressLine2: String? = null,
        @SerialName("address_country") val addressCountry: String? = null,
        @SerialName("address_city") val addressCity: String? = null,
        @SerialName("address_postcode") val addressPostcode: String? = null,
        @SerialName("address_state") val addressState: String? = null
    )

    @Serializable
    data class CardInfo(
        @SerialName("card_scheme") val cardScheme: String? = null,
        @SerialName("card_number_last4") val cardNumberLast4: String? = null
    )
}

/**
 * Represents the OTT request wrapper for creating a Google Pay payment token.
 * This matches the web client's CreateOTTRequest structure.
 *
 * @property serviceId The service ID for Google Pay.
 * @property serviceType The service type (e.g., "GooglePay").
 * @property serviceGroup The service group (e.g., "wallet").
 * @property payload Base64 encoded JSON string containing the Google Pay payload data.
 * @property payloadFormat The format of the payload (e.g., "encrypted_string").
 */
@Serializable
internal data class CreateGooglePayTokenRequest(
    @SerialName("service_id") val serviceId: String,
    @SerialName("service_type") val serviceType: String,
    @SerialName("service_group") val serviceGroup: String,
    @SerialName("payload") val payload: String,
    @SerialName("payload_format") val payloadFormat: String
)

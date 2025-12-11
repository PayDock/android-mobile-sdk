package com.paydock.feature.zip.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for initializing an external checkout session with Zip.
 *
 * @property gatewayId The gateway ID for processing the payment.
 * @property meta Metadata containing customer and charge information.
 * @property successRedirectUrl URL to redirect to on successful payment.
 * @property errorRedirectUrl URL to redirect to on payment error.
 * @property redirectUrl General redirect URL for the checkout flow.
 */
@Serializable
internal data class ExternalCheckoutRequest(
    @SerialName("gateway_id")
    val gatewayId: String,
    @SerialName("meta")
    val meta: ExternalCheckoutMeta,
    @SerialName("success_redirect_url")
    val successRedirectUrl: String,
    @SerialName("error_redirect_url")
    val errorRedirectUrl: String,
    @SerialName("redirect_url")
    val redirectUrl: String
)

/**
 * Metadata for the external checkout request.
 *
 * @property firstName Customer's first name.
 * @property lastName Customer's last name.
 * @property email Customer's email address.
 * @property tokenize Whether to tokenize the payment source.
 * @property charge Charge details including amount and currency.
 */
@Serializable
internal data class ExternalCheckoutMeta(
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("tokenize")
    val tokenize: Boolean? = null,
    @SerialName("charge")
    val charge: ExternalCheckoutCharge? = null
)

/**
 * Charge details for the external checkout.
 *
 * @property amount The amount to charge.
 * @property currency The currency code (e.g., "AUD").
 * @property billingAddress Optional billing address.
 */
@Serializable
internal data class ExternalCheckoutCharge(
    @SerialName("amount")
    val amount: Double? = null,
    @SerialName("currency")
    val currency: String? = null,
    @SerialName("billing_address")
    val billingAddress: ExternalBillingAddress? = null
)

/**
 * Billing address for the external checkout.
 *
 * @property firstName First name associated with the billing address.
 * @property lastName Last name associated with the billing address.
 * @property addressLine1 First line of the street address.
 * @property addressCity City name.
 * @property addressState State or province.
 * @property addressCountry Country code.
 * @property addressPostcode Postal or ZIP code.
 */
@Serializable
internal data class ExternalBillingAddress(
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    @SerialName("address_line1")
    val addressLine1: String? = null,
    @SerialName("address_city")
    val addressCity: String? = null,
    @SerialName("address_state")
    val addressState: String? = null,
    @SerialName("address_country")
    val addressCountry: String? = null,
    @SerialName("address_postcode")
    val addressPostcode: String? = null
)

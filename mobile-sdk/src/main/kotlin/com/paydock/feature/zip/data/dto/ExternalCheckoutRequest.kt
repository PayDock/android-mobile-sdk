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
 * @property phone Customer's phone number (Optional).
 * @property gender Customer's gender (Optional).
 * @property tokenize Whether to tokenize the payment source.
 * @property charge Charge details including amount and currency.
 * @property statistics Customer statistics for fraud prevention (Optional).
 */
@Serializable
internal data class ExternalCheckoutMeta(
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val gender: String? = null,
    @SerialName("date_of_birth")
    val dateOfBirth: String? = null,
    @SerialName("tokenize")
    val tokenize: Boolean? = null,
    @SerialName("charge")
    val charge: ExternalCheckoutCharge,
    @SerialName("statistics")
    val statistics: ExternalCheckoutStatistics? = null
)

/**
 * Charge details for the external checkout.
 *
 * @property amount The amount to charge.
 * @property currency The currency code (e.g., "AUD").
 * @property shippingType The shipping type (e.g., "delivery", "pickup").
 * @property billingAddress Optional billing address.
 * @property shippingAddress Optional shipping address.
 * @property items Optional list of items in the order.
 */
@Serializable
internal data class ExternalCheckoutCharge(
    @SerialName("amount")
    val amount: Double,
    @SerialName("currency")
    val currency: String,
    @SerialName("shipping_type")
    val shippingType: String? = null,
    @SerialName("billing_address")
    val billingAddress: ExternalBillingAddress? = null,
    @SerialName("shipping_address")
    val shippingAddress: ExternalBillingAddress? = null,
    @SerialName("items")
    val items: List<ExternalCheckoutItem>? = null
)

/**
 * Item details for the checkout.
 *
 * @property name The name of the item.
 * @property amount The price of the item.
 * @property quantity The quantity of the item.
 * @property reference Optional reference or description.
 */
@Serializable
internal data class ExternalCheckoutItem(
    @SerialName("name")
    val name: String,
    @SerialName("amount")
    val amount: String,
    @SerialName("quantity")
    val quantity: Int,
    @SerialName("reference")
    val reference: String? = null
)

/**
 * Customer statistics for fraud prevention.
 *
 * @property accountCreated Date when the account was created.
 * @property salesTotalNumber Total number of sales.
 * @property salesTotalAmount Total amount of sales.
 * @property salesAvgValue Average value of sales.
 * @property salesMaxValue Maximum value of sales.
 * @property refundsTotalAmount Total amount of refunds.
 * @property previousChargeback Whether there was a previous chargeback.
 * @property currency Currency code.
 * @property lastLogin Date of last login.
 */
@Serializable
internal data class ExternalCheckoutStatistics(
    @SerialName("account_created")
    val accountCreated: String? = null,
    @SerialName("sales_total_number")
    val salesTotalNumber: String? = null,
    @SerialName("sales_total_amount")
    val salesTotalAmount: String? = null,
    @SerialName("sales_avg_value")
    val salesAvgValue: String? = null,
    @SerialName("sales_max_value")
    val salesMaxValue: String? = null,
    @SerialName("refunds_total_amount")
    val refundsTotalAmount: String? = null,
    @SerialName("previous_chargeback")
    val previousChargeback: String? = null,
    @SerialName("currency")
    val currency: String? = null,
    @SerialName("last_login")
    val lastLogin: String? = null
)

/**
 * Billing address for the external checkout.
 *
 * @property firstName First name associated with the billing address.
 * @property lastName Last name associated with the billing address.
 * @property addressLine1 First line of the street address.
 * @property addressLine2 The second line of the address entered in the form (optional).
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
    @SerialName("line1")
    val addressLine1: String? = null,
    @SerialName("line2")
    val addressLine2: String? = null,
    @SerialName("city")
    val addressCity: String? = null,
    @SerialName("state")
    val addressState: String? = null,
    @SerialName("country")
    val addressCountry: String? = null,
    @SerialName("postcode")
    val addressPostcode: String? = null
)

package com.paydock.feature.zip.domain.model.integration

import java.math.BigDecimal

/**
 * Configuration for the Zip payment widget.
 *
 * This class holds all the required and optional parameters needed to initialize
 * a Zip payment session, including customer information, order details, and
 * billing/shipping addresses.
 *
 * @property accessToken The access token used for authenticating with the Paydock API.
 * @property gatewayId The gateway ID for processing Zip payments.
 * @property amount The total amount for the transaction.
 * @property currency The currency code (e.g., "AUD") for the transaction.
 * @property firstName The first name of the shopper.
 * @property lastName The last name of the shopper.
 * @property email The email address of the shopper.
 * @property phone Optional phone number of the shopper.
 * @property tokenize Whether to tokenize the payment method (default: true).
 * @property gender Optional gender of the shopper ("male", "female", or other).
 * @property dateOfBirth Optional date of birth in format "YYYY-MM-DD".
 * @property shippingType Optional shipping type (e.g., "delivery", "pickup").
 * @property billing Optional billing address information.
 * @property shipping Optional shipping address information.
 * @property items Optional list of items in the order.
 * @property statistics Optional statistics about the customer's account.
 */
data class ZipWidgetConfig(
    val accessToken: String,
    val gatewayId: String,
    val amount: BigDecimal,
    val currency: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String? = null,
    val tokenize: Boolean = true,
    val gender: String? = null,
    val dateOfBirth: String? = null,
    val shippingType: String? = null,
    val billing: Address? = null,
    val shipping: Address? = null,
    val items: List<Item>? = null,
    val statistics: Statistics? = null
) {
    /**
     * Represents an address for billing or shipping purposes.
     *
     * @property firstName Optional first name associated with the address.
     * @property lastName Optional last name associated with the address.
     * @property line1 Optional first line of the street address.
     * @property line2 Optional second line of the street address.
     * @property city Optional city name.
     * @property state Optional state or province.
     * @property postcode Optional postal or ZIP code.
     * @property country Optional ISO country code (e.g., "AU").
     */
    data class Address(
        val firstName: String? = null,
        val lastName: String? = null,
        val line1: String? = null,
        val line2: String? = null,
        val city: String? = null,
        val state: String? = null,
        val postcode: String? = null,
        val country: String? = null
    )

    /**
     * Represents an item in the order.
     *
     * @property name The name of the item.
     * @property amount The price of the item.
     * @property quantity The quantity of the item.
     * @property reference Optional reference or description for the item.
     */
    data class Item(
        val name: String,
        val amount: String,
        val quantity: Int,
        val reference: String? = null
    )

    /**
     * Represents customer statistics for fraud prevention.
     *
     * @property accountCreated Date when the account was created (YYYY-MM-DD).
     * @property salesTotalNumber Total number of sales.
     * @property salesTotalAmount Total amount of sales.
     * @property salesAvgValue Average value of sales.
     * @property salesMaxValue Maximum value of sales.
     * @property refundsTotalAmount Total amount of refunds.
     * @property previousChargeback Whether there was a previous chargeback.
     * @property currency Currency code.
     * @property lastLogin Date of last login (YYYY-MM-DD).
     */
    data class Statistics(
        val accountCreated: String? = null,
        val salesTotalNumber: String? = null,
        val salesTotalAmount: String? = null,
        val salesAvgValue: String? = null,
        val salesMaxValue: String? = null,
        val refundsTotalAmount: String? = null,
        val previousChargeback: String? = null,
        val currency: String? = null,
        val lastLogin: String? = null
    )
}

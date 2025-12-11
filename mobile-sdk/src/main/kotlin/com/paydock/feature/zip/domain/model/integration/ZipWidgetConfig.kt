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
 * @property shopper Information about the customer making the purchase.
 * @property billing Optional billing address information.
 */
data class ZipWidgetConfig(
    val accessToken: String,
    val gatewayId: String,
    val amount: BigDecimal,
    val currency: String,
    val shopper: Shopper,
    val billing: Address? = null
) {
    /**
     * Represents the shopper's personal information.
     *
     * @property firstName The first name of the shopper.
     * @property lastName The last name of the shopper.
     * @property email The email address of the shopper.
     * @property phone Optional phone number of the shopper.
     */
    data class Shopper(
        val firstName: String,
        val lastName: String,
        val email: String,
        val phone: String? = null
    )

    /**
     * Represents an address for billing or shipping purposes.
     *
     * @property firstName Optional first name associated with the address.
     * @property lastName Optional last name associated with the address.
     * @property line1 Optional first line of the street address.
     * @property line2 Optional second line of the street address.
     * @property city Optional city name.
     * @property state Optional state or province.
     * @property postalCode Optional postal or ZIP code.
     * @property countryCode Optional ISO country code (e.g., "AU").
     */
    data class Address(
        val firstName: String? = null,
        val lastName: String? = null,
        val line1: String? = null,
        val line2: String? = null,
        val city: String? = null,
        val state: String? = null,
        val postalCode: String? = null,
        val countryCode: String? = null
    )
}

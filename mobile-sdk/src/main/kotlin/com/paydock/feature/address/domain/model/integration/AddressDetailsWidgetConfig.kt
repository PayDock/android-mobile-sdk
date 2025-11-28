package com.paydock.feature.address.domain.model.integration

/**
 * Configuration data class for the Address Details Widget.
 *
 * This class holds the necessary parameters to initialize and display the address details form within the Paydock SDK.
 *
 * @property address An optional [BillingAddress] object to pre-populate the address form fields.
 * If null, the form will be displayed with empty fields.
 */
data class AddressDetailsWidgetConfig(
    val address: BillingAddress? = null
)

package com.paydock.feature.address.domain.model.integration

/**
 * Configuration data class for the Address Details Widget.
 *
 * This class holds the necessary parameters to initialize and display the address details form within the Paydock SDK.
 *
 * @property address An optional [BillingAddress] object to pre-populate the address form fields.
 * If null, the form will be displayed with empty fields.
 * @property activePrimaryButton Specifies whether the primary button (e.g., Save) should be enabled by default. If `true`,
 * the button is always enabled, and validation is performed upon clicking it. If `false`, the button remains disabled until
 * all fields are valid. Defaults to true.
 */
data class AddressDetailsWidgetConfig(
    val address: BillingAddress? = null,
    val activePrimaryButton: Boolean = true
)

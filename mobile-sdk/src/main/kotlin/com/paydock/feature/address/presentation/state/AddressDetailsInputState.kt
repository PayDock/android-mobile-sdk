package com.paydock.feature.address.presentation.state

import com.paydock.feature.address.domain.model.integration.BillingAddress

/**
 * UI State that represents address details input and processing state.
 *
 * @property addressLine1 The first line of the address.
 * @property addressLine2 The second line of the address.
 * @property city The city of the address.
 * @property state The state of the address.
 * @property postalCode The postal code of the address.
 * @property country The country of the address.
 */
internal data class AddressDetailsInputState(
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "",
    val state: String = "",
    val postalCode: String = "",
    val country: String = ""
) {
    /**
     * Get the [BillingAddress] representation of the input address.
     */
    val billingAddress: BillingAddress
        get() = BillingAddress(
            addressLine1 = addressLine1,
            addressLine2 = addressLine2,
            city = city,
            state = state,
            postalCode = postalCode,
            country = country
        )

    /**
     * Get the validity status of all input data.
     */
    val isDataValid: Boolean
        get() = addressLine1.isNotBlank() &&
            city.isNotBlank() &&
            state.isNotBlank() &&
            postalCode.isNotBlank() &&
            country.isNotBlank()
}
package com.paydock.feature.address.presentation.state

import com.paydock.feature.address.domain.model.integration.BillingAddress

/**
 * Represents the state of an address details form.
 *
 * @property firstName The first name entered in the form.
 * @property lastName The last name entered in the form.
 * @property addressLine1 The first line of the address entered in the form.
 * @property addressLine2 The second line of the address entered in the form (optional).
 * @property city The city entered in the form.
 * @property state The state or province entered in the form.
 * @property postalCode The postal code entered in the form.
 * @property country The country entered in the form.
 */
internal data class AddressDetailsFormState(
    val firstName: String = "",
    val lastName: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "",
    val state: String = "",
    val postalCode: String = "",
    val country: String = ""
) {
    /**
     * Converts the [AddressDetailsFormState] to a [BillingAddress] domain model.
     * If [addressLine2] is blank, it will be set to null in the resulting [BillingAddress].
     *
     * @return The [BillingAddress] representation of the form state.
     */
    fun toBillingAddress(): BillingAddress = BillingAddress(
        firstName = firstName,
        lastName = lastName,
        addressLine1 = addressLine1,
        addressLine2 = addressLine2.ifBlank { null },
        city = city,
        state = state,
        postalCode = postalCode,
        country = country
    )

    /**
     * Checks if all address fields are blank.
     *
     * @return True if all fields (firstName, lastName, addressLine1, addressLine2, city, state, postalCode, country)
     * are blank, false otherwise.
     */
    fun isEmpty(): Boolean {
        return firstName.isBlank() && lastName.isBlank() && addressLine1.isBlank() &&
            addressLine2.isBlank() && city.isBlank() && state.isBlank() &&
            postalCode.isBlank() && country.isBlank()
    }
}
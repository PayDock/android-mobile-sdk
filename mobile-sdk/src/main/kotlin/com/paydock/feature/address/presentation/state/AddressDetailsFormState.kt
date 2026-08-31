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
    val country: String = "",
    val firstNameErrorOverwrite: Boolean = false,
    val lastNameErrorOverwrite: Boolean = false,
    val addressLine1ErrorOverwrite: Boolean = false,
    val cityErrorOverwrite: Boolean = false,
    val stateErrorOverwrite: Boolean = false,
    val postcodeErrorOverwrite: Boolean = false
) {
    /**
     * Enum representing the required input fields in the address form.
     */
    enum class AddressField {
        FIRST_NAME,
        LAST_NAME,
        ADDRESS_LINE_1,
        CITY,
        STATE,
        POSTCODE,
        COUNTRY
    }

    /**
     * Returns a list of invalid required fields based on the current state, in visual/tab order.
     */
    val invalidFields: List<AddressField>
        get() = mutableListOf<AddressField>().apply {
            if (firstName.isBlank()) add(AddressField.FIRST_NAME)
            if (lastName.isBlank()) add(AddressField.LAST_NAME)
            if (addressLine1.isBlank()) add(AddressField.ADDRESS_LINE_1)
            if (city.isBlank()) add(AddressField.CITY)
            if (state.isBlank()) add(AddressField.STATE)
            if (postalCode.isBlank()) add(AddressField.POSTCODE)
            if (country.isBlank()) add(AddressField.COUNTRY)
        }

    /**
     * Returns the total number of validation errors in the form.
     */
    val errorCount: Int
        get() = invalidFields.size

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
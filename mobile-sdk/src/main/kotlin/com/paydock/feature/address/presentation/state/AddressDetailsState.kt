package com.paydock.feature.address.presentation.state

/**
 * Represents the state of the address details UI.
 *
 * @property formState The state of the address details form, containing the input field values and validation status.
 * @property isDataValid A boolean flag indicating whether the address data is valid and ready to be submitted.
 */
internal data class AddressDetailsState(
    val formState: AddressDetailsFormState = AddressDetailsFormState(),
    val isDataValid: Boolean = false
) {

    /**
     * Represents the billing address details, derived from the form state.
     * This property provides a convenient way to access the address details
     * for billing purposes, assuming that the `AddressDetailsFormState`
     * contains all necessary information.
     */
    val billingAddress: AddressDetailsFormState
        get() = formState
}
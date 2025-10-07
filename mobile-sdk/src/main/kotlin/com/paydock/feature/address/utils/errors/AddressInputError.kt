package com.paydock.feature.address.utils.errors

/**
 * Represents the possible input errors for an address field.
 *
 * This sealed class defines the different types of errors that can occur when validating address input.
 *
 * - [Empty]: Indicates that the input field is empty.
 * - [None]: Indicates that there is no error with the input. This is typically used to represent a valid state.
 */
internal sealed class AddressInputError {
    /**
     * Represents an error indicating that the address input field is empty.
     */
    data object Empty : AddressInputError()

    /**
     * Represents the absence of an error in the address input field.
     * This is used to signify that the input is valid or has not yet been validated.
     */
    data object None : AddressInputError()
}
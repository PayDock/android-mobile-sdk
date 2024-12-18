package com.paydock.feature.card.presentation.utils.errors

/**
 * A sealed class representing possible validation errors for a security code (CVV/CVC).
 *
 * This class defines the various states that indicate whether the security code input is valid,
 * empty, or invalid. It is designed to provide structured error handling for security code validation.
 *
 * ## States:
 * - **Empty**: The security code field is blank or has not been filled in.
 * - **Invalid**: The security code does not meet the required format or length.
 * - **None**: No validation error; the security code is valid.
 */
internal sealed class SecurityCodeError {

    /** Represents a state where the security code field is empty. */
    data object Empty : SecurityCodeError()

    /** Represents a state where the security code is invalid (incorrect format or length). */
    data object Invalid : SecurityCodeError()

    /** Represents a state where there is no error, and the security code is valid. */
    data object None : SecurityCodeError()
}

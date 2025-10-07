package com.paydock.feature.address.utils.validators

import com.paydock.feature.address.utils.errors.AddressInputError

/**
 * An internal object that provides validation functions for address input fields.
 * This validator helps ensure that address data meets specific criteria before being processed.
 */
internal object AddressValidator {
    /**
     * Validates the input value for an address field.
     *
     * This function checks if the input value, after trimming whitespace, is blank.
     * It only returns an [AddressInputError.Empty] if the user has interacted with the field
     * and the input is blank. Otherwise, it returns [AddressInputError.None].
     *
     * @param value The string value to validate.
     * @param hasUserInteracted A boolean flag indicating whether the user has interacted with the input field.
     * @return An [AddressInputError] indicating the validation result. Returns [AddressInputError.Empty]
     * if the trimmed input is blank and the user has interacted, otherwise [AddressInputError.None].
     */
    fun validateInput(value: String, hasUserInteracted: Boolean): AddressInputError {
        val trimmedInput = value.trim()

        return when {
            trimmedInput.isBlank() && hasUserInteracted -> AddressInputError.Empty
            else -> AddressInputError.None
        }
    }
}

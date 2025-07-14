package com.paydock.feature.card.presentation.utils.errors

/**
 * Represents the possible errors that can occur when validating a cardholder's name.
 *
 * This sealed class is used to identify and categorize validation errors related to the
 * cardholder's name input field.
 */
internal sealed class CardHolderNameError {

    /**
     * Indicates that the cardholder name field is empty.
     */
    data object Empty : CardHolderNameError()

    /**
     * Indicates that the cardholder name does not pass Luhn validation.
     *
     * This is typically used when the name format or structure is checked against the Luhn algorithm.
     */
    data object InvalidLuhn : CardHolderNameError()

    /**
     * Indicates that the cardholder name contains invalid characters.
     *
     * This error occurs when the name includes characters that are not allowed,
     * such as numbers or special symbols, depending on the validation rules.
     * It ensures that the cardholder's name adheres to the expected character set.
     */
    data object InvalidFormat : CardHolderNameError()

    /**
     * Represents the absence of any validation error. This state implies that the cardholder name is valid.
     */
    data object None : CardHolderNameError()
}

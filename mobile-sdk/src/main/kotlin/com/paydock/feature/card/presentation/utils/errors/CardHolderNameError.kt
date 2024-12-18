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
     * Represents the absence of any validation error. This state implies that the cardholder name is valid.
     */
    data object None : CardHolderNameError()
}

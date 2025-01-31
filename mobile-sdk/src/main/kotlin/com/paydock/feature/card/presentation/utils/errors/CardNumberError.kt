package com.paydock.feature.card.presentation.utils.errors

/**
 * Represents the various error states that can occur during card number validation.
 *
 * This sealed class defines the possible outcomes of a card number validation process.
 * Each nested class represents a specific error condition, allowing for targeted
 * handling and display of appropriate error messages in the UI.
 */
internal sealed class CardNumberError {

    /**
     * Indicates that the card number field is empty.
     * This error typically occurs when the user has interacted with the field
     * but has not entered any input.
     */
    data object Empty : CardNumberError()

    /**
     * Indicates that the card number is invalid because it fails the Luhn algorithm check.
     * The Luhn algorithm is a checksum formula commonly used to validate
     * the integrity of credit card numbers.
     */
    data object InvalidLuhn : CardNumberError()

    /**
     * Indicates that the card number has an invalid length for the detected card scheme.
     * This error occurs when the number of digits in the card number does not match
     * the expected length range for the identified card scheme (e.g., Visa, Mastercard).
     */
    data object InvalidLength : CardNumberError()

    /**
     * Indicates that the card number is invalid because it belongs to an unsupported card scheme.
     * This error occurs when the detected card scheme is not included in the list of
     * supported card schemes for the payment integration.
     */
    data object UnsupportedCardScheme : CardNumberError()

    /**
     * Represents the absence of any validation errors.
     * This state indicates that the card number is valid and can be processed.
     */
    data object None : CardNumberError()
}
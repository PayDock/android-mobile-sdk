package com.paydock.feature.card.presentation.utils.errors

/**
 * Represents the various error states for validating a card number.
 *
 * This sealed class defines the possible outcomes of a card number validation process.
 * Each state corresponds to a specific validation scenario, making it easier to handle
 * and display appropriate error messages or feedback in the UI.
 */
internal sealed class CardNumberError {

    /**
     * Indicates that the card number field is empty.
     */
    data object Empty : CardNumberError()

    /**
     * Indicates that the card number is invalid due to failing the Luhn algorithm check.
     * The Luhn algorithm is commonly used for validating credit card numbers.
     */
    data object InvalidLuhn : CardNumberError()

    /**
     * Represents the absence of any error. This state implies that the card number is valid.
     */
    data object None : CardNumberError()
}
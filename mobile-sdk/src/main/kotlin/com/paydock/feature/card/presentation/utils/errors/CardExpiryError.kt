package com.paydock.feature.card.presentation.utils.errors

/**
 * A sealed class representing the possible validation errors for a credit card's expiry date.
 *
 * This class is used to handle different states of errors that may occur when validating the
 * expiry date of a credit card.
 */
internal sealed class CardExpiryError {

    /**
     * Represents the state where the expiry date field is empty.
     */
    data object Empty : CardExpiryError()

    /**
     * Represents the state where the provided expiry date has already passed.
     */
    data object Expired : CardExpiryError()

    /**
     * Represents the state where the expiry date format is invalid.
     * For example, if the format is not in "MM/YY" or is incomplete.
     */
    data object InvalidFormat : CardExpiryError()

    /**
     * Represents the state where no error is present, indicating a valid expiry date.
     */
    data object None : CardExpiryError()
}
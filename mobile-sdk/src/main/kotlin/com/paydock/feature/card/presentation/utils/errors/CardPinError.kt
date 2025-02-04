package com.paydock.feature.card.presentation.utils.errors

/**
 * Represents the various states of validation errors for a card PIN.
 *
 * This sealed class is used to identify and handle potential errors during the
 * validation of a card PIN input.
 */
internal sealed class CardPinError {

    /**
     * Indicates that the card PIN field is empty.
     *
     * This error occurs when no input is provided in the card PIN field.
     */
    data object Empty : CardPinError()

    /**
     * Indicates that the gift card pib is invalid.
     *
     * This error is triggered when the input fails validation checks,
     * such as format or length constraints.
     */
    data object Invalid : CardPinError()

    /**
     * Represents the absence of any validation error.
     *
     * This state implies that the card PIN has been provided and is valid.
     */
    data object None : CardPinError()
}
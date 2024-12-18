package com.paydock.feature.card.presentation.utils.errors

/**
 * Represents the various states of validation errors for a gift card number.
 *
 * This sealed class is used to categorize errors that may occur when validating a gift card number,
 * providing specific error types for better handling and user feedback.
 */
internal sealed class GiftCardNumberError {

    /**
     * Indicates that the gift card number field is empty.
     *
     * This error occurs when no input is provided in the gift card number field.
     */
    data object Empty : GiftCardNumberError()

    /**
     * Indicates that the gift card number is invalid.
     *
     * This error is triggered when the input fails validation checks,
     * such as format or length constraints.
     */
    data object Invalid : GiftCardNumberError()

    /**
     * Represents the absence of any validation error.
     *
     * This state implies that the gift card number has passed all validation checks
     * and is considered valid.
     */
    data object None : GiftCardNumberError()
}
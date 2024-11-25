package com.paydock.feature.paypal.vault.presentation.utils

/**
 * Enum representing different cancellation statuses that can occur during the payment process.
 */
internal enum class CancellationStatus {
    /**
     * Indicates that the cancellation was due to invalid parameters being provided.
     *
     * This may occur if required parameters for the payment process are missing or incorrect.
     */
    INVALID_PARAMS,

    /**
     * Represents a cancellation initiated by the user, typically when the user navigates back
     * or manually closes the payment process.
     */
    USER_INITIATED
}
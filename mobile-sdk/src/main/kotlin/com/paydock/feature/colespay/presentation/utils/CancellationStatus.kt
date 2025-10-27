package com.paydock.feature.colespay.presentation.utils

/**
 * Enum representing the different types of cancellation statuses for Coles Pay transactions.
 */
internal enum class CancellationStatus {
    /**
     * Represents a cancellation initiated by the user, typically when the user navigates back
     * or closes the payment process.
     */
    USER_INITIATED,

    /**
     * Represents a cancellation triggered by the web page (e.g., "Close and return to store").
     */
    PAGE_CLOSED
}
package com.paydock.feature.colespay.presentation.utils

import android.content.Intent

/**
 * This object defines constants used as keys for Coles Pay-specific extras in an Intent.
 */
private object ColesPayIntent {
    const val CANCELLATION_STATUS = "COLES_PAY_CANCELLATION_STATUS" // Key for the Coles Pay cancellation status.
    const val ORDER_ID = "COLES_PAY_ORDER_ID" // Key for the Coles Pay order ID.
    const val CLIENT_ID = "COLES_PAY_CLIENT_ID" // Key for the Coles Pay client ID.
}

/**
 * Extension function to add the Coles Pay cancellation status to an Intent.
 *
 * @param status The cancellation status to be added.
 * @return The updated Intent with the cancellation status included.
 */
internal fun Intent.putCancellationStatusExtra(status: CancellationStatus): Intent =
    putExtra(ColesPayIntent.CANCELLATION_STATUS, status.name)

/**
 * Extension function to retrieve the Coles Pay cancellation status from an Intent.
 *
 * @return The cancellation status if present, or null if parsing fails.
 */
internal fun Intent.getCancellationStatusExtra(): CancellationStatus? = try {
    getStringExtra(ColesPayIntent.CANCELLATION_STATUS)?.let { enumValueOf<CancellationStatus>(it) }
} catch (_: Exception) {
    null // Returns null if the cancellation status cannot be parsed.
}

/**
 * Extension function to add the Coles Pay order ID to an Intent.
 *
 * @param orderId The order ID to be added.
 * @return The updated Intent with the order ID included.
 */
internal fun Intent.putOrderIdExtra(orderId: String): Intent =
    putExtra(ColesPayIntent.ORDER_ID, orderId)

/**
 * Extension function to retrieve the Coles Pay order ID from an Intent.
 *
 * @return The order ID if present, or null otherwise.
 */
internal fun Intent.getOrderIdExtra(): String? =
    getStringExtra(ColesPayIntent.ORDER_ID)

/**
 * Extension function to add the Coles Pay client ID to an Intent.
 *
 * @param clientId The client ID to be added.
 * @return The updated Intent with the client ID included.
 */
internal fun Intent.putClientIdExtra(clientId: String): Intent =
    putExtra(ColesPayIntent.CLIENT_ID, clientId)

/**
 * Extension function to retrieve the Coles Pay client ID from an Intent.
 *
 * @return The client ID if present, or null otherwise.
 */
internal fun Intent.getClientIdExtra(): String? =
    getStringExtra(ColesPayIntent.CLIENT_ID)

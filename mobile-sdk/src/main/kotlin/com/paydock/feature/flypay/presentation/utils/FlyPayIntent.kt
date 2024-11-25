package com.paydock.feature.flypay.presentation.utils

import android.content.Intent

/**
 * This object defines constants used as keys for FlyPay-specific extras in an Intent.
 */
private object FlyPayIntent {
    const val CANCELLATION_STATUS = "FLYPAY_CANCELLATION_STATUS" // Key for the FlyPay cancellation status.
    const val ORDER_ID = "FLYPAY_ORDER_ID" // Key for the FlyPay order ID.
    const val CLIENT_ID = "FLYPAY_CLIENT_ID" // Key for the FlyPay client ID.
}

/**
 * Extension function to add the FlyPay cancellation status to an Intent.
 *
 * @param status The cancellation status to be added.
 * @return The updated Intent with the cancellation status included.
 */
internal fun Intent.putCancellationStatusExtra(status: CancellationStatus): Intent =
    putExtra(FlyPayIntent.CANCELLATION_STATUS, status.name)

/**
 * Extension function to retrieve the FlyPay cancellation status from an Intent.
 *
 * @return The cancellation status if present, or null if parsing fails.
 */
internal fun Intent.getCancellationStatusExtra(): CancellationStatus? = try {
    getStringExtra(FlyPayIntent.CANCELLATION_STATUS)?.let { enumValueOf<CancellationStatus>(it) }
} catch (_: Exception) {
    null // Returns null if the cancellation status cannot be parsed.
}

/**
 * Extension function to add the FlyPay order ID to an Intent.
 *
 * @param orderId The order ID to be added.
 * @return The updated Intent with the order ID included.
 */
internal fun Intent.putOrderIdExtra(orderId: String): Intent =
    putExtra(FlyPayIntent.ORDER_ID, orderId)

/**
 * Extension function to retrieve the FlyPay order ID from an Intent.
 *
 * @return The order ID if present, or null otherwise.
 */
internal fun Intent.getOrderIdExtra(): String? =
    getStringExtra(FlyPayIntent.ORDER_ID)

/**
 * Extension function to add the FlyPay client ID to an Intent.
 *
 * @param clientId The client ID to be added.
 * @return The updated Intent with the client ID included.
 */
internal fun Intent.putClientIdExtra(clientId: String): Intent =
    putExtra(FlyPayIntent.CLIENT_ID, clientId)

/**
 * Extension function to retrieve the FlyPay client ID from an Intent.
 *
 * @return The client ID if present, or null otherwise.
 */
internal fun Intent.getClientIdExtra(): String? =
    getStringExtra(FlyPayIntent.CLIENT_ID)

package com.paydock.feature.paypal.checkout.presentation.utils

import android.content.Intent

/**
 * This object defines constants used as keys for PayPal-specific extras in an Intent.
 */
private object PayPalIntent {
    const val CALLBACK_URL = "PAYPAL_CALLBACK_URL" // Key for the PayPal callback URL.
    const val CANCELLATION_STATUS = "PAYPAL_CANCELLATION_STATUS" // Key for the cancellation status.
    const val DECODED_URL = "PAYPAL_DECODED_URL" // Key for the decoded PayPal redirect URL.
}

/**
 * Extension function to add the PayPal callback URL to an Intent.
 *
 * @param url The PayPal callback URL to be added.
 * @return The updated Intent with the callback URL included.
 */
internal fun Intent.putCallbackUrlExtra(url: String): Intent =
    putExtra(PayPalIntent.CALLBACK_URL, url)

/**
 * Extension function to retrieve the PayPal callback URL from an Intent.
 *
 * @return The PayPal callback URL if present, or null otherwise.
 */
internal fun Intent.getCallbackUrlExtra(): String? =
    getStringExtra(PayPalIntent.CALLBACK_URL)

/**
 * Extension function to add the cancellation status to an Intent.
 *
 * @param status The cancellation status to be added.
 * @return The updated Intent with the cancellation status included.
 */
internal fun Intent.putCancellationStatusExtra(status: CancellationStatus): Intent =
    putExtra(PayPalIntent.CANCELLATION_STATUS, status.name)

/**
 * Extension function to retrieve the cancellation status from an Intent.
 *
 * @return The cancellation status if present, or null if parsing fails.
 */
internal fun Intent.getCancellationStatusExtra(): CancellationStatus? = try {
    getStringExtra(PayPalIntent.CANCELLATION_STATUS)?.let { enumValueOf<CancellationStatus>(it) }
} catch (_: Exception) {
    null
}

/**
 * Extension function to add the decoded PayPal redirect URL to an Intent.
 *
 * @param url The decoded PayPal URL to be added.
 * @return The updated Intent with the decoded URL included.
 */
internal fun Intent.putDecodedUrlExtra(url: String): Intent =
    putExtra(PayPalIntent.DECODED_URL, url)

/**
 * Extension function to retrieve the decoded PayPal URL from an Intent.
 *
 * @return The decoded PayPal URL if present, or null otherwise.
 */
internal fun Intent.getDecodedUrlExtra(): String? =
    getStringExtra(PayPalIntent.DECODED_URL)

package com.paydock.core.presentation.extensions

import android.content.Intent

/**
 * This object defines common constants used as keys for extras in an Intent.
 */
private object CoreIntent {
    const val STATUS = "STATUS" // Key for the status code.
    const val MESSAGE = "MESSAGE" // Key for the failure message.
}

/**
 * Extension function to add the status code to an Intent.
 *
 * @param status The status code to be added, or null to skip adding.
 * @return The updated Intent with the status code, or the same Intent if status is null.
 */
internal fun Intent.putStatusExtra(status: Int?): Intent =
    status?.let { putExtra(CoreIntent.STATUS, status) } ?: this

/**
 * Extension function to retrieve the status code from an Intent.
 *
 * @return The error status code, or -1 if not present.
 */
internal fun Intent.getStatusExtra(): Int =
    getIntExtra(CoreIntent.STATUS, -1)

/**
 * Extension function to add the failure message to an Intent.
 *
 * @param message The failure message to be added.
 * @return The updated Intent with the failure message included.
 */
internal fun Intent.putMessageExtra(message: String): Intent =
    putExtra(CoreIntent.MESSAGE, message)

/**
 * Extension function to retrieve the failure message from an Intent.
 *
 * @return The failure message if present, or a default error message if not present.
 */
internal fun Intent.getMessageExtra(defaultErrorMessage: String): String =
    getStringExtra(CoreIntent.MESSAGE) ?: defaultErrorMessage

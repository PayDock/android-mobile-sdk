package com.paydock.feature.paypal.vault.presentation.utils

import android.content.Intent

/**
 * Object containing constants used as keys for Intent extras in the PayPal Vault flow.
 */
private object PayPalVaultIntent {
    const val SETUP_TOKEN = "PAYPAL_SETUP_TOKEN" // Key for the PayPal setup token extra
    const val CLIENT_ID = "CLIENT_ID" // Key for the client ID extra
    const val CANCELLATION_STATUS = "CANCELLATION_STATUS" // Key for the cancellation status extra
}

/**
 * Extension function to add the PayPal setup token to an Intent.
 *
 * @param setupToken The PayPal setup token to be added.
 * @return The updated Intent with the setup token included.
 */
internal fun Intent.putSetupTokenExtra(setupToken: String): Intent =
    putExtra(PayPalVaultIntent.SETUP_TOKEN, setupToken)

/**
 * Extension function to retrieve the PayPal setup token from an Intent.
 *
 * @return The setup token if present, or null if it is not found.
 */
internal fun Intent.getSetupTokenExtra(): String? =
    getStringExtra(PayPalVaultIntent.SETUP_TOKEN)

/**
 * Extension function to add the client ID to an Intent.
 *
 * @param clientId The client ID to be added.
 * @return The updated Intent with the client ID included.
 */
internal fun Intent.putClientIdExtra(clientId: String): Intent =
    putExtra(PayPalVaultIntent.CLIENT_ID, clientId)

/**
 * Extension function to retrieve the client ID from an Intent.
 *
 * @return The client ID if present, or null if it is not found.
 */
internal fun Intent.getClientIdExtra(): String? =
    getStringExtra(PayPalVaultIntent.CLIENT_ID)

/**
 * Extension function to add the cancellation status to an Intent.
 *
 * @param status The cancellation status to be added (e.g. [CancellationStatus.USER_INITIATED], [CancellationStatus.INVALID_PARAMS]).
 * @return The updated Intent with the cancellation status included.
 */
internal fun Intent.putCancellationStatusExtra(status: CancellationStatus): Intent =
    putExtra(PayPalVaultIntent.CANCELLATION_STATUS, status.name)

/**
 * Extension function to retrieve the cancellation status from an Intent.
 *
 * @return The [CancellationStatus] if present, or null if parsing fails or the status is not found.
 */
internal fun Intent.getCancellationStatusExtra(): CancellationStatus? = try {
    getStringExtra(PayPalVaultIntent.CANCELLATION_STATUS)?.let { enumValueOf<CancellationStatus>(it) }
} catch (_: Exception) {
    null
}
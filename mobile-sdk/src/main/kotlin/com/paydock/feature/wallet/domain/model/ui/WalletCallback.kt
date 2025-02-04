package com.paydock.feature.wallet.domain.model.ui

/**
 * Data class representing the wallet callback response.
 *
 * This class holds the details of the callback returned from a wallet-related transaction, including
 * the callback ID, status of the charge, the callback URL, and a reference token.
 *
 * @property callbackId The ID of the callback, which can be `null` if not present.
 * @property status The status of the charge (e.g., "SUCCESS", "FAILED"), which can be `null`.
 * @property callbackUrl The URL to which the callback will be sent, which can be `null`.
 * @property refToken The reference token associated with the transaction, which can be `null`.
 */
internal data class WalletCallback(
    val callbackId: String?,
    val status: String?,
    val callbackUrl: String?,
    val refToken: String?
)

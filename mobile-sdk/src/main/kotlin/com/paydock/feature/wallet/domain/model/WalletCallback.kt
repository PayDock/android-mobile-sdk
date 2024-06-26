package com.paydock.feature.wallet.domain.model

/**
 * Represents callback information related to wallet operations.
 *
 * @property callbackId The unique identifier for the wallet callback.
 * @property status The status of the wallet callback, indicating the result or current state.
 * @property callbackUrl The URL to which the wallet callback notifications should be sent.
 * @property refToken The wallet reference token.
 */
data class WalletCallback(
    val callbackId: String?,
    val status: String?,
    val callbackUrl: String?,
    val refToken: String?
)

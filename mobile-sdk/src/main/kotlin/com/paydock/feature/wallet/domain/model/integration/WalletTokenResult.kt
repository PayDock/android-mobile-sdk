package com.paydock.feature.wallet.domain.model.integration

/**
 * Represents the result of a wallet tokenization operation.
 *
 * @property token The unique identifier (token) representing the wallet for payment processing.
 */
data class WalletTokenResult(
    val token: String
)
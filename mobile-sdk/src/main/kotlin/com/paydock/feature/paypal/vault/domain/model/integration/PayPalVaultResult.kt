package com.paydock.feature.paypal.vault.domain.model.integration

/**
 * Represents the result of a successful PayPal vaulting operation.
 *
 * This data class contains the token that is generated after a PayPal vaulting process.
 * The token is used to authorize or process future payments with the PayPal account.
 *
 * @property token The token generated from the PayPal vaulting process.
 * @property email The email address associated with the PayPal account.
 */
data class PayPalVaultResult(
    val token: String,
    val email: String
)
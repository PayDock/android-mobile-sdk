package com.paydock.feature.paypal.vault.presentation.state

import com.paydock.core.domain.error.exceptions.PayPalVaultException

/**
 * Represents the state of the PayPal vaulting process.
 *
 * This data class holds the current state of the vaulting operation, including the authentication token,
 * setup token, client ID, payment token, vault result, loading state, and any potential errors encountered during the process.
 *
 * @property authToken The authentication token used for the vaulting session, or `null` if not available.
 * @property setupToken The setup token used for initiating the PayPal vaulting process, or `null` if not available.
 * @property clientId The PayPal client ID used for the vaulting request, or `null` if not available.
 * @property paymentToken The payment token generated after successful vaulting, or `null` if not available.
 * @property isLoading Indicates whether the vaulting operation is currently in progress.
 * @property error The error encountered during the PayPal vaulting process, if any.
 */
internal data class PayPalVaultState(
    val authToken: String? = null,
    val setupToken: String? = null,
    val clientId: String? = null,
    val paymentToken: String? = null,
    val isLoading: Boolean = false,
    val error: PayPalVaultException? = null
)
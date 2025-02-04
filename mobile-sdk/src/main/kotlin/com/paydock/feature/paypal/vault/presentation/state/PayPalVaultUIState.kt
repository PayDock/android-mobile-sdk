package com.paydock.feature.paypal.vault.presentation.state

import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.feature.paypal.core.domain.model.ui.PayPalPaymentTokenDetails

/**
 * Represents the various UI states for the PayPal Vault feature.
 */
internal sealed class PayPalVaultUIState {

    /**
     * Represents an idle state, where no action is currently in progress.
     */
    data object Idle : PayPalVaultUIState()

    /**
     * Represents a loading state, typically displayed while the vaulting process is underway.
     */
    data object Loading : PayPalVaultUIState()

    /**
     * Represents a state that triggers the launch of a new PayPal intent.
     *
     * @param clientId The client ID associated with the PayPal setup.
     * @param setupToken The setup token needed to initiate the PayPal vaulting process.
     */
    data class LaunchIntent(val clientId: String, val setupToken: String) : PayPalVaultUIState()

    /**
     * Represents a successful vaulting operation.
     *
     * @param details The PayPal payment token details received upon successful completion of the vaulting process.
     */
    data class Success(val details: PayPalPaymentTokenDetails) : PayPalVaultUIState()

    /**
     * Represents an error state, containing details of the exception encountered.
     *
     * @param exception The exception that occurred during the vaulting process.
     */
    data class Error(val exception: SdkException) : PayPalVaultUIState()
}
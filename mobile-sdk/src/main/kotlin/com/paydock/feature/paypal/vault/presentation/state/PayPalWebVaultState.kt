package com.paydock.feature.paypal.vault.presentation.state

import com.paypal.android.corepayments.PayPalSDKError

/**
 * Represents the different states of the PayPal web vaulting process.
 *
 * This sealed class allows modeling various outcomes of the PayPal vault flow, ensuring all
 * possible states are handled explicitly.
 */
internal sealed class PayPalWebVaultState {

    /**
     * State representing the idle state of the PayPal vaulting process.
     * This state is typically used before any vaulting action has been initiated.
     */
    data object Idle : PayPalWebVaultState()

    /**
     * State representing the user-initiated cancellation of the PayPal vaulting process.
     * This occurs when the user exits or cancels the PayPal vault flow.
     */
    data object Canceled : PayPalWebVaultState()

    /**
     * State representing a failure during the PayPal vaulting process.
     *
     * @param error An instance of [PayPalSDKError] containing details about the failure.
     */
    data class Failure(val error: PayPalSDKError) : PayPalWebVaultState()

    /**
     * State representing the successful completion of the PayPal vaulting process.
     *
     * @param approvalSessionId The session ID provided upon successful approval of the PayPal vault.
     */
    data class Success(val approvalSessionId: String) : PayPalWebVaultState()
}
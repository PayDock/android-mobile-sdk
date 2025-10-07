package com.paydock.feature.paypal.checkout.presentation.state

import com.paypal.android.corepayments.PayPalSDKError

/**
 * Represents the different states of the PayPal web checkout process.
 *
 * This sealed class allows modeling various outcomes of the PayPal checkout flow, ensuring all
 * possible states are handled explicitly.
 */
internal sealed class PayPalWebCheckoutState {

    /**
     * State representing the idle or loading state of the PayPal checkout process.
     * This state is typically used before any checkout action has been initiated, or while waiting
     * for the SDK UI to present.
     */
    data object Idle : PayPalWebCheckoutState()

    /**
     * State representing the user-initiated cancellation of the PayPal checkout process.
     * This occurs when the user exits or cancels the PayPal checkout flow.
     */
    data object Canceled : PayPalWebCheckoutState()

    /**
     * State representing a failure during the PayPal checkout process.
     *
     * @param error An instance of [PayPalSDKError] containing details about the failure.
     */
    data class Failure(val error: PayPalSDKError) : PayPalWebCheckoutState()

    /**
     * State representing the successful completion of the PayPal checkout process.
     *
     * @param paymentMethodId The PayPal Order ID to be used for capture.
     * @param payerId The PayPal Payer ID associated with the approval.
     */
    data class Success(val paymentMethodId: String, val payerId: String) : PayPalWebCheckoutState()
}

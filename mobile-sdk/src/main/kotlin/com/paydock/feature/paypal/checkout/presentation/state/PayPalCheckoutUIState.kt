package com.paydock.feature.paypal.checkout.presentation.state

import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.model.ui.WalletCallback

/**
 * Represents the various UI states of the PayPal Checkout flow in the SDK.
 *
 * This sealed class encapsulates all possible states, enabling the app to respond to changes
 * in the checkout process efficiently and accurately.
 */
internal sealed class PayPalCheckoutUIState {

    /**
     * The default state when no PayPal checkout action is being performed.
     */
    data object Idle : PayPalCheckoutUIState()

    /**
     * The state indicating that a PayPal checkout process is currently in progress.
     * This is typically used to show a loading indicator.
     */
    data object Loading : PayPalCheckoutUIState()

    /**
     * The state representing the need to launch a PayPal checkout intent.
     *
     * @property callbackData The callback data required for launching the checkout process.
     */
    data class LaunchIntent(val callbackData: WalletCallback) : PayPalCheckoutUIState()

    /**
     * The state representing the capture phase of the PayPal checkout process.
     *
     * @property payPalToken The token returned by PayPal after the authorization process.
     * @property payerId The unique identifier of the payer, returned by PayPal.
     */
    data class Capture(
        var payPalToken: String,
        var payerId: String
    ) : PayPalCheckoutUIState()

    /**
     * The state indicating that the PayPal checkout process completed successfully.
     *
     * @property chargeData The charge response data returned upon successful payment capture.
     */
    data class Success(val chargeData: ChargeResponse) : PayPalCheckoutUIState()

    /**
     * The state representing an error that occurred during the PayPal checkout process.
     *
     * @property exception The exception detailing the error.
     */
    data class Error(val exception: SdkException) : PayPalCheckoutUIState()
}

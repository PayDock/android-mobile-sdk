package com.paydock.feature.paypal.checkout.presentation.state

import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse

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
     * The state representing the need to launch the PayPal Web SDK checkout flow.
     *
     * @property clientId The PayPal CLIENT_ID required to initialize the SDK.
     * @property orderId The PayPal ORDER_ID to approve.
     */
    data class LaunchIntent(val clientId: String, val orderId: String) : PayPalCheckoutUIState()

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

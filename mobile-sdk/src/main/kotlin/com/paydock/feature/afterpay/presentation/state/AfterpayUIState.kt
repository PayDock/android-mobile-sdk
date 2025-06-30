package com.paydock.feature.afterpay.presentation.state

import android.content.Intent
import com.afterpay.android.model.ShippingOptionUpdateResult
import com.afterpay.android.model.ShippingOptionsSuccessResult
import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse

/**
 * Represents the various UI states for the Afterpay flow.
 *
 * This sealed class is used to manage and represent the state of the Afterpay UI,
 * enabling the composable to react appropriately to changes in state.
 * Each subclass represents a distinct phase in the UI's lifecycle.
 */
internal sealed class AfterpayUIState {

    /**
     * Represents the idle state when no actions have been performed yet.
     *
     * This state is the default and indicates that the UI is not currently processing
     * or displaying any data related to the Afterpay flow.
     */
    data object Idle : AfterpayUIState()

    /**
     * Represents the loading state when a process is actively being performed.
     *
     * This state is used when the UI needs to display a loading indicator,
     * such as when communicating with the Afterpay SDK or backend services.
     */
    data object Loading : AfterpayUIState()

    /**
     * Represents the state when an Intent to launch the Afterpay checkout is available.
     *
     * This state is used to signal that the UI should launch the provided [checkoutIntent]
     * to start the Afterpay checkout process.
     *
     * @property checkoutIntent The [Intent] to be used to launch the Afterpay checkout.
     */
    data class LaunchIntent(val checkoutIntent: Intent) : AfterpayUIState()

    /**
     * Event representing the result of providing a checkout token.
     *
     * This event is triggered when the SDK requires a token for the checkout process.
     *
     * @property tokenResult The result containing the checkout token, which may be
     * a success or failure.
     */
    data class ProvideCheckoutTokenResult(val tokenResult: Result<String>) : AfterpayUIState()

    /**
     * Event representing the result of providing shipping options.
     *
     * This event is triggered when the SDK receives a request for available shipping options
     * after the user has provided their address during checkout.
     *
     * @property shippingOptionsResult The result containing a list of valid shipping options.
     */
    data class ProvideShippingOptionsResult(val shippingOptionsResult: ShippingOptionsSuccessResult) : AfterpayUIState()

    /**
     * Event representing the result of updating a selected shipping option.
     *
     * This event is triggered when the user selects or changes a shipping option,
     * and the SDK processes the update.
     *
     * @property shippingOptionUpdateResult The result of the update process for the selected shipping option.
     * This may be null if no update result is provided.
     */
    data class ProvideShippingOptionUpdateResult(
        val shippingOptionUpdateResult: ShippingOptionUpdateResult?
    ) : AfterpayUIState()

    /**
     * Represents a successful operation in the Afterpay flow.
     *
     * This state indicates that a charge or transaction has been completed successfully,
     * and includes the resulting `ChargeResponse` data.
     *
     * @property chargeData The response data associated with the successful charge.
     */
    data class Success(val chargeData: ChargeResponse) : AfterpayUIState()

    /**
     * Represents an error state in the Afterpay flow.
     *
     * This state is used when an operation fails due to an exception,
     * providing the exception details for error handling and display purposes.
     *
     * @property exception The exception containing details about the failure.
     */
    data class Error(val exception: SdkException) : AfterpayUIState()

    /**
     * Represents a state where an error occurred and the Afterpay transaction is pending decline.
     *
     * This state is used when an error is encountered during the Afterpay flow, and the transaction
     * needs to be explicitly declined. It holds the [SdkException] that caused the error.
     * The UI should typically display an error message and potentially offer a way to retry or cancel.
     *
     * @property exception The [SdkException] that led to the pending decline state.
     */
    data class PendingDeclineOnError(val exception: SdkException) : AfterpayUIState()
}
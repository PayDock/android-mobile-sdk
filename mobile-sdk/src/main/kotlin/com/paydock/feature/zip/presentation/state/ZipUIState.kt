package com.paydock.feature.zip.presentation.state

import com.paydock.core.domain.error.exceptions.SdkException

/**
 * Represents the various UI states for the Zip payment widget.
 *
 * This sealed class provides a structured way to handle different states in the Zip payment flow,
 * ensuring type safety and clear state management.
 */
internal sealed class ZipUIState {

    /**
     * Represents the idle state where no operation is in progress.
     *
     * This state is the initial state or a reset state when the Zip flow is not active.
     */
    data object Idle : ZipUIState()

    /**
     * Represents the loading state where an operation is in progress.
     *
     * Use this state to indicate to the user that they should wait until the operation completes.
     */
    data object Loading : ZipUIState()

    /**
     * Represents a state where the Zip checkout URL is ready to be launched.
     *
     * @property checkoutUrl The URL to redirect the user to for completing Zip checkout.
     * @property checkoutToken The checkout token for subsequent API calls.
     */
    data class LaunchCheckout(
        val checkoutUrl: String,
        val checkoutToken: String
    ) : ZipUIState()

    /**
     * Represents a successful state where the Zip payment has been completed.
     *
     * @property token The payment source token generated after successful Zip checkout.
     */
    data class Success(val token: String) : ZipUIState()

    /**
     * Represents an error state where an operation has failed.
     *
     * @property exception The exception that encapsulates the error details.
     */
    data class Error(val exception: SdkException) : ZipUIState()
}

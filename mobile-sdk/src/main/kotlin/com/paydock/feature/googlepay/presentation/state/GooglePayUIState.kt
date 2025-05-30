package com.paydock.feature.googlepay.presentation.state

import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.PaymentData
import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse

/**
 * Represents the UI state for Google Pay interactions.
 *
 * This sealed class encapsulates various states the UI can be in during a Google Pay transaction,
 * including idle, loading, error, and success states.
 */
internal sealed class GooglePayUIState {

    /**
     * Represents the idle state where no operation is currently in progress.
     */
    data object Idle : GooglePayUIState()

    /**
     * Represents a loading state where a Google Pay operation is in progress.
     */
    data object Loading : GooglePayUIState()

    /**
     * Represents a state where the Google Pay UI needs to be launched.
     *
     * @property paymentDataTask The task that will provide payment data.
     */
    data class LaunchGooglePayTask(val paymentDataTask: Task<PaymentData>) : GooglePayUIState()

    /**
     * Represents an error state where a Google Pay operation failed.
     *
     * @property exception The exception describing the error.
     */
    data class Error(val exception: SdkException) : GooglePayUIState()

    /**
     * Represents a successful state where a Google Pay operation completed successfully.
     *
     * @property chargeData The charge data returned from the successful operation.
     */
    data class Success(val chargeData: ChargeResponse) : GooglePayUIState()
}

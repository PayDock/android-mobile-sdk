package com.paydock.feature.colespay.presentation.state

import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.feature.wallet.domain.model.ui.WalletCallback

/**
 * Represents the various states of the Coles Pay UI.
 *
 * This sealed class models the possible states during the Coles Pay workflow, enabling
 * the ViewModel to manage UI rendering and user interactions effectively. Each state
 * corresponds to a specific phase or outcome of the Coles Pay operation.
 */
internal sealed class ColesPayUIState {

    /**
     * Represents the idle state of the Coles Pay UI.
     *
     * This is the default state when no operation is in progress or after a reset.
     * The UI remains inactive until a user action triggers a state change.
     */
    data object Idle : ColesPayUIState()

    /**
     * Represents the loading state of the Coles Pay UI.
     *
     * This state indicates that a background operation, such as an API call or
     * data processing, is in progress. The UI may display a loading indicator
     * to inform the user.
     */
    data object Loading : ColesPayUIState()

    /**
     * Represents the state where the Coles Pay UI launches an intent.
     *
     * This state provides the required callback data (`WalletCallback`) to
     * facilitate the transition to an external intent, such as navigating to
     * a web page or initiating a Coles Pay-specific flow.
     *
     * @property callbackData The data required to launch the Coles Pay intent.
     */
    data class LaunchIntent(val callbackData: WalletCallback) : ColesPayUIState()

    /**
     * Represents the success state of the Coles Pay UI.
     *
     * This state indicates that the Coles Pay operation was completed successfully.
     * It provides the `orderId` associated with the successful transaction.
     *
     * @property orderId The identifier for the successfully processed order.
     */
    data class Success(val orderId: String) : ColesPayUIState()

    /**
     * Represents the error state of the Coles Pay UI.
     *
     * This state captures and provides details about an error encountered
     * during the Coles Pay workflow. It enables the UI to display error messages
     * or take corrective actions.
     *
     * @property exception The exception describing the error that occurred.
     */
    data class Error(val exception: SdkException) : ColesPayUIState()
}
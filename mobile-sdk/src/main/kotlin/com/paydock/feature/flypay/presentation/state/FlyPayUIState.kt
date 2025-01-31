package com.paydock.feature.flypay.presentation.state

import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.feature.wallet.domain.model.ui.WalletCallback

/**
 * Represents the various states of the FlyPay UI.
 *
 * This sealed class models the possible states during the FlyPay workflow, enabling
 * the ViewModel to manage UI rendering and user interactions effectively. Each state
 * corresponds to a specific phase or outcome of the FlyPay operation.
 */
internal sealed class FlyPayUIState {

    /**
     * Represents the idle state of the FlyPay UI.
     *
     * This is the default state when no operation is in progress or after a reset.
     * The UI remains inactive until a user action triggers a state change.
     */
    data object Idle : FlyPayUIState()

    /**
     * Represents the loading state of the FlyPay UI.
     *
     * This state indicates that a background operation, such as an API call or
     * data processing, is in progress. The UI may display a loading indicator
     * to inform the user.
     */
    data object Loading : FlyPayUIState()

    /**
     * Represents the state where the FlyPay UI launches an intent.
     *
     * This state provides the required callback data (`WalletCallback`) to
     * facilitate the transition to an external intent, such as navigating to
     * a web page or initiating a FlyPay-specific flow.
     *
     * @property callbackData The data required to launch the FlyPay intent.
     */
    data class LaunchIntent(val callbackData: WalletCallback) : FlyPayUIState()

    /**
     * Represents the success state of the FlyPay UI.
     *
     * This state indicates that the FlyPay operation was completed successfully.
     * It provides the `orderId` associated with the successful transaction.
     *
     * @property orderId The identifier for the successfully processed order.
     */
    data class Success(val orderId: String) : FlyPayUIState()

    /**
     * Represents the error state of the FlyPay UI.
     *
     * This state captures and provides details about an error encountered
     * during the FlyPay workflow. It enables the UI to display error messages
     * or take corrective actions.
     *
     * @property exception The exception describing the error that occurred.
     */
    data class Error(val exception: SdkException) : FlyPayUIState()
}
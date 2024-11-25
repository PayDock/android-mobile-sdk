package com.paydock.feature.card.presentation.state

import com.paydock.core.domain.error.exceptions.SdkException

/**
 * Represents the various UI states for displaying and processing card details.
 *
 * This sealed class provides a structured way to handle different states in a card details flow,
 * ensuring type safety and clear state management. Each state corresponds to a specific stage in the card processing lifecycle.
 */
sealed class CardDetailsUIState {

    /**
     * Represents the idle state where no operation is in progress.
     *
     * This state is typically the initial state or a reset state when the card details flow is not active.
     */
    data object Idle : CardDetailsUIState()

    /**
     * Represents the loading state where an operation (e.g., fetching or processing card details) is in progress.
     *
     * Use this state to indicate to the user that they should wait until the operation completes.
     */
    data object Loading : CardDetailsUIState()

    /**
     * Represents a successful state where the card processing operation has completed successfully.
     *
     * @param token The unique token generated after successfully processing the card details.
     *              This token can be used for further transactions or validations.
     */
    data class Success(val token: String) : CardDetailsUIState()

    /**
     * Represents an error state where an operation has failed.
     *
     * @param exception The exception that encapsulates the error details.
     *                  This can provide a user-friendly message or technical information about the failure.
     */
    data class Error(val exception: SdkException) : CardDetailsUIState()
}
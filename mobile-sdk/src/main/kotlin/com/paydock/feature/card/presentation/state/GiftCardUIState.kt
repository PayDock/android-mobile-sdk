package com.paydock.feature.card.presentation.state

import com.paydock.core.domain.error.exceptions.SdkException

/**
 * Represents the UI state of the gift card flow.
 *
 * This sealed class is used to model the various states that the UI can be in, allowing
 * for clear and explicit state management in the gift card feature.
 */
internal sealed class GiftCardUIState {

    /**
     * Represents the idle state, indicating no ongoing actions or events.
     */
    data object Idle : GiftCardUIState()

    /**
     * Represents the loading state, typically shown when an operation is in progress.
     */
    data object Loading : GiftCardUIState()

    /**
     * Represents the success state, containing a valid gift card token.
     *
     * @property token The token associated with the successfully processed gift card.
     */
    data class Success(val token: String) : GiftCardUIState()

    /**
     * Represents the error state, containing the exception that caused the failure.
     *
     * @property exception The [SdkException] describing the error encountered.
     */
    data class Error(val exception: SdkException) : GiftCardUIState()
}
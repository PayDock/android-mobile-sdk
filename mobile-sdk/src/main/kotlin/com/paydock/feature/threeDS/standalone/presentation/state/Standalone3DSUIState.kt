package com.paydock.feature.threeDS.standalone.presentation.state

import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSResult

/**
 * Sealed class representing the UI state for a Standalone 3D Secure (3DS) authentication flow.
 *
 * This state model is used to track different phases of the 3DS authentication process,
 * ensuring that the UI responds appropriately to changes in authentication status.
 */
internal sealed class Standalone3DSUIState {

    /**
     * Represents the idle state where no authentication is in progress.
     */
    data object Idle : Standalone3DSUIState()

    /**
     * Represents the loading state while the authentication process is in progress.
     */
    data object Loading : Standalone3DSUIState()

    /**
     * Represents a successful 3DS authentication result.
     *
     * @param result The result of the authentication process.
     */
    data class Success(val result: Standalone3DSResult) : Standalone3DSUIState()

    /**
     * Represents an error state when authentication fails.
     *
     * @param exception The exception containing details about the failure.
     */
    data class Error(val exception: SdkException) : Standalone3DSUIState()
}

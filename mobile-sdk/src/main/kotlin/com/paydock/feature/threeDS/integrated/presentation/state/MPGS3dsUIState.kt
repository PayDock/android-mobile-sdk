package com.paydock.feature.threeDS.integrated.presentation.state

import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.feature.threeDS.integrated.domain.model.integration.MPGS3dsResult

/**
 * Sealed class representing the UI state for an MPGS 3D Secure (3DS) authentication flow.
 *
 * This state model is used to track different phases of the 3DS authentication process,
 * ensuring that the UI responds appropriately to changes in authentication status.
 */
internal sealed class MPGS3dsUIState {

    /**
     * Represents the idle state where no authentication is in progress.
     */
    data object Idle : MPGS3dsUIState()

    /**
     * Represents the loading state while the authentication process is in progress.
     */
    data object Loading : MPGS3dsUIState()

    /**
     * Represents a successful 3DS authentication result.
     *
     * @param result The result of the authentication process.
     */
    data class Success(val result: MPGS3dsResult) : MPGS3dsUIState()

    /**
     * Represents an error state when authentication fails.
     *
     * @param exception The exception containing details about the failure.
     */
    data class Error(val exception: SdkException) : MPGS3dsUIState()
}

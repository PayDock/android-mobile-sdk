package com.paydock.feature.threeDS.standalone.presentation.state

import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSProgress
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
     * Requests the widget's loading indicator (or loading delegate) to start or finish.
     *
     * @param isLoading `true` to show the loading indicator, `false` to hide it.
     */
    data class Loading(val isLoading: Boolean) : Standalone3DSUIState()

    /**
     * Represents intermediate progress of the authentication, delivered to `onProgress` only.
     *
     * @param progress The progress event.
     */
    data class Progress(val progress: Standalone3DSProgress) : Standalone3DSUIState()

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

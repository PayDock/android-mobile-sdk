package com.paydock.feature.threeDS.presentation

import com.paydock.core.domain.error.exceptions.ThreeDSException
import com.paydock.feature.threeDS.domain.model.ThreeDSResult

/**
 * Represents the state of the 3D Secure (3DS) charge processing feature.
 *
 * @property isLoading Indicates whether the 3DS feature is currently in a loading state.
 * @property error Represents any error that occurred during the operation related to 3DS.
 * @param result The 3DS result containing the charge ID and event type.
 * @param status The unique identifier for the 3DS charge associated with the event.
 */
internal data class ThreeDSViewState(
    val isLoading: Boolean = false,
    val error: ThreeDSException? = null,
    val result: ThreeDSResult? = null,
    val status: String? = null
)
package com.paydock.feature.threeDS.domain.model.ui

import kotlinx.serialization.Serializable

/**
 * Serializable data class representing an error message related to a charge operation.
 *
 * @param message The error message describing the charge error.
 */
@Serializable
internal data class ChargeError(
    val message: String?
)

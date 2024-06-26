package com.paydock.feature.threeDS.presentation.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Serializable data class representing event data related to a charge error,
 * including the error response and the unique transaction identifier.
 *
 * @param error The error response containing details about the charge error.
 * @param charge3dsId The Universal unique transaction identifier used to identify the transaction, if applicable.
 */
@Serializable
internal data class ChargeErrorEventData(
    val error: ChargeError,
    @SerialName("charge_3ds_id") val charge3dsId: String? = null
)

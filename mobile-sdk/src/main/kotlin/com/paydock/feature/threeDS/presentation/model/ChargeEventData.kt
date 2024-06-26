package com.paydock.feature.threeDS.presentation.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Serializable data class representing event data related to a charge,
 * including information about the 3DS charge event with optional type details.
 *
 * @property charge3dsId The Universal unique transaction identifier used to identify the transaction.
 * @property status The status for the event transaction.
 * @property info Additional information specific to the `chargeAuthInfo` event.
 * @property result Result information specific to the `chargeAuthDecoupled` event.
 */
@Serializable
internal data class ChargeEventData(
    @SerialName("charge_3ds_id") val charge3dsId: String,
    val status: String,
    val info: String? = null,
    val result: ChargeResult? = null
)

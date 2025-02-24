package com.paydock.feature.threeDS.standalone.domain.model.ui

import com.paydock.feature.threeDS.standalone.domain.model.ui.enums.StandaloneStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing the event data for standalone 3DS flows.
 *
 * This class contains event-related details for standalone charge events, including the charge
 * status, optional charge 3DS ID, and the charge result.
 *
 * @property status The status of the charge event.
 * @property charge3dsId Charge 3DS ID for tracking the authentication process.
 * @property result Optional result of the charge.
 */
@Serializable
internal data class StandaloneChargeEventData(
    val status: StandaloneStatus?,
    @SerialName("charge_3ds_id") val charge3dsId: String? = null,
    val result: ChargeResult? = null
)

/**
 * Converts a [StandaloneChargeEventData] instance into a [ChargeIdProvider].
 *
 * This function creates an anonymous implementation of the [ChargeIdProvider] interface
 * that delegates the retrieval of the `charge3dsId` to the corresponding property
 * within the provided [StandaloneChargeEventData] instance. This is useful for situations where
 * a component expects a [ChargeIdProvider] but you only have access to a [StandaloneChargeEventData]
 * object.
 *
 * @return A [ChargeIdProvider] instance that provides access to the `charge3dsId` from the
 *   original [StandaloneChargeEventData].
 */
internal fun StandaloneChargeEventData.asChargeIdProvider(): ChargeIdProvider = object :
    ChargeIdProvider {
    override val charge3dsId: String?
        get() = this@asChargeIdProvider.charge3dsId
}
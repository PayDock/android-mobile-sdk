package com.paydock.feature.threeDS.standalone.domain.model.ui

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

/**
 * Converts a [ChargeErrorEventData] instance to a [ChargeIdProvider].
 *
 * This function creates an anonymous implementation of the [ChargeIdProvider] interface
 * that delegates the `charge3dsId` property to the corresponding property of the
 * [ChargeErrorEventData] instance.  This is useful for providing a consistent
 * interface for accessing a potential 3DS charge ID, regardless of the specific
 * data class that contains it.
 *
 * @return A [ChargeIdProvider] instance whose `charge3dsId` property is backed by the
 *         `charge3dsId` property of the [ChargeErrorEventData].
 */
internal fun ChargeErrorEventData.asChargeIdProvider(): ChargeIdProvider =
    object : ChargeIdProvider {
        override val charge3dsId: String?
            get() = this@asChargeIdProvider.charge3dsId
    }

package com.paydock.feature.wallet.data.dto

import kotlinx.serialization.Serializable

/**
 * Represents information about the associated charge.
 *
 * @property status The status of the charge.
 */
@Serializable
internal data class CallbackChargeData(
    val status: String
)
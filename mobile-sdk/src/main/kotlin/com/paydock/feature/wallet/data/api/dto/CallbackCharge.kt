package com.paydock.feature.wallet.data.api.dto

import kotlinx.serialization.Serializable

/**
 * Represents information about the associated charge.
 *
 * @property status The status of the charge.
 */
@Serializable
data class CallbackCharge(
    val status: String
)
package com.paydock.feature.wallet.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the wallet callback data.
 *
 * @property id The unique identifier for the wallet callback.
 * @property status The status of the wallet callback.
 * @property refToken The wallet reference token ie. Afterpay checkout token.
 * @property callbackMethod The callback method used for the wallet callback.
 * @property callbackRel The callback relation.
 * @property callbackUrl The URL to which the callback is made.
 * @property charge Information about the associated charge.
 */
@Serializable
data class CallbackData(
    val id: String? = null,
    val status: String? = null,
    @SerialName("ref_token") val refToken: String? = null,
    @SerialName("callback_method") val callbackMethod: String? = null,
    @SerialName("callback_rel") val callbackRel: String? = null,
    @SerialName("callback_url") val callbackUrl: String? = null,
    val charge: CallbackCharge
)
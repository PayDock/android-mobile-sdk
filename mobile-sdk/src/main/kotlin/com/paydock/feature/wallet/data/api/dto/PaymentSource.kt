package com.paydock.feature.wallet.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing payment source information.
 *
 * @param externalPayerId The external payer ID for the payment source, if available.
 * @param refToken The reference token associated with the payment source.
 */
@Serializable
data class PaymentSource(
    @SerialName("external_payer_id") val externalPayerId: String? = null,
    @SerialName("ref_token") val refToken: String? = null
)
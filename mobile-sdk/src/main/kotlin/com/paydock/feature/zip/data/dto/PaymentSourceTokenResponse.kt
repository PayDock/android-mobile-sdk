package com.paydock.feature.zip.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from the payment source token creation endpoint.
 *
 * @property status HTTP status code.
 * @property error Optional error message.
 * @property resource The response resource containing the token data.
 */
@Serializable
internal data class PaymentSourceTokenResponse(
    @SerialName("status")
    val status: Int,
    @SerialName("error")
    val error: String? = null,
    @SerialName("resource")
    val resource: PaymentSourceTokenResource
)

/**
 * Resource wrapper for the payment source token.
 *
 * @property type The type of resource.
 * @property data The payment source token string.
 */
@Serializable
internal data class PaymentSourceTokenResource(
    @SerialName("type")
    val type: String,
    @SerialName("data")
    val data: String
)

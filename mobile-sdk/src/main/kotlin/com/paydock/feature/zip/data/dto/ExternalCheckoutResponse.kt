package com.paydock.feature.zip.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from the external checkout initialization endpoint.
 *
 * @property status HTTP status code.
 * @property error Optional error message.
 * @property resource The response resource containing checkout data.
 */
@Serializable
internal data class ExternalCheckoutResponse(
    @SerialName("status")
    val status: Int,
    @SerialName("error")
    val error: String? = null,
    @SerialName("resource")
    val resource: ExternalCheckoutResource
)

/**
 * Resource wrapper for the checkout data.
 *
 * @property type The type of resource.
 * @property data The checkout data containing the link and token.
 */
@Serializable
internal data class ExternalCheckoutResource(
    @SerialName("type")
    val type: String,
    @SerialName("data")
    val data: ExternalCheckoutData
)

/**
 * Checkout data containing the redirect link and checkout token.
 *
 * @property link The URL to redirect the user to for completing Zip checkout.
 * @property token The checkout token for subsequent API calls.
 */
@Serializable
internal data class ExternalCheckoutData(
    @SerialName("link")
    val link: String,
    @SerialName("token")
    val token: String
)

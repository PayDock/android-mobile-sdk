package com.paydock.feature.zip.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for creating a payment source token from a checkout token.
 *
 * @property type The type of token request (always "checkout_token" for Zip).
 * @property gatewayId The gateway ID for processing.
 * @property checkoutToken The checkout token received from the external checkout flow.
 */
@Serializable
internal data class PaymentSourceTokenRequest(
    @SerialName("type")
    val type: String = "checkout_token",
    @SerialName("gateway_id")
    val gatewayId: String,
    @SerialName("checkout_token")
    val checkoutToken: String
)

package com.paydock.feature.paypal.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a request for creating a PayPal vault payment token.
 *
 * This class contains the necessary details, such as the gateway ID and the setup token, to create
 * a PayPal payment token, which will be used in subsequent transactions.
 *
 * @property gatewayId The unique identifier for the payment gateway.
 */
@Serializable
internal data class PayPalVaultTokenRequest(
    @SerialName("gateway_id") val gatewayId: String
)
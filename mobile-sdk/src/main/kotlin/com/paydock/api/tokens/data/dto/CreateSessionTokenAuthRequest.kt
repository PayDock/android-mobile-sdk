package com.paydock.api.tokens.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a request to create a session auth token for authentication.
 *
 * This data class is used for serializing and deserializing the JSON body required
 * when creating a session auth token. It includes the gateway ID, which identifies
 * the payment gateway to be used in the session.
 *
 * @property gatewayId The identifier for the payment gateway.
 */
@Serializable
internal data class CreateSessionTokenAuthRequest(
    @SerialName("gateway_id") val gatewayId: String
)

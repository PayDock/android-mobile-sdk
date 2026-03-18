package com.paydock.feature.googlepay.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the response for a Google Pay OTT token request.
 * This matches the web client's CreateOTTResponse structure.
 *
 * @property status The HTTP status code of the response.
 * @property error Optional error information.
 * @property resource The resource containing the token data.
 */
@Serializable
internal data class GooglePayTokenResponse(
    val status: Int,
    val error: String? = null,
    val resource: TokenResource
) {
    /**
     * Represents the token resource in the response.
     *
     * @property type The type of the resource (e.g., "token").
     * @property data The token data containing temp_token and token_type.
     */
    @Serializable
    data class TokenResource(
        val type: String,
        val data: TokenData
    )

    /**
     * Represents the token data in the response.
     *
     * @property tempToken The temporary token (temp_token in API).
     * @property tokenType The type of the token (token_type in API).
     */
    @Serializable
    data class TokenData(
        @SerialName("temp_token") val tempToken: String,
        @SerialName("token_type") val tokenType: String
    )
}

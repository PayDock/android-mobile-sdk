package com.paydock.api.tokens.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing the authentication token data for a PayPal session.
 *
 * @property accessToken The access token used for authenticating API requests.
 * @property idToken The ID token, typically used for user authentication.
 */
@Serializable
internal data class AuthTokenData(
    @SerialName("access_token") val accessToken: String,
    @SerialName("id_token") val idToken: String
)

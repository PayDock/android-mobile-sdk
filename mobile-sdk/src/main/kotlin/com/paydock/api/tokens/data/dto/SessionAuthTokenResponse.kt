package com.paydock.api.tokens.data.dto

import com.paydock.core.network.dto.Resource
import kotlinx.serialization.Serializable

/**
 * Represents the response from creating a session auth token for authentication.
 *
 * This data class is used for serializing and deserializing the JSON response
 * returned from a session auth token creation request. It contains the authentication
 * token data and the status code of the response.
 *
 * @property resource The authentication token data wrapped in a `Resource` object,
 *                    which includes the relevant session information.
 * @property status The HTTP status code returned from the session auth token creation request.
 */
@Serializable
internal data class SessionAuthTokenResponse(
    val resource: Resource<AuthTokenData>,
    val status: Int
)

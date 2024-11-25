package com.paydock.api.tokens.domain.model

/**
 * Data class representing the session authentication details.
 *
 * @property accessToken The access token used for authenticating API requests (optional).
 * @property idToken The ID token, typically used for user authentication (optional).
 */
internal data class SessionAuthToken(
    val accessToken: String,
    val idToken: String
)

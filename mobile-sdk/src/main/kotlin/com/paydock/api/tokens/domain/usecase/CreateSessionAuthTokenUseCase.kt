package com.paydock.api.tokens.domain.usecase

import com.paydock.api.tokens.data.dto.CreateSessionTokenAuthRequest
import com.paydock.api.tokens.domain.model.SessionAuthToken
import com.paydock.api.tokens.domain.repository.TokenRepository
import com.paydock.core.domain.error.exceptions.PayPalVaultException
import com.paydock.core.extensions.suspendRunCatchingMapper

/**
 * A use case class for creating a session authentication token with PayPal.
 *
 * This use case leverages the [TokenRepository] to create an OAuth token for payment sources.
 *
 * @property repository The [TokenRepository] that handles the communication with the PayPal API.
 */
internal class CreateSessionAuthTokenUseCase(private val repository: TokenRepository) {

    /**
     * Executes the use case to create a session authentication token.
     *
     * This function invokes the [repository.createSessionAuthToken] method to request an OAuth token
     * for the session. The function is suspended and returns a [Result] object that either contains
     * the [SessionAuthToken] or an error.
     *
     * @param accessToken The access token required for authorization.
     * @param request The [CreateSessionTokenAuthRequest] containing the gateway ID.
     * @return A [Result] containing either the [SessionAuthToken] or an error if the operation fails.
     */
    suspend operator fun invoke(
        accessToken: String,
        request: CreateSessionTokenAuthRequest
    ): Result<SessionAuthToken> =
        suspendRunCatchingMapper<SessionAuthToken, PayPalVaultException.CreateSessionAuthTokenException> {
            repository.createSessionAuthToken(accessToken, request)
        }
}

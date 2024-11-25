package com.paydock.api.tokens.domain.repository

import com.paydock.api.tokens.data.dto.CreatePaymentTokenRequest
import com.paydock.api.tokens.data.dto.CreateSessionTokenAuthRequest
import com.paydock.api.tokens.data.dto.CreateSetupTokenRequest
import com.paydock.api.tokens.domain.model.PayPalPaymentTokenDetails
import com.paydock.api.tokens.domain.model.SessionAuthToken
import com.paydock.api.tokens.domain.model.TokenDetails
import kotlinx.coroutines.flow.Flow

/**
 * A repository interface for handling payment token operations related to payment gateways.
 *
 * This interface defines the contract for creating payment tokens, session authentication tokens,
 * and setup tokens. Implementations of this interface will handle the network requests to the
 * PayPal API to manage payment-related tokens.
 */
internal interface TokenRepository {

    /**
     * Creates a payment token using the provided access token and request data.
     *
     * This method should be implemented to send a request to the Paydock API to generate
     * a payment token based on the provided request information.
     *
     * @param accessToken The access token required for authorization.
     * @param request The [CreatePaymentTokenRequest] containing the necessary parameters for token creation.
     * @return A [TokenDetails] object containing the generated payment token and its associated type.
     */
    suspend fun createPaymentToken(
        accessToken: String,
        request: CreatePaymentTokenRequest
    ): TokenDetails

    /**
     * Creates a payment token using the provided access token and setup token along with the request data.
     *
     * This method should be implemented to send a request to the Paydock API to generate
     * a payment token based on the provided setup token and request information.
     *
     * @param accessToken The access token required for authorization.
     * @param setupToken The setup token that was previously generated to initialize the PayPal payment process.
     * @param request The [CreatePaymentTokenRequest] containing the necessary parameters for token creation.
     * @return A [PayPalPaymentTokenDetails] object containing the generated payment token and its associated type.
     */
    suspend fun createPaymentToken(
        accessToken: String,
        setupToken: String,
        request: CreatePaymentTokenRequest.PayPalVaultRequest
    ): PayPalPaymentTokenDetails

    /**
     * Creates a flow for generating a payment token asynchronously.
     *
     * This function should be implemented to provide a [Flow] of [TokenDetails] as a response
     * to token creation requests. This can be useful for observing changes or handling
     * updates in the token creation process.
     *
     * @param accessToken The access token required for authorization.
     * @param request The [CreatePaymentTokenRequest] containing the necessary parameters for token creation.
     * @return A [Flow] emitting [TokenDetails] as the payment token is created.
     */
    fun createPaymentTokenFlow(
        accessToken: String,
        request: CreatePaymentTokenRequest
    ): Flow<TokenDetails>

    /**
     * Creates a session authentication token using the provided access token and request data.
     *
     * This method should be implemented to send a request to the PayPal API to generate
     * an OAuth token for payment sources.
     *
     * @param accessToken The access token required for authorization.
     * @param request The [CreateSessionTokenAuthRequest] containing the gateway ID.
     * @return A [SessionAuthToken] object containing the access token and ID token for the session.
     */
    suspend fun createSessionAuthToken(
        accessToken: String,
        request: CreateSessionTokenAuthRequest
    ): SessionAuthToken

    /**
     * Sends a request to the PayPal API to create a setup token for payment sources.
     *
     * This function performs a network call to create a setup token using the provided OAuth token
     * and gateway ID. The setup token is required for creating or managing PayPal payment sources.
     *
     * @param accessToken The OAuth token used for authenticating the request.
     * @param request The request body containing the necessary parameters, such as the gateway ID.
     * @return The setup token if the request is successful, or throw an exception if no setup token is available.
     */
    suspend fun createSetupToken(
        accessToken: String,
        request: CreateSetupTokenRequest
    ): String
}
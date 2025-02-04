package com.paydock.feature.card.domain.repository

import com.paydock.feature.card.data.dto.CreateCardPaymentTokenRequest
import com.paydock.feature.card.domain.model.ui.CardSchema
import com.paydock.feature.card.domain.model.ui.TokenDetails
import kotlinx.coroutines.flow.Flow
import java.util.TreeMap

/**
 * A repository interface for handling payment token operations related to payment gateways.
 *
 * This interface defines the contract for creating payment tokens, session authentication tokens,
 * and setup tokens. Implementations of this interface will handle the network requests to the
 * PayPal API to manage payment-related tokens.
 */
internal interface CardRepository {

    /**
     * Creates a payment token using the provided access token and request data.
     *
     * This method should be implemented to send a request to the Paydock API to generate
     * a payment token based on the provided request information.
     *
     * @param accessToken The access token required for authorization.
     * @param request The [CreateCardPaymentTokenRequest] containing the necessary parameters for token creation.
     * @return A [TokenDetails] object containing the generated payment token and its associated type.
     */
    suspend fun createPaymentToken(
        accessToken: String,
        request: CreateCardPaymentTokenRequest
    ): TokenDetails

    /**
     * Creates a flow for generating a payment token asynchronously.
     *
     * This function should be implemented to provide a [Flow] of [TokenDetails] as a response
     * to token creation requests. This can be useful for observing changes or handling
     * updates in the token creation process.
     *
     * @param accessToken The access token required for authorization.
     * @param request The [CreateCardPaymentTokenRequest] containing the necessary parameters for token creation.
     * @return A [Flow] emitting [TokenDetails] as the payment token is created.
     */
    fun createPaymentTokenFlow(
        accessToken: String,
        request: CreateCardPaymentTokenRequest
    ): Flow<TokenDetails>

    /**
     * Fetches the supported card schemas from a local file.
     *
     * @return A [TreeMap] containing card schema information.
     */
    suspend fun getCardSchemas(): TreeMap<Int, CardSchema>
}
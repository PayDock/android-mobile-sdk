package com.paydock.feature.card.domain.repository

import com.paydock.feature.card.data.api.dto.TokeniseCardRequest
import com.paydock.feature.card.domain.model.TokenisedCardDetails
import kotlinx.coroutines.flow.Flow

/**
 * Represents a repository responsible for handling tokenization of card details.
 */
internal interface CardDetailsRepository {
    /**
     * Tokenizes the provided card details using the given [request].
     *
     * @param accessToken The access token used for authentication with the backend services.
     * @param request The [TokeniseCardRequest] representing the card details to be tokenized.
     * @return A [TokenisedCardDetails] object containing the token and type of the tokenized card.
     */
    suspend fun tokeniseCardDetails(accessToken: String, request: TokeniseCardRequest): TokenisedCardDetails

    /**
     * Tokenizes card details using a flow-based approach.
     *
     * This function takes a [request] containing card details and returns a [Flow] of [TokenisedCardDetails].
     * The function uses Kotlin coroutines and Flow to perform the tokenization asynchronously.
     *
     * @param accessToken The access token used for authentication with the backend services.
     * @param request The [TokeniseCardRequest] containing card details to be tokenized.
     * @return A [Flow] of [TokenisedCardDetails] representing the tokenized card details.
     */
    fun tokeniseCardDetailsFlow(accessToken: String, request: TokeniseCardRequest): Flow<TokenisedCardDetails>
}
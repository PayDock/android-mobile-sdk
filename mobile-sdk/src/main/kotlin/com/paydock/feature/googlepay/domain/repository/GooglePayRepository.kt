package com.paydock.feature.googlepay.domain.repository

import com.paydock.feature.card.domain.model.ui.TokenDetails
import com.paydock.feature.googlepay.data.dto.CreateGooglePayTokenRequest

/**
 * Repository interface for handling Google Pay token operations.
 *
 * This interface defines the contract for creating payment tokens from Google Pay payment data.
 * Implementations of this interface will handle the network requests to the Paydock API
 * to create one-time transaction (OTT) tokens.
 */
internal interface GooglePayRepository {

    /**
     * Creates a payment token using the provided access token and Google Pay token.
     *
     * This method should be implemented to send a request to the Paydock API to generate
     * a payment token based on the provided Google Pay token.
     *
     * @param accessToken The access token required for authorization.
     * @param request The [CreateGooglePayTokenRequest] containing the Google Pay token.
     * @return A [TokenDetails] object containing the generated payment token and its associated type.
     */
    suspend fun createPaymentToken(
        accessToken: String,
        request: CreateGooglePayTokenRequest
    ): TokenDetails
}

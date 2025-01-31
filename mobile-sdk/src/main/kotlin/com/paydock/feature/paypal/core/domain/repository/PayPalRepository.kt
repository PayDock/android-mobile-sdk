package com.paydock.feature.paypal.core.domain.repository

import com.paydock.feature.card.data.dto.CreateCardPaymentTokenRequest
import com.paydock.feature.paypal.core.data.dto.CreateSetupTokenRequest
import com.paydock.feature.paypal.core.data.dto.PayPalVaultTokenRequest
import com.paydock.feature.paypal.core.domain.model.ui.PayPalPaymentTokenDetails

/**
 * A repository interface for handling PayPal operations related to payment gateways.
 *
 * This interface defines the contract for creating payment tokens, session authentication tokens,
 * and setup tokens. Implementations of this interface will handle the network requests to the
 * PayPal API to manage payment-related tokens.
 */
internal interface PayPalRepository {

    /**
     * Creates a payment token using the provided access token and setup token along with the request data.
     *
     * This method should be implemented to send a request to the Paydock API to generate
     * a payment token based on the provided setup token and request information.
     *
     * @param accessToken The access token required for authorization.
     * @param setupToken The setup token that was previously generated to initialize the PayPal payment process.
     * @param request The [CreateCardPaymentTokenRequest] containing the necessary parameters for token creation.
     * @return A [PayPalPaymentTokenDetails] object containing the generated payment token and its associated type.
     */
    suspend fun createPaymentToken(
        accessToken: String,
        setupToken: String,
        request: PayPalVaultTokenRequest
    ): PayPalPaymentTokenDetails

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
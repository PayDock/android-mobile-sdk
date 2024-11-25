package com.paydock.api.tokens.data.dto

import com.paydock.core.network.dto.Resource
import kotlinx.serialization.Serializable

/**
 * Represents a sealed class for handling different types of payment token responses.
 *
 * This class encapsulates responses related to payment tokens, providing specific subclasses for
 * different token response types, such as Card Token and PayPal Vault Token responses. Each subclass
 * contains the response resource and status.
 */
internal sealed class PaymentTokenResponse {

    /**
     * Represents a response for a card token request.
     *
     * @property resource The resource containing the card token as a string.
     * @property status The HTTP status code of the response.
     */
    @Serializable
    internal class CardTokenResponse(
        val resource: Resource<String>,
        val status: Int
    ) : PaymentTokenResponse()

    /**
     * Represents a response for a PayPal vault token request.
     *
     * @property resource The resource containing the PayPal vault token data.
     * @property status The HTTP status code of the response.
     */
    @Serializable
    internal class PayPalVaultTokenResponse(
        val resource: Resource<PayPalPaymentTokenData>,
        val status: Int
    ) : PaymentTokenResponse()
}
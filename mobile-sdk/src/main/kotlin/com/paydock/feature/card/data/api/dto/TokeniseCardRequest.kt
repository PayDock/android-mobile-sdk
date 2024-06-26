package com.paydock.feature.card.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the request for tokenizing card details. It is a sealed class, and each subclass represents a specific type of card request.
 */
@Serializable
internal sealed class TokeniseCardRequest {

    /**
     * Represents a tokenization request for a credit card.
     * @property cvv The CVV of the credit card.
     * @property cardholderName The cardholder's name.
     * @property cardNumber The credit card number.
     * @property expiryMonth The expiry month of the credit card.
     * @property expiryYear The expiry year of the credit card.
     * @property gatewayId The gateway ID for processing the request.
     */
    @Serializable
    data class CreditCard(
        @SerialName("card_ccv") val cvv: String,
        @SerialName("card_name") val cardholderName: String,
        @SerialName("card_number") val cardNumber: String,
        @SerialName("expire_month") val expiryMonth: String,
        @SerialName("expire_year") val expiryYear: String,
        @SerialName("gateway_id") val gatewayId: String
    ) : TokeniseCardRequest()

    // ... (other subclasses for different types of card requests, such as GiftCard and BankCard)

    @Serializable
    data class GiftCard(
        @SerialName("card_type") val type: String = "gift_card",
        @SerialName("first_name") val firstName: String? = null,
        @SerialName("last_name") val lastName: String? = null,
        val email: String? = null,
        val phone: String? = null,
        @SerialName("card_name") val cardName: String? = null,
        @SerialName("card_number") val cardNumber: String,
        @SerialName("card_pin") val cardPin: String,
        @SerialName("store_pin") val storePin: Boolean,
        @SerialName("card_scheme") val cardScheme: String = "vii_giftcard",
        @SerialName("card_processing_network") val cardProcessingNetwork: String = "vii_giftcard",
    ) : TokeniseCardRequest()

    @Serializable
    data class BankCard(
        val type: String,
        @SerialName("account_name") val accountName: String,
        @SerialName("account_number") val accountNumber: String,
        @SerialName("account_routing") val accountRouting: String,
        @SerialName("gateway_id") val gatewayId: String
    ) : TokeniseCardRequest()

}
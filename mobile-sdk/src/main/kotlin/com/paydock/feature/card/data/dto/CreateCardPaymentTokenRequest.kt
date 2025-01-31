package com.paydock.feature.card.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a sealed class for creating payment token requests.
 *
 * This class serves as the base for various types of payment token requests such as tokenizing cards (credit, gift, bank)
 * and PayPal vault tokenization. Each subclass provides the specific data necessary to create a payment token.
 */
@Serializable
internal sealed class CreateCardPaymentTokenRequest {

    /**
     * Represents a request for tokenizing card details. This is a sealed class with multiple subclasses
     * for different types of card tokenization requests, including credit cards, gift cards, and bank cards.
     */
    @Serializable
    internal sealed class TokeniseCardRequest : CreateCardPaymentTokenRequest() {

        /**
         * Represents a tokenization request for a credit card.
         *
         * @property cvv The CVV (Card Verification Value) of the credit card.
         * @property cardholderName The name of the credit cardholder (optional).
         * @property cardNumber The number of the credit card.
         * @property expiryMonth The month the credit card expires.
         * @property expiryYear The year the credit card expires.
         * @property storeCVV A flag to be able to use a CCV value for the initial transaction.
         * @property gatewayId The unique identifier for the payment gateway handling the request.
         */
        @Serializable
        data class CreditCard(
            @SerialName("card_ccv") val cvv: String,
            @SerialName("card_name") val cardholderName: String?,
            @SerialName("card_number") val cardNumber: String,
            @SerialName("expire_month") val expiryMonth: String,
            @SerialName("expire_year") val expiryYear: String,
            @SerialName("store_ccv") val storeCVV: Boolean = true,
            @SerialName("gateway_id") val gatewayId: String?
        ) : TokeniseCardRequest()

        /**
         * Represents a tokenization request for a gift card.
         *
         * @property type The type of card, defaulting to "gift_card".
         * @property firstName The first name of the cardholder (optional).
         * @property lastName The last name of the cardholder (optional).
         * @property email The email address associated with the gift card (optional).
         * @property phone The phone number associated with the gift card (optional).
         * @property cardName The name on the gift card (optional).
         * @property cardNumber The number of the gift card.
         * @property cardPin The PIN of the gift card.
         * @property storePin Whether the PIN should be stored for future use.
         * @property cardScheme The scheme of the gift card, defaulting to "vii_giftcard".
         * @property cardProcessingNetwork The processing network for the card, defaulting to "vii_giftcard".
         */
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
            @SerialName("card_processing_network") val cardProcessingNetwork: String = "vii_giftcard"
        ) : TokeniseCardRequest()

        /**
         * Represents a tokenization request for a bank card.
         *
         * @property type The type of bank card.
         * @property accountName The name of the bank account holder.
         * @property accountNumber The number of the bank account.
         * @property accountRouting The routing number for the bank account.
         * @property gatewayId The unique identifier for the payment gateway handling the request.
         */
        @Serializable
        data class BankCard(
            val type: String,
            @SerialName("account_name") val accountName: String,
            @SerialName("account_number") val accountNumber: String,
            @SerialName("account_routing") val accountRouting: String,
            @SerialName("gateway_id") val gatewayId: String
        ) : TokeniseCardRequest()

    }
}
package com.paydock.feature.card.presentation.model

/**
 * A data class representing the result of a card processing operation.
 *
 * @property token The token associated with the card. This token is usually returned from a payment gateway
 * and is used for identifying the card in future transactions without needing to store the actual card details.
 * @property saveCard A boolean flag indicating whether the card should be saved for future use. Defaults to false.
 */
data class CardResult(
    val token: String,
    val saveCard: Boolean = false
)

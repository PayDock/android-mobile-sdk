package com.paydock.feature.card.domain.model

/**
 * Data class representing tokenized card details.
 *
 * @property token The token representing the tokenized card.
 * @property type The type of the tokenized card.
 */
data class TokenisedCardDetails(
    val token: String?,
    val type: String
)
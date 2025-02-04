package com.paydock.feature.card.domain.model.ui

import com.paydock.core.MobileSDKConstants
import com.paydock.feature.card.domain.model.integration.enums.CardType
import com.paydock.feature.card.domain.model.integration.enums.CardType.Companion.displayLabel

/**
 * Represents a card scheme, including its type, formatting rules, length constraints, and security code details.
 *
 * @property type The [CardType] associated with this scheme (e.g., Visa, MasterCard, Amex).
 * @property gaps A list of positions where spaces should be inserted when formatting the card number.
 * @property lengths A list of valid lengths for the card number under this scheme.
 * @property code The [CardCode] defining the security code type and length for this scheme.
 * @property label The display label derived from the [CardType].
 */
@Suppress("MagicNumber")
internal data class CardScheme(
    val type: CardType,
    val gaps: List<Int> = listOf(4, 8, 12, 16),
    val lengths: List<Int> = listOf(
        MobileSDKConstants.CardDetailsConfig.MIN_CREDIT_CARD_LENGTH,
        MobileSDKConstants.CardDetailsConfig.MAX_CREDIT_CARD_LENGTH
    ),
    val code: CardCode
) {
    val label: String = type.displayLabel()
}
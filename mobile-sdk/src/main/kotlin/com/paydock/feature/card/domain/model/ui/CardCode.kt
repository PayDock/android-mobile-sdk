package com.paydock.feature.card.domain.model.ui

import com.paydock.feature.card.domain.model.ui.enums.CodeType

/**
 * Represents a security code (CVV, CVC, CID) associated with a card scheme.
 *
 * @property type The [CodeType] specifying the type of security code (e.g., CVV, CVC, CID).
 * @property size The required length of the security code for this card scheme.
 */
internal data class CardCode(
    val type: CodeType,
    val size: Int
)
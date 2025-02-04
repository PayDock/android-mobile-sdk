package com.paydock.feature.card.data.dto

import kotlinx.serialization.Serializable

/**
 * Data class representing a single card schema entry.
 *
 * @property bin The Bank Identification Number (BIN) associated with the card schema.
 * @property schema The card's schema details, or `null` if not available.
 */
@Serializable
internal data class CardSchemaData(
    val bin: String,
    val schema: Schema?
)
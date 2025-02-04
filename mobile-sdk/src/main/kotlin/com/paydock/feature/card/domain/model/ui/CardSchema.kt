package com.paydock.feature.card.domain.model.ui

import com.paydock.feature.card.domain.model.integration.enums.CardType

/**
 * A data class representing a card schema, which contains a `bin` (Bank Identification Number),
 * a `schema` (Card Scheme), and calculates the start and end values of the BIN range.
 *
 * The `bin` is expected to be a string that may represent a range, in the format "start~end".
 * The `start` and `end` values are extracted from the `bin`. If no range is specified, the `start` and `end` will be the same.
 *
 * @param bin The Bank Identification Number (BIN), which may represent a range (e.g., "123456~123459").
 * @param schema The `CardScheme` that corresponds to the card type (e.g., VISA, MASTERCARD).
 * @property start The starting value of the BIN range. If the `bin` represents a single value, `start` equals `end`.
 * @property end The ending value of the BIN range. If no range is specified, `end` equals `start`.
 */
internal data class CardSchema(
    val bin: String,
    val schema: CardType?
) {
    val start: String
    val end: String

    init {
        val binRange = bin.split("~")
        start = binRange[0]
        end = if (binRange.size > 1) binRange[1] else start
    }
}

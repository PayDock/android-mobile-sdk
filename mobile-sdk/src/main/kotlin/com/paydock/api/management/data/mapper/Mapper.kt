package com.paydock.api.management.data.mapper

import com.paydock.api.management.data.dto.CardSchemasResponse
import com.paydock.api.management.data.dto.Schema
import com.paydock.api.management.domain.model.CardSchema
import com.paydock.feature.card.domain.model.integration.enums.CardScheme
import java.util.TreeMap

/**
 * Converts a `CardSchemasResponse` object to a `TreeMap` of `CardSchema` entities.
 * The `CardSchemasResponse` contains a list of card schemas, each of which is mapped to a `CardSchema` entity.
 * The list is sorted by the card bin number, and the resulting list is passed to `preprocessCardSchemas` for further processing.
 *
 * @return A `TreeMap` of `CardSchema` entities where the key is the start of the bin and the value is the `CardSchema` entity.
 */
internal fun CardSchemasResponse.asEntity(): TreeMap<Int, CardSchema> {
    val cardSchemas = this.cardSchemas.map { cardSchema ->
        CardSchema(
            bin = cardSchema.bin,
            schema = mapSchemaStringToEnum(cardSchema.schema)
        )
    }
    return preprocessCardSchemas(cardSchemas)
}

/**
 * Maps a `Schema` enum value to a corresponding `CardScheme` enum value.
 * This function converts a `Schema` string value to a `CardScheme` enum, which represents the card scheme.
 * If the schema is not recognized, the function returns `null`.
 *
 * @param schema The `Schema` enum value to be mapped to a `CardScheme`.
 * @return The corresponding `CardScheme` enum value, or `null` if the schema is not recognized.
 */
private fun mapSchemaStringToEnum(schema: Schema?): CardScheme? {
    return when (schema) {
        Schema.VISA -> CardScheme.VISA
        Schema.MASTERCARD -> CardScheme.MASTERCARD
        Schema.AMEX -> CardScheme.AMEX
        Schema.DINERS -> CardScheme.DINERS
        Schema.DISCOVER -> CardScheme.DISCOVER
        Schema.SOLO -> CardScheme.SOLO
        Schema.JAPCB -> CardScheme.JAPCB
        Schema.AUSBC -> CardScheme.AUSBC
        else -> null
    }
}

/**
 * Preprocesses a list of `CardSchema` entities and returns a `TreeMap` where the key is the card's start number.
 * The list of `CardSchema` entities is processed into a `TreeMap` sorted by the `start` value (converted to `Int`).
 * This ensures the card schemas are stored in ascending order of their start values.
 *
 * @param cardSchemas The list of `CardSchema` entities to be processed.
 * @return A `TreeMap` where the keys are the start numbers of the `CardSchema` entities,
 * and the values are the `CardSchema` entities themselves.
 */
private fun preprocessCardSchemas(cardSchemas: List<CardSchema>): TreeMap<Int, CardSchema> {
    return cardSchemas.associateByTo(TreeMap()) { cardSchema -> cardSchema.start.toInt() }
}

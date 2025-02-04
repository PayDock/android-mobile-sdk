package com.paydock.feature.card.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing the response for card schemas.
 *
 * @property cardSchemas A list of [CardSchemaData] representing the supported card schemas.
 */
@Serializable
internal data class CardSchemasResponse(
    @SerialName("card_schemas") val cardSchemas: List<CardSchemaData>
)
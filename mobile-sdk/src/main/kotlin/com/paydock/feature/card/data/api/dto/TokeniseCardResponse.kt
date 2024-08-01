package com.paydock.feature.card.data.api.dto

import com.paydock.core.network.dto.Resource
import kotlinx.serialization.Serializable

/**
 * Data class representing the response from the tokenization request.
 * @property resource The [Resource] object representing the tokenized card details.
 * @property status The status code of the response.
 */
@Serializable
internal data class TokeniseCardResponse(
    val resource: Resource<String>,
    val status: Int
)

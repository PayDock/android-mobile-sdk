package com.paydock.feature.card.data.dto

import com.paydock.core.network.dto.Resource
import kotlinx.serialization.Serializable

/**
 * Represents a response for a card token request.
 *
 * @property resource The resource containing the card token as a string.
 * @property status The HTTP status code of the response.
 */
@Serializable
internal class CardTokenResponse(
    val resource: Resource<String>,
    val status: Int
)
package com.paydock.feature.card.data.mapper

import com.paydock.feature.card.data.api.dto.TokeniseCardResponse
import com.paydock.feature.card.domain.model.TokenisedCardDetails

/**
 * Extension function to convert a [TokeniseCardResponse] to a [TokenisedCardDetails].
 * @receiver The [TokeniseCardResponse] to convert.
 * @return A [TokenisedCardDetails] object containing the token and type of the tokenized card.
 */
internal fun TokeniseCardResponse.asEntity() = TokenisedCardDetails(
    token = resource.data,
    type = resource.type
)
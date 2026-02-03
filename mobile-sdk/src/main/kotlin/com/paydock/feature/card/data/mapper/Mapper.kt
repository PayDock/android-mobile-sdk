package com.paydock.feature.card.data.mapper

import com.paydock.feature.card.data.dto.CardTokenResponse
import com.paydock.feature.card.domain.model.ui.TokenDetails

/**
 * Converts a `PaymentTokenResponse.CardTokenResponse` to a `TokenDetails` entity.
 *
 * This function transforms the `PaymentTokenResponse.CardTokenResponse` object, typically received from a network
 * call, into a `TokenDetails` object used within the application domain. It extracts the token and
 * type information from the `resource` property of the response.
 *
 * @return A `TokenDetails` object containing the token string and its associated type.
 */
internal fun CardTokenResponse.asEntity() =
    TokenDetails(
        token = resource.data,
        type = resource.type
    )

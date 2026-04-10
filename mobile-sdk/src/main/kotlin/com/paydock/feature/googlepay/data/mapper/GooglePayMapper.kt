package com.paydock.feature.googlepay.data.mapper

import com.paydock.feature.card.domain.model.ui.TokenDetails
import com.paydock.feature.googlepay.data.dto.GooglePayTokenResponse

/**
 * Converts a [GooglePayTokenResponse] to a [TokenDetails] entity.
 *
 * This function transforms the Google Pay OTT token response into a TokenDetails object
 * used within the application domain. It extracts the temp_token and token_type from
 * the response resource data.
 *
 * @return A [TokenDetails] object containing the token string and its associated type.
 */
internal fun GooglePayTokenResponse.asEntity(): TokenDetails =
    TokenDetails(
        token = resource.data.tempToken,
        type = resource.data.tokenType
    )

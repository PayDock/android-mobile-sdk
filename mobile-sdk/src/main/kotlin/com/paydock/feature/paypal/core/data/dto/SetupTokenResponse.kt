package com.paydock.feature.paypal.core.data.dto

import com.paydock.core.network.dto.Resource
import kotlinx.serialization.Serializable

/**
 * Represents the response for a PayPal setup token request.
 *
 * This data class is used to deserialize the response from the PayPal API when requesting a setup token.
 * It contains the setup token data and the status of the response.
 *
 * @param resource A [Resource] object containing the [SetupTokenData], which holds the setup token information.
 * @param status The HTTP status code of the response.
 */
@Serializable
internal data class SetupTokenResponse(
    val resource: Resource<SetupTokenData>,
    val status: Int
)

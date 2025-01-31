package com.paydock.feature.paypal.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the setup token data returned from a PayPal API request.
 *
 * This data class is used to deserialize the setup token information from the PayPal API response.
 *
 * @param setupToken The setup token generated for PayPal transactions.
 */
@Serializable
internal data class SetupTokenData(
    @SerialName("setup_token") val setupToken: String
)

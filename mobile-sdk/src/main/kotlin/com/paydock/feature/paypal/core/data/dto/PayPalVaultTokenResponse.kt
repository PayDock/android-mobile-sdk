package com.paydock.feature.paypal.core.data.dto

import com.paydock.core.network.dto.Resource
import kotlinx.serialization.Serializable

/**
 * Represents a response for a PayPal vault token request.
 *
 * @property resource The resource containing the PayPal vault token data.
 * @property status The HTTP status code of the response.
 */
@Serializable
internal class PayPalVaultTokenResponse(
    val resource: Resource<PayPalPaymentTokenData>,
    val status: Int
)
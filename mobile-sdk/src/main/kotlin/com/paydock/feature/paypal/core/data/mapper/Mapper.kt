package com.paydock.feature.paypal.core.data.mapper

import com.paydock.feature.paypal.core.data.dto.PayPalVaultTokenResponse
import com.paydock.feature.paypal.core.domain.model.ui.PayPalPaymentTokenDetails

/**
 * Converts a `PaymentTokenResponse.PayPalVaultTokenResponse` to a `PayPalPaymentTokenDetails` entity.
 *
 * This function transforms the `PaymentTokenResponse.PayPalVaultTokenResponse` object, typically received from a network
 * call, into a `PayPalTokenDetails` object used within the application domain. It extracts the token from the data and
 * type information from the `resource` property of the response.
 *
 * @return A `TokenDetails` object containing the token string and its associated type.
 */
internal fun PayPalVaultTokenResponse.asEntity() = PayPalPaymentTokenDetails(
    token = resource.data?.token ?: "",
    email = resource.data?.email ?: ""
)
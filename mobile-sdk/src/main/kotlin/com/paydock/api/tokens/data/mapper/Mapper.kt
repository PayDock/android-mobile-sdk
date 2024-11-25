package com.paydock.api.tokens.data.mapper

import com.paydock.api.tokens.data.dto.PaymentTokenResponse
import com.paydock.api.tokens.data.dto.SessionAuthTokenResponse
import com.paydock.api.tokens.domain.model.PayPalPaymentTokenDetails
import com.paydock.api.tokens.domain.model.SessionAuthToken
import com.paydock.api.tokens.domain.model.TokenDetails

/**
 * Converts a `PaymentTokenResponse.CardTokenResponse` to a `TokenDetails` entity.
 *
 * This function transforms the `PaymentTokenResponse.CardTokenResponse` object, typically received from a network
 * call, into a `TokenDetails` object used within the application domain. It extracts the token and
 * type information from the `resource` property of the response.
 *
 * @return A `TokenDetails` object containing the token string and its associated type.
 */
internal fun PaymentTokenResponse.CardTokenResponse.asEntity() = TokenDetails(
    token = resource.data,
    type = resource.type
)

/**
 * Converts a `PaymentTokenResponse.PayPalVaultTokenResponse` to a `PayPalTokenDetails` entity.
 *
 * This function transforms the `PaymentTokenResponse.PayPalVaultTokenResponse` object, typically received from a network
 * call, into a `PayPalTokenDetails` object used within the application domain. It extracts the token from the data and
 * type information from the `resource` property of the response.
 *
 * @return A `TokenDetails` object containing the token string and its associated type.
 */
internal fun PaymentTokenResponse.PayPalVaultTokenResponse.asEntity() = PayPalPaymentTokenDetails(
    token = resource.data?.token ?: "",
    email = resource.data?.email ?: ""
)

/**
 * Converts a [SessionAuthTokenResponse] into a [SessionAuthToken] entity.
 *
 * This function extracts the `accessToken` and `idToken` from the response's resource data
 * and maps them to the corresponding properties in the [SessionAuthToken] object.
 *
 * @return A [SessionAuthToken] instance with the access token and ID token.
 */
internal fun SessionAuthTokenResponse.asEntity() = SessionAuthToken(
    accessToken = resource.data?.accessToken ?: "",
    idToken = resource.data?.idToken ?: ""
)
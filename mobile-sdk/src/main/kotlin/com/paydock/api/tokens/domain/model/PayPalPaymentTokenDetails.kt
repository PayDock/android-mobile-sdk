package com.paydock.api.tokens.domain.model

import com.paydock.api.tokens.data.dto.PaymentTokenResponse

/**
 * Represents data associated with a PayPal payment token for [PaymentTokenResponse.PayPalVaultTokenResponse].
 *
 * This class contains the token information and associated email, typically used
 * in scenarios involving payment processing and tokenization.
 *
 * @property token The payment token as a string.
 * @property email The email address associated with the token.
 */
internal data class PayPalPaymentTokenDetails(
    val token: String,
    val email: String
)
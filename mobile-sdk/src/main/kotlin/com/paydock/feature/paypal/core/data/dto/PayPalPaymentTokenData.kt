package com.paydock.feature.paypal.core.data.dto

import kotlinx.serialization.Serializable

/**
 * Represents data associated with a PayPal payment token for .
 *
 * This class contains the token information and associated email, typically used
 * in scenarios involving payment processing and tokenization.
 *
 * @property token The payment token as a string.
 * @property email The email address associated with the PayPal account.
 */
@Serializable
internal data class PayPalPaymentTokenData(
    val token: String,
    val email: String
)
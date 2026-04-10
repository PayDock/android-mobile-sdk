package com.paydock.feature.googlepay.domain.model.integration

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val jsonConfig = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

/**
 * Represents the response data returned by the Google Pay API after a user has authorized a payment.
 *
 * This class captures the essential details including the payment token, billing/shipping information,
 * and card metadata required to process the transaction through Paydock.
 *
 * @property email The user's email address, if requested in the payment configuration.
 * @property shippingAddress The shipping address selected by the user, if requested.
 * @property paymentMethodData Contains detailed information about the selected payment method and the payment token.
 */
@Serializable
data class GooglePayPaymentResponse(
    val email: String? = null,
    // Google returns the actual address object here, not the parameters object
    val shippingAddress: GooglePayBillingAddress? = null,
    val paymentMethodData: PaymentMethodData
) {
    @Serializable
    data class PaymentMethodData(
        val info: Info? = null,
        val tokenizationData: TokenizationData
    )

    @Serializable
    data class Info(
        val billingAddress: GooglePayBillingAddress? = null,
        val cardNetwork: String? = null,
        val cardDetails: String? = null
    )

    @Serializable
    data class TokenizationData(
        val token: String
    )

    companion object {
        fun fromJson(jsonString: String): GooglePayPaymentResponse =
            jsonConfig.decodeFromString(jsonString)
    }
}
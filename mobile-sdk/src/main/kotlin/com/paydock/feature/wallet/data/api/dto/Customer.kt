package com.paydock.feature.wallet.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing customer details for a wallet transaction.
 *
 * @param paymentSource The payment source information for the customer.
 */
@Serializable
data class Customer(
    @SerialName("payment_source") val paymentSource: PaymentSource? = null
)
package com.paydock.feature.wallet.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing customer details for a wallet transaction.
 *
 * @param paymentSource The payment source information for the customer.
 */
@Serializable
internal data class CustomerData(
    @SerialName("payment_source") val paymentSource: PaymentSourceData? = null
)
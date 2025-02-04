package com.paydock.feature.wallet.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing a request for capturing a wallet transaction.
 *
 * @param paymentMethodId The ID of the payment method to be used for the transaction.
 * @param customer The customer details associated with the transaction.
 */
@Serializable
internal data class CaptureWalletChargeRequest(
    @SerialName("payment_method_id") val paymentMethodId: String? = null,
    @SerialName("customer") val customer: CustomerData? = null
)

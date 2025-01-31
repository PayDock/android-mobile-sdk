package com.paydock.feature.wallet.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a request object used to initiate a wallet callback.
 *
 * @property type The type of the wallet callback request, default is "CREATE_TRANSACTION".
 * @property shipping An optional flag indicating whether shipping information is included in the request.
 * @property sessionId An optional session identifier.
 * @property walletType An optional wallet type.
 */
@Serializable
internal data class WalletCallbackRequest(
    @SerialName("request_type") val type: String,
    @SerialName("request_shipping") val shipping: Boolean? = null,
    @SerialName("session_id") val sessionId: String? = null,
    @SerialName("wallet_type") val walletType: String? = null
)

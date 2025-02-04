package com.paydock.feature.wallet.data.dto

import com.paydock.core.network.dto.Resource
import kotlinx.serialization.Serializable

/**
 * Represents the response from the wallet configuration API ie. PayPal.
 *
 * This data class is used to deserialize the response from the `/v1/gateways/{gatewayId}/wallet-config`
 * API. It contains the wallet configuration details wrapped inside the `resource` field and the HTTP
 * status code of the response.
 *
 * @property resource The `Resource` object that contains the configuration data for the wallet.
 * @property status The HTTP status code of the response.
 */
@Serializable
internal data class WalletConfigResponse(
    val resource: Resource<ConfigData>,
    val status: Int
)

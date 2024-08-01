package com.paydock.feature.wallet.data.api.dto

import com.paydock.core.network.dto.Resource
import kotlinx.serialization.Serializable

/**
 * Data class representing a response for declining a wallet transaction.
 *
 * @param resource The wallet transaction resource containing transaction data.
 * @param status The HTTP status code of the response.
 */
@Serializable
internal data class WalletDeclineResponse(
    val resource: Resource<WalletDeclineData>,
    val status: Int
)

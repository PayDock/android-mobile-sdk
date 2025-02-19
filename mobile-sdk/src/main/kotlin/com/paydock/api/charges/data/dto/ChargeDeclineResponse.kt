package com.paydock.api.charges.data.dto

import com.paydock.core.network.dto.Resource
import kotlinx.serialization.Serializable

/**
 * Data class representing a response for declining a wallet transaction.
 *
 * @param resource The wallet transaction resource containing transaction data.
 * @param status The HTTP status code of the response.
 */
@Serializable
internal data class ChargeDeclineResponse(
    val resource: Resource<WalletDeclineData>,
    val status: Int
)

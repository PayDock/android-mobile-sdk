package com.paydock.feature.wallet.data.dto

import com.paydock.core.network.dto.Resource
import kotlinx.serialization.Serializable

/**
 * Represents the response object received when retrieving wallet callback information from the server.
 *
 * @property resource The resource containing the wallet callback data.
 * @property status The HTTP status code of the response.
 */
@Serializable
internal data class WalletCallbackResponse(
    val resource: Resource<CallbackData>,
    val status: Int
)

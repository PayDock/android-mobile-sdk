package com.paydock.feature.wallet.data.mapper

import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.wallet.data.api.dto.WalletCallbackResponse
import com.paydock.feature.wallet.data.api.dto.WalletCaptureResponse
import com.paydock.feature.wallet.data.api.dto.WalletDeclineResponse
import com.paydock.feature.wallet.domain.model.WalletCallback

/**
 * Converts a [WalletCaptureResponse] into a [ChargeResponse].
 *
 * @return A [ChargeResponse] entity representing the wallet capture response.
 */
internal fun WalletCaptureResponse.asEntity() = ChargeResponse(
    status = status,
    resource = ChargeResponse.ChargeResource(
        type = resource.type,
        data = resource.data?.let { data ->
            ChargeResponse.ChargeData(
                status = data.status,
                id = data.id,
                amount = data.amount,
                currency = data.currency
            )
        }
    )
)

/**
 * Converts a [WalletCaptureResponse] into a [ChargeResponse].
 *
 * @return A [ChargeResponse] entity representing the wallet capture response.
 */
internal fun WalletDeclineResponse.asEntity() = ChargeResponse(
    status = status,
    resource = ChargeResponse.ChargeResource(
        type = resource.type,
        data = resource.data?.let { data ->
            ChargeResponse.ChargeData(
                status = data.status
            )
        }
    )
)

/**
 * Extracts the callback URL from a [WalletCallbackResponse].
 *
 * @return The callback URL from the wallet callback response.
 */
internal fun WalletCallbackResponse.asEntity() = WalletCallback(
    callbackId = resource.data?.id,
    status = resource.data?.charge?.status,
    callbackUrl = resource.data?.callbackUrl,
    refToken = resource.data?.refToken
)
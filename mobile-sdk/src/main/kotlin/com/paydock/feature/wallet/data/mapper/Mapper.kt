package com.paydock.feature.wallet.data.mapper

import com.paydock.feature.wallet.data.dto.CaptureChargeResponse
import com.paydock.feature.wallet.data.dto.ChargeDeclineResponse
import com.paydock.feature.wallet.data.dto.WalletCallbackResponse
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.model.ui.WalletCallback

/**
 * Maps a [CaptureChargeResponse] to a domain-level [ChargeResponse].
 *
 * This function converts the [CaptureChargeResponse] from the network or data layer
 * into a more domain-friendly [ChargeResponse] object, preserving the essential
 * information for further processing in the business logic.
 *
 * @return The mapped [ChargeResponse] object.
 */
internal fun CaptureChargeResponse.asEntity() = ChargeResponse(
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
 * Maps a [ChargeDeclineResponse] to a domain-level [ChargeResponse].
 *
 * This function converts the [ChargeDeclineResponse] into a [ChargeResponse]
 * for handling cases where a charge was declined. Only the status field of the
 * charge data is preserved.
 *
 * @return The mapped [ChargeResponse] object.
 */
internal fun ChargeDeclineResponse.asEntity() = ChargeResponse(
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
 * Maps a [WalletCallbackResponse] to a domain-level [WalletCallback].
 *
 * This function converts the [WalletCallbackResponse] from the data layer into a
 * [WalletCallback] object that is used in the domain or business layer. This mapping
 * includes essential fields such as the callback ID, status, callback URL, and reference token.
 *
 * @return The mapped [WalletCallback] object.
 */
internal fun WalletCallbackResponse.asEntity() = WalletCallback(
    callbackId = resource.data?.id,
    status = resource.data?.charge?.status,
    callbackUrl = resource.data?.callbackUrl,
    refToken = resource.data?.refToken
)

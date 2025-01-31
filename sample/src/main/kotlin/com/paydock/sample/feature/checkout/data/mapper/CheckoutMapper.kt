package com.paydock.sample.feature.checkout.data.mapper

import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.sample.feature.checkout.data.api.dto.CaptureChargeResponse
import java.net.HttpURLConnection

fun CaptureChargeResponse.toDomain(): ChargeResponse = ChargeResponse(
    status = HttpURLConnection.HTTP_OK,
    resource = ChargeResponse.ChargeResource(
        type = this.resource.type,
        data = ChargeResponse.ChargeData(
            id = this.resource.resourceData.id,
            status = this.resource.resourceData.status
        )
    )
)
package com.paydock.sample.feature.charges.data.mapper

import com.paydock.feature.charge.domain.model.integration.ChargeResponse
import com.paydock.sample.feature.charges.data.api.dto.CaptureChargeResponse
import com.paydock.sample.feature.charges.data.api.dto.ThreeDSTokenResponse
import com.paydock.sample.feature.charges.data.api.dto.WalletCaptureResponse
import com.paydock.sample.feature.charges.data.api.dto.WalletInitiateResponse
import com.paydock.sample.feature.charges.domain.model.ThreeDSToken
import com.paydock.sample.feature.charges.domain.model.WalletCharge
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

fun WalletInitiateResponse.toDomain(): WalletCharge = WalletCharge(
    walletToken = this.resource.resourceData.token,
    chargeId = this.resource.resourceData.charge.id,
    status = this.resource.resourceData.charge.status
)

fun WalletCaptureResponse.toDomain(): WalletCharge = WalletCharge(
    chargeId = this.resource.resourceData.id,
    status = this.resource.resourceData.status
)

fun ThreeDSTokenResponse.mapToDomain(): ThreeDSToken = ThreeDSToken(
    token = this.resource.resourceData.threeDS.token,
    status = ThreeDSToken.ThreeDSStatus.byNameIgnoreCaseOrNull(this.resource.resourceData.status)
)
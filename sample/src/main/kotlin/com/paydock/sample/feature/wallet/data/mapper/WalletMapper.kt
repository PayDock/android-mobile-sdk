package com.paydock.sample.feature.wallet.data.mapper

import com.paydock.sample.feature.wallet.data.api.dto.WalletCaptureResponse
import com.paydock.sample.feature.wallet.data.api.dto.WalletInitiateResponse
import com.paydock.sample.feature.wallet.data.model.WalletCharge

fun WalletInitiateResponse.toDomain(): WalletCharge = WalletCharge(
    walletToken = this.resource.resourceData.token,
    chargeId = this.resource.resourceData.charge.id,
    status = this.resource.resourceData.charge.status
)

fun WalletCaptureResponse.toDomain(): WalletCharge = WalletCharge(
    chargeId = this.resource.resourceData.id,
    status = this.resource.resourceData.status
)
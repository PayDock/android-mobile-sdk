package com.paydock.sample.feature.charges.data.api.dto

import com.paydock.sample.core.data.api.dto.Resource

data class WalletCaptureResponse(
    val error: Any,
    val resource: Resource<WalletChargeDTO>,
    val status: Int,
)
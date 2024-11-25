package com.paydock.sample.feature.wallet.data.api.dto

import com.paydock.sample.core.data.api.dto.Resource

data class WalletInitiateResponse(
    val error: Any,
    val resource: Resource<WalletData>,
    val status: Int,
)
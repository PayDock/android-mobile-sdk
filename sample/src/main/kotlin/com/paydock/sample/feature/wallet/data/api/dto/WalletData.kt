package com.paydock.sample.feature.wallet.data.api.dto

data class WalletData(
    val token: String,
    val charge: WalletChargeData,
)
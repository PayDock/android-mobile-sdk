package com.paydock.sample.feature.charges.domain.model

data class WalletCharge(
    val walletToken: String? = null,
    val chargeId: String,
    val status: String,
)

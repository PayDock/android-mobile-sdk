package com.paydock.sample.feature.wallet.data.api.dto

import com.google.gson.annotations.SerializedName

data class WalletChargeData(
    @SerializedName("_id") val id: String,
    val status: String
    // We do not need any other response properties (ie. charge details)
)
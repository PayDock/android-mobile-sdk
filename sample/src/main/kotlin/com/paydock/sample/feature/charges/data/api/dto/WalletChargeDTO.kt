package com.paydock.sample.feature.charges.data.api.dto

import com.google.gson.annotations.SerializedName

data class WalletChargeDTO(
    @SerializedName("_id") val id: String,
    val status: String,
    // We do not need any other response properties (ie. charge details)
)
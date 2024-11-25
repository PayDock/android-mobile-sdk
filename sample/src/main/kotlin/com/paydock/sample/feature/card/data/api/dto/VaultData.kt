package com.paydock.sample.feature.card.data.api.dto

import com.google.gson.annotations.SerializedName

data class VaultData(
    @SerializedName("vault_token") val token: String,
    // We do not need any other response properties (ie. charge)
)
package com.paydock.sample.feature.wallet.data.api.dto

import com.google.gson.annotations.SerializedName
import com.paydock.sample.core.MERCHANT_NAME

data class Meta(
    @SerializedName("store_id") val storeId: String = "007",
    @SerializedName("store_name") val storeName: String = MERCHANT_NAME,
    @SerializedName("merchant_name") val merchantName: String = MERCHANT_NAME,
    @SerializedName("success_url") val successUrl: String? = null,
    @SerializedName("error_url") val errorUrl: String? = null,

    )
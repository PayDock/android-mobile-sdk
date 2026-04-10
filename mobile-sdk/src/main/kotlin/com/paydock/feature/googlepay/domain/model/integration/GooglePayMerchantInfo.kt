package com.paydock.feature.googlepay.domain.model.integration

import kotlinx.serialization.Serializable
import org.json.JSONObject

@Serializable
data class GooglePayMerchantInfo(
    val merchantName: String,
    val merchantId: String
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("merchantName", merchantName)
        put("merchantId", merchantId)
    }
}
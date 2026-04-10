package com.paydock.feature.googlepay.domain.model.integration

import com.paydock.core.MobileSDKConstants
import kotlinx.serialization.Serializable
import org.json.JSONObject

@Serializable
data class GooglePayTransactionInfo(
    val totalPrice: String,
    val totalPriceLabel: String,
    val countryCode: String,
    val currencyCode: String,
    val totalPriceStatus: String = MobileSDKConstants.GooglePayConfig.TRANSACTION_PRICE_STATUS,
    val checkoutOption: String? = null
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("totalPrice", totalPrice)
        put("totalPriceLabel", totalPriceLabel)
        put("totalPriceStatus", totalPriceStatus)
        put("countryCode", countryCode.uppercase())
        put("currencyCode", currencyCode.uppercase())
        checkoutOption?.let { put("checkoutOption", it) }
    }
}
package com.paydock.feature.googlepay.domain.model.integration

import com.paydock.core.MobileSDKConstants
import kotlinx.serialization.Serializable
import org.json.JSONObject

@Serializable
data class GooglePayPaymentMethodTokenizationSpecification(
    val type: String = MobileSDKConstants.GooglePayConfig.TOKENIZATION_TYPE,
    val parameters: GooglePayPaymentMethodTokenizationParameters
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("type", type)
        put("parameters", parameters.toJsonObject())
    }
}

@Serializable
data class GooglePayPaymentMethodTokenizationParameters(
    val gateway: String = MobileSDKConstants.GooglePayConfig.GATEWAY,
    val gatewayMerchantId: String
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("gateway", gateway)
        put("gatewayMerchantId", gatewayMerchantId)
    }
}

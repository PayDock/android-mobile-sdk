package com.paydock.feature.googlepay.domain.model.integration

import com.paydock.core.MobileSDKConstants
import kotlinx.serialization.Serializable
import org.json.JSONObject

@Serializable
data class GooglePayCardPaymentMethod(
    val parameters: GooglePayCardParameters,
    val tokenizationSpecification: GooglePayPaymentMethodTokenizationSpecification? = null,
    val type: String = MobileSDKConstants.GooglePayConfig.CARD_PAYMENT_TYPE
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("type", type)
        put("parameters", parameters.toJsonObject())
        tokenizationSpecification?.let { put("tokenizationSpecification", it.toJsonObject()) }
    }
}
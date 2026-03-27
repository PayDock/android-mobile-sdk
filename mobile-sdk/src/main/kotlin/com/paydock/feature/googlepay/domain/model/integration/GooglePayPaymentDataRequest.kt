package com.paydock.feature.googlepay.domain.model.integration

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject

private val googlePayJson: Json = Json {
    encodeDefaults = true
    explicitNulls = false
}

@Serializable
data class GooglePayPaymentDataRequest(
    val apiVersion: Int = 2,
    val apiVersionMinor: Int = 0,
    val emailRequired: Boolean = false,
    val shippingAddressRequired: Boolean = false,
    val allowedPaymentMethods: List<GooglePayCardPaymentMethod>,
    val transactionInfo: GooglePayTransactionInfo,
    val merchantInfo: GooglePayMerchantInfo,
    val shippingAddressParameters: GooglePayShippingAddressParameters? = null
) {
    fun toJsonString(): String = googlePayJson.encodeToString(this)

    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("apiVersion", apiVersion)
        put("apiVersionMinor", apiVersionMinor)

        put("emailRequired", emailRequired)
        put("shippingAddressRequired", shippingAddressRequired)
        put(
            "allowedPaymentMethods",
            JSONArray().apply {
                allowedPaymentMethods.forEach { put(it.toJsonObject()) }
            }
        )

        put("transactionInfo", transactionInfo.toJsonObject())
        put("merchantInfo", merchantInfo.toJsonObject())

        if (shippingAddressRequired) {
            val shippingParams = shippingAddressParameters
            if (shippingParams != null && !shippingParams.isEmpty()) {
                put("shippingAddressParameters", shippingParams.toJsonObject())
            }
        }
    }

    fun allowedPaymentMethodsJsonArrayString(): String =
        googlePayJson.encodeToString(allowedPaymentMethods)
}

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
data class GooglePayIsReadyToPayRequest(
    val apiVersion: Int = 2,
    val apiVersionMinor: Int = 0,
    val allowedPaymentMethods: List<GooglePayCardPaymentMethod>
) {
    fun toJsonString(): String = googlePayJson.encodeToString(this)

    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("apiVersion", apiVersion)
        put("apiVersionMinor", apiVersionMinor)
        put(
            "allowedPaymentMethods",
            JSONArray().apply {
                allowedPaymentMethods.forEach { put(it.toJsonObject()) }
            }
        )
    }
}

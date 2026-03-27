package com.paydock.feature.googlepay.domain.model.integration

import kotlinx.serialization.Serializable
import org.json.JSONArray
import org.json.JSONObject

@Serializable
data class GooglePayCardParameters(
    val allowedAuthMethods: List<String>,
    val allowedCardNetworks: List<String>,
    val billingAddressRequired: Boolean,
    val billingAddressParameters: GooglePayBillingAddressParameters? = null
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put(
            "allowedAuthMethods",
            JSONArray().apply {
                allowedAuthMethods.forEach { put(it) }
            }
        )
        put(
            "allowedCardNetworks",
            JSONArray().apply {
                allowedCardNetworks.forEach { put(it) }
            }
        )
        put("billingAddressRequired", billingAddressRequired)
        billingAddressParameters?.let { params ->
            put("billingAddressParameters", params.toJsonObject())
        }
    }
}
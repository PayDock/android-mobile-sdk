package com.paydock.feature.googlepay.domain.model.integration

import kotlinx.serialization.Serializable
import org.json.JSONArray
import org.json.JSONObject

@Serializable
data class GooglePayShippingAddressParameters(
    val allowedCountryCodes: List<String>? = null,
    val phoneNumberRequired: Boolean? = null,
    val format: String? = null
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        allowedCountryCodes?.takeIf { it.isNotEmpty() }?.let { codes ->
            put("allowedCountryCodes", JSONArray().apply { codes.forEach { put(it) } })
        }
        phoneNumberRequired?.let { put("phoneNumberRequired", it) }
        format?.let { put("format", it) }
    }

    fun isEmpty(): Boolean =
        (allowedCountryCodes.isNullOrEmpty() && phoneNumberRequired == null && format == null)
}
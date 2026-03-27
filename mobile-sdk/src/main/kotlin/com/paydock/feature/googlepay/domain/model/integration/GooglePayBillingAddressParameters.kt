package com.paydock.feature.googlepay.domain.model.integration

import com.paydock.core.MobileSDKConstants
import kotlinx.serialization.Serializable
import org.json.JSONObject

@Serializable
data class GooglePayBillingAddressParameters(
    val format: String = MobileSDKConstants.GooglePayConfig.BILLING_ADDRESS_FORMAT,
    val phoneNumberRequired: Boolean
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("format", format)
        put("phoneNumberRequired", phoneNumberRequired)
    }
}
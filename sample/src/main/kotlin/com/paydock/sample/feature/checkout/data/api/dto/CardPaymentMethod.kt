package com.paydock.sample.feature.checkout.data.api.dto

import com.google.gson.annotations.SerializedName
import com.paydock.sample.BuildConfig

data class CardPaymentMethod(
    @SerializedName("use_case") val useCase: String = "guest_direct_capture", // guest_direct_capture or guest_direct_capture_gw_3ds
    @SerializedName("payment_service_id") val paymentServiceId: String = BuildConfig.GATEWAY_ID,
    @SerializedName("_3ds_service_id") val threeDSServiceId: String = BuildConfig.GATEWAY_ID,
    @SerializedName("fraud_protection_service_id") val fraudProtectionServiceId: String? = null
)
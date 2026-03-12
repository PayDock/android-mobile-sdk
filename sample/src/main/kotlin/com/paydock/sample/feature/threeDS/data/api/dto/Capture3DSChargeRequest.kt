package com.paydock.sample.feature.threeDS.data.api.dto

import com.google.gson.annotations.SerializedName
import com.paydock.sample.feature.checkout.data.api.dto.ChargesCustomerDTO
import java.math.BigDecimal

sealed class Capture3DSChargeRequest {

    data class CaptureMPGS3dsChargeRequest(
        val amount: BigDecimal,
        val currency: String,
        val reference: String = "some_reference",
        val description: String = "some_description",
        @SerializedName("_3ds") val threeDSData: ThreeDSChargeData? = null
    ) : Capture3DSChargeRequest() {
        data class ThreeDSChargeData(
            val id: String
        )
    }

    data class CaptureStandalone3DSChargeRequest(
        val amount: BigDecimal,
        val currency: String,
        val reference: String = "some_reference",
        val description: String = "some_description",
        val customer: ChargesCustomerDTO,
        @SerializedName("_3ds_charge_id") val threeDSChargeId: String
    ) : Capture3DSChargeRequest()
}
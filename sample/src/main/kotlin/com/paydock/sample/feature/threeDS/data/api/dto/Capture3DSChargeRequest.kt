package com.paydock.sample.feature.threeDS.data.api.dto

import com.google.gson.annotations.SerializedName
import com.paydock.sample.core.AMOUNT
import com.paydock.sample.core.AU_CURRENCY_CODE
import java.math.BigDecimal

data class Capture3DSChargeRequest(
    val amount: BigDecimal = BigDecimal(AMOUNT),
    val currency: String = AU_CURRENCY_CODE,
    val reference: String = "some_reference",
    val description: String = "some_description",
    @SerializedName("_3ds") val threeDSData: ThreeDSChargeData,
) {
    data class ThreeDSChargeData(
        val id: String
    )
}
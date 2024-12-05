package com.paydock.sample.feature.charges.data.api.dto

import com.google.gson.annotations.SerializedName

data class ThreeDSResourceDTO(
    @SerializedName("_3ds") val threeDS: ThreeDSDTO,
    @SerializedName("status") val status: String,
)
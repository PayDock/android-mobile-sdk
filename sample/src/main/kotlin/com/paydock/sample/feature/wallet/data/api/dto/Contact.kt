package com.paydock.sample.feature.wallet.data.api.dto

import com.google.gson.annotations.SerializedName
import com.paydock.sample.core.FIRST_NAME
import com.paydock.sample.core.LAST_NAME
import com.paydock.sample.core.PHONE_NUMBER

data class Contact(
    @SerializedName("first_name") val firstName: String = FIRST_NAME,
    @SerializedName("last_name") val lastName: String = LAST_NAME,
    val phone: String = PHONE_NUMBER
)
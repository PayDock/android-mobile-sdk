package com.paydock.sample.feature.wallet.data.api.dto

import com.google.gson.annotations.SerializedName
import com.paydock.sample.core.EMAIL
import com.paydock.sample.core.FIRST_NAME
import com.paydock.sample.core.LAST_NAME
import com.paydock.sample.core.PHONE_NUMBER

data class Customer(
    val email: String = EMAIL,
    @SerializedName("first_name") val firstName: String = FIRST_NAME,
    @SerializedName("last_name") val lastName: String = LAST_NAME,
    @SerializedName("credentials_updated_at") val credentialsUpdatedAt: String? = null,
    val suspicious: Boolean? = false,
    @SerializedName("payment_source") val paymentSource: PaymentSource,
    val phone: String = PHONE_NUMBER
)
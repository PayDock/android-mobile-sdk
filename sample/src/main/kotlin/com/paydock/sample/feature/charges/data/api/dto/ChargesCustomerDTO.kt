package com.paydock.sample.feature.charges.data.api.dto

import com.google.gson.annotations.SerializedName
import com.paydock.sample.core.EMAIL
import com.paydock.sample.core.FIRST_NAME
import com.paydock.sample.core.LAST_NAME
import com.paydock.sample.core.PHONE_NUMBER

data class ChargesCustomerDTO(
    val email: String = EMAIL,
    @SerializedName("first_name") val firstName: String = FIRST_NAME,
    @SerializedName("last_name") val lastName: String = LAST_NAME,
    @SerializedName("credentials_updated_at") val credentialsUpdatedAt: String? = null,
    val suspicious: Boolean? = false,
    @SerializedName("payment_source") val paymentSource: PaymentSourceDTO,
    val phone: String = PHONE_NUMBER,
) {

    data class PaymentSourceDTO(
        @SerializedName("gateway_id") val gatewayId: String? = null,
        @SerializedName("wallet_type") val walletType: String? = null,
        @SerializedName("vault_token") val vaultToken: String? = null,
        @SerializedName("created_at") val createdAt: String? = null,
        @SerializedName("add_attempts") val addAttempts: List<String>? = null,
        @SerializedName("card_type") val cardType: String? = null,
        @SerializedName("address_line1") val addressLine1: String? = null,
        @SerializedName("address_line2") val addressLine2: String? = null,
        @SerializedName("address_line3") val addressLine3: String? = null,
        @SerializedName("address_city") val city: String? = null,
        @SerializedName("address_state") val state: String? = null,
        @SerializedName("address_country") val countryCode: String? = null,
        @SerializedName("address_postcode") val postalCode: String? = null,
    )
}
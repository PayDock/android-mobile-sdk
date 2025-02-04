package com.paydock.sample.feature.card.data.api.dto

import com.google.gson.annotations.SerializedName

sealed class VaultTokenRequest {

    // Test Cards: https://docs.activeserver.cloud/en/guides/testlabs/
    data class CreateCardVaultTokenRequest(
        @SerializedName("card_ccv") val cvv: String = "123",
        @SerializedName("card_name") val cardholderName: String = "Test Card",
        @SerializedName("card_number") val cardNumber: String = "4100000000005000",
        @SerializedName("expire_month") val expiryMonth: String = "08",
        @SerializedName("expire_year") val expiryYear: String = "25",
    ) : VaultTokenRequest()

    data class CreateCardSessionVaultTokenRequest(
        val token: String,
        @SerializedName("vault_type") val vaultType: String = "session",
    ) : VaultTokenRequest()
}
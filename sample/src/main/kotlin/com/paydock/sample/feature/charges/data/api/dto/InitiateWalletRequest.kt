package com.paydock.sample.feature.charges.data.api.dto

import com.google.gson.annotations.SerializedName
import com.paydock.sample.core.AMOUNT
import com.paydock.sample.core.AU_COUNTRY_CODE
import com.paydock.sample.core.AU_CURRENCY_CODE
import com.paydock.sample.core.FIRST_NAME
import com.paydock.sample.core.LAST_NAME
import com.paydock.sample.core.MERCHANT_NAME
import com.paydock.sample.core.PHONE_NUMBER
import java.math.BigDecimal
import java.util.UUID

data class InitiateWalletRequest(
    val amount: BigDecimal = BigDecimal(AMOUNT),
    val currency: String,
    val customer: ChargesCustomerDTO,
    val description: String = "description007",
    val meta: MetaDTO = MetaDTO(),
    val reference: String = UUID.randomUUID().toString(),
    val shippingDTO: ShippingDTO? = null,
    val itemDTOS: List<ItemDTO>? = null,
) {

    data class ShippingDTO(
        @SerializedName("address_line1") val addressLine1: String = "ship1",
        @SerializedName("address_line2") val addressLine2: String = "ship22",
        @SerializedName("address_line3") val addressLine3: String = "ship3",
        @SerializedName("address_city") val city: String = "shipcity",
        @SerializedName("address_state") val state: String = "shipstate",
        @SerializedName("address_country") val countryCode: String = AU_COUNTRY_CODE,
        @SerializedName("address_postcode") val postalCode: String = "123456",
        val amount: BigDecimal = BigDecimal(AMOUNT),
        val contact: ContactDTO = ContactDTO(),
        val currency: String = AU_CURRENCY_CODE,
    ) {
        data class ContactDTO(
            @SerializedName("first_name") val firstName: String = FIRST_NAME,
            @SerializedName("last_name") val lastName: String = LAST_NAME,
            val phone: String = PHONE_NUMBER,
        )
    }

    data class ItemDTO(
        val amount: BigDecimal = BigDecimal(AMOUNT),
        @SerializedName("image_uri") val imageUri: String = "https://johndoesupplies.com/image",
        @SerializedName("item_uri") val itemUri: String = "https://johndoesupplies.com/1",
        val name: String = "itemname",
        val quantity: Int = 1,
        val type: String = "itemtype",
    )

    data class MetaDTO(
        @SerializedName("store_id") val storeId: String = "007",
        @SerializedName("store_name") val storeName: String = MERCHANT_NAME,
        @SerializedName("merchant_name") val merchantName: String = MERCHANT_NAME,
        @SerializedName("success_url") val successUrl: String? = null,
        @SerializedName("error_url") val errorUrl: String? = null,
    )
}


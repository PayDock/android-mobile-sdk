package com.paydock.sample.feature.wallet.data.api.dto

import com.google.gson.annotations.SerializedName
import com.paydock.sample.core.AMOUNT
import java.math.BigDecimal

data class Item(
    val amount: BigDecimal = BigDecimal(AMOUNT),
    @SerializedName("image_uri") val imageUri: String = "https://johndoesupplies.com/image",
    @SerializedName("item_uri") val itemUri: String = "https://johndoesupplies.com/1",
    val name: String = "itemname",
    val quantity: Int = 1,
    val type: String = "itemtype"
)
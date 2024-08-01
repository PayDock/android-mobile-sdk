package com.paydock.sample.feature.checkout.data.api.dto

import com.google.gson.annotations.SerializedName
import com.paydock.sample.core.extensions.toHexCode
import com.paydock.sample.designsystems.theme.OffWhite
import com.paydock.sample.designsystems.theme.PaydockBlack
import com.paydock.sample.designsystems.theme.Teal

data class Base(
    @SerializedName("font_family") val fontFamily: String = "Roboto",
    @SerializedName("font_size") val fontSize: String = "14px",
    @SerializedName("background_color") val backgroundColor: String = OffWhite.toHexCode(),
    @SerializedName("text_color") val textColor: String = PaydockBlack.toHexCode(),
    @SerializedName("border_color") val borderColor: String = PaydockBlack.toHexCode(),
    @SerializedName("button_color") val buttonColor: String = Teal.toHexCode()
)
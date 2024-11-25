package com.paydock.feature.src.domain.model.integration.meta

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents styles for UI customization.
 *
 * @property buttonTextColor The color of the button text.
 * @property primaryColor The primary color used for UI elements.
 * @property fontFamily The font family used for text.
 * @property cardSchemes The list of card schemes.
 */
@Serializable
data class Styles(
    @SerialName("button_text_color") val buttonTextColor: String? = null,
    @SerialName("primary_color") val primaryColor: String? = null,
    @SerialName("font_family") val fontFamily: String? = null,
    @SerialName("card_schemes") val cardSchemes: List<String>? = null
)

package com.paydock.feature.src.domain.model.ui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents card data.
 *
 * @property cardNumberBin The BIN (Bank Identification Number) of the card.
 * @property cardNumberLast4 The last four digits of the card number.
 * @property cardScheme The scheme of the card (e.g. "mastercard", "visa").
 * @property cardType The type of the card (e.g. "credit").
 */
@Serializable
internal data class CardData(
    @SerialName("card_number_bin") val cardNumberBin: String?,
    @SerialName("card_number_last4") val cardNumberLast4: String?,
    @SerialName("card_scheme") val cardScheme: String?,
    @SerialName("card_type") val cardType: String?
)

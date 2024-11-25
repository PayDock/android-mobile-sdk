package com.paydock.feature.afterpay.domain.model.integration

import com.paydock.core.utils.serializer.BigDecimalSerializer
import com.paydock.core.utils.serializer.CurrencySerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.Currency

/**
 * Represents an update to a shipping option for Afterpay transactions.
 *
 * @property id The unique identifier of the shipping option.
 * @property currency The currency of the shipping option.
 * @property shippingAmount The updated shipping amount associated with the shipping option.
 * @property orderAmount The updated order amount associated with the shipping option.
 * @property taxAmount The updated tax amount associated with the shipping option, nullable.
 */
data class AfterpayShippingOptionUpdate(
    val id: String,
    @Serializable(with = CurrencySerializer::class) val currency: Currency,
    @Serializable(with = BigDecimalSerializer::class) var shippingAmount: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class) var orderAmount: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class) var taxAmount: BigDecimal?
)

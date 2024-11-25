package com.paydock.feature.afterpay.domain.model.integration

import com.paydock.core.utils.serializer.BigDecimalSerializer
import com.paydock.core.utils.serializer.CurrencySerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.Currency

/**
 * Represents a shipping option available for Afterpay transactions.
 *
 * @property id The unique identifier of the shipping option.
 * @property name The name of the shipping option.
 * @property description The description of the shipping option.
 * @property currency The currency of the shipping option.
 * @property shippingAmount The shipping amount associated with the shipping option.
 * @property orderAmount The order amount associated with the shipping option.
 * @property taxAmount The tax amount associated with the shipping option, nullable.
 */
data class AfterpayShippingOption(
    val id: String,
    val name: String,
    val description: String,
    @Serializable(with = CurrencySerializer::class) val currency: Currency,
    @Serializable(with = BigDecimalSerializer::class) var shippingAmount: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class) var orderAmount: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class) var taxAmount: BigDecimal?,
)

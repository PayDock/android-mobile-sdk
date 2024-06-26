package com.paydock.core.utils.jwt.models

import com.paydock.core.utils.serializer.BigDecimalSerializer
import com.paydock.core.utils.serializer.CurrencySerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.Currency

@Serializable
data class Charge(
    @Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal,
    val capture: Boolean,
    @Serializable(with = CurrencySerializer::class) val currency: Currency,
    val id: String,
    val meta: MetaX,
    val reference: String
)
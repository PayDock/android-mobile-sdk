package com.paydock.sample.feature.wallet.data.api.dto

import com.paydock.sample.core.AMOUNT
import java.math.BigDecimal
import java.util.UUID

data class InitiateWalletRequest(
    val amount: BigDecimal = BigDecimal(AMOUNT),
    val currency: String,
    val customer: Customer,
    val description: String = "description007",
    val meta: Meta = Meta(),
    val reference: String = UUID.randomUUID().toString(),
    val shipping: Shipping? = null,
    val items: List<Item>? = null,
)
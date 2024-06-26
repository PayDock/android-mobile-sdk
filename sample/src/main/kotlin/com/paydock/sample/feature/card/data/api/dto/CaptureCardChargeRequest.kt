package com.paydock.sample.feature.card.data.api.dto

import com.paydock.sample.core.AMOUNT
import com.paydock.sample.feature.wallet.data.api.dto.Customer
import java.math.BigDecimal

data class CaptureCardChargeRequest(
    val amount: BigDecimal = BigDecimal(AMOUNT),
    val currency: String,
    val customer: Customer,
)
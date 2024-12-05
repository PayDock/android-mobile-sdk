package com.paydock.sample.feature.charges.data.api.dto

import com.paydock.sample.core.AMOUNT
import java.math.BigDecimal

data class CaptureCardChargeRequest(
    val amount: BigDecimal = BigDecimal(AMOUNT),
    val currency: String,
    val customer: ChargesCustomerDTO,
)
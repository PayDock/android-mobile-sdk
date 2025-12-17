package com.paydock.sample.feature.card.data.api.dto

import com.paydock.sample.feature.checkout.data.api.dto.ChargesCustomerDTO
import java.math.BigDecimal

data class CaptureCardChargeRequest(
    val amount: BigDecimal,
    val currency: String,
    val customer: ChargesCustomerDTO,
)
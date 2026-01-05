package com.paydock.sample.feature.zip.data.api.dto

import java.math.BigDecimal

/**
 * Request body for creating a Zip charge via POST /v1/charges
 * @param amount The charge amount
 * @param currency The currency code (e.g., "AUD")
 * @param token The Zip payment token
 */
data class CaptureZipChargeRequest(
    val amount: BigDecimal,
    val currency: String,
    val token: String,
)


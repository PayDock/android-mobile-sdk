package com.paydock.feature.zip.domain.model

/**
 * Represents the result of a successful Zip payment flow.
 *
 * @property token The payment source token generated after successful Zip checkout.
 */
data class ZipResult(
    val token: String
)

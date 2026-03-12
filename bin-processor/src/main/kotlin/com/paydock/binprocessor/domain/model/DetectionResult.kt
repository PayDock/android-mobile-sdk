package com.paydock.binprocessor.domain.model

/**
 * Result of card scheme detection.
 *
 * @property scheme The detected scheme name (e.g., "visa", "mastercard")
 * @property detectedAt The number of digits at which the scheme was detected (1, 2, 4, or 6)
 * @property length The length of the PAN that was checked
 */
data class DetectionResult(
    val scheme: String,
    val detectedAt: Int,
    val length: Int
)

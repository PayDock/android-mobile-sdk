package com.paydock.feature.zip.domain.model

/**
 * Result data from Zip callback after user completes the checkout flow.
 *
 * @property status The status/result of the transaction.
 * @property checkoutId The checkout ID from Zip.
 * @property orderId Additional order ID (legacy parameter).
 */
data class ZipCallbackData(
    val status: ZipStatus,
    val checkoutId: String? = null,
    val orderId: String? = null
) {
    /**
     * The primary identifier (prefers checkoutId, falls back to orderId).
     */
    val identifier: String?
        get() = checkoutId ?: orderId
}

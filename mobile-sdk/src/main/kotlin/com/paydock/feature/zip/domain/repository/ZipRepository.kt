package com.paydock.feature.zip.domain.repository

import com.paydock.feature.zip.domain.model.integration.ZipWidgetConfig

/**
 * Repository interface for Zip payment operations.
 *
 * This interface defines the contract for interacting with Zip payment services,
 * including initializing checkout sessions and creating payment source tokens.
 */
internal interface ZipRepository {

    /**
     * Initializes an external checkout session with Zip.
     *
     * This method creates a checkout session and returns the checkout URL and token
     * needed to complete the payment flow.
     *
     * @param accessToken The access token for API authentication.
     * @param config The Zip widget configuration containing payment details.
     * @return A pair containing the checkout URL (link) and checkout token.
     */
    suspend fun initializeExternalCheckout(
        accessToken: String,
        config: ZipWidgetConfig
    ): Pair<String, String>

    /**
     * Creates a payment source token from the checkout token.
     *
     * This method is called after the user completes the Zip checkout flow
     * to convert the checkout token into a payment source token.
     *
     * @param accessToken The access token for API authentication.
     * @param checkoutToken The checkout token received from the Zip checkout flow.
     * @param gatewayId The gateway ID for processing.
     * @return The payment source token string.
     */
    suspend fun createPaymentSourceToken(
        accessToken: String,
        checkoutToken: String,
        gatewayId: String
    ): String
}

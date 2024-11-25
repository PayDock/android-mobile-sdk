package com.paydock.feature.paypal.fraud.domain.model.integration

/**
 * Configuration data class used for initializing the PayPalDataCollector.
 *
 * @property accessToken The access token used for authentication with the PayPal API.
 *                       This token is required to interact with PayPal services.
 * @property gatewayId The identifier for the specific payment gateway being used.
 *                     This ID helps in directing requests to the appropriate PayPal gateway.
 */
data class PayPalDataCollectorConfig(
    val accessToken: String,
    val gatewayId: String
)

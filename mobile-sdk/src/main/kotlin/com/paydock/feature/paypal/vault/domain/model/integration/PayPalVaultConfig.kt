package com.paydock.feature.paypal.vault.domain.model.integration

/**
 * Configuration data class for PayPal Vault, containing the parameters required for API integration and UI customization.
 *
 * This class encapsulates both API authentication details and optional UI configurations, allowing for flexibility
 * in managing PayPal Vault interactions and appearance.
 *
 * @property accessToken The OAuth access token required for authenticating API requests to the PayPal Vault services.
 * @property gatewayId The unique identifier for the payment gateway used to route transactions.
 */
data class PayPalVaultConfig(
    val accessToken: String,
    val gatewayId: String,
)
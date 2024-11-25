package com.paydock.feature.paypal.vault.domain.model.integration

/**
 * PayPal Vault configuration data class that holds the necessary parameters for making API requests.
 *
 * @property accessToken The OAuth access token required for authenticating API requests.
 * @property gatewayId The gateway ID used to identify the payment gateway.
 * @property actionText The text to be displayed on the button. Defaults to "Link PayPal account".
 */
data class PayPalVaultConfig(
    val accessToken: String,
    val gatewayId: String,
    val actionText: String? = null
)
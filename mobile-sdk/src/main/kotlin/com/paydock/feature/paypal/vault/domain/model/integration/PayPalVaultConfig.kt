package com.paydock.feature.paypal.vault.domain.model.integration

import com.paydock.R

/**
 * Configuration data class for PayPal Vault, containing the parameters required for API integration and UI customization.
 *
 * This class encapsulates both API authentication details and optional UI configurations, allowing for flexibility
 * in managing PayPal Vault interactions and appearance.
 *
 * @property accessToken The OAuth access token required for authenticating API requests to the PayPal Vault services.
 * @property gatewayId The unique identifier for the payment gateway used to route transactions.
 * @property actionText Optional text to display on the button. If null, the default text "Link PayPal account" will be used.
 * @property icon Optional icon to display on the button, defined as a `ButtonIcon`. This allows for custom vector
 * or drawable resources to enhance the button's visual presentation.
 */
data class PayPalVaultConfig(
    val accessToken: String,
    val gatewayId: String,
    val actionText: String? = null,
    val icon: ButtonIcon? = ButtonIcon.DrawableRes(R.drawable.ic_link)
)
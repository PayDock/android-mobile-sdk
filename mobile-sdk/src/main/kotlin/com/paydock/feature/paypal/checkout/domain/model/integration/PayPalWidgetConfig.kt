package com.paydock.feature.paypal.checkout.domain.model.integration

/**
 * Configuration for the PayPal widget integration.
 *
 * This data class holds various configuration options that can be used to customize the behavior
 * of the PayPal widget when presented to the user.
 *
 * @property requestShipping A boolean indicating whether shipping information should be requested
 * from the user during the PayPal checkout flow. Defaults to `true`.
 */
data class PayPalWidgetConfig(
    val requestShipping: Boolean = true
)
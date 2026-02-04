package com.paydock.feature.afterpay.domain.model.integration

import java.util.Locale

/**
 * Configuration class for Afterpay SDK settings.
 *
 * @property locale Optional locale for SDK initialisation (currency/language). When null, [Locale.getDefault] is used.
 * @property options Additional checkout options for Afterpay.
 */
data class AfterpaySDKConfig(
    val locale: Locale? = null,
    val options: CheckoutOptions? = null
) {

    /**
     * Additional checkout options for Afterpay.
     *
     * @property pickup Indicates whether pickup option is enabled.
     * @property buyNow Indicates whether buy now option is enabled.
     * @property shippingOptionRequired Indicates whether shipping option is required.
     * @property enableSingleShippingOptionUpdate Indicates whether single shipping option update is enabled.
     */
    data class CheckoutOptions(
        val pickup: Boolean? = null,
        val buyNow: Boolean? = null,
        val shippingOptionRequired: Boolean? = null,
        val enableSingleShippingOptionUpdate: Boolean? = null
    )
}

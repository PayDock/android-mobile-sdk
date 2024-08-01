package com.paydock.feature.afterpay.presentation.model

import com.afterpay.android.view.AfterpayColorScheme
import com.afterpay.android.view.AfterpayPaymentButton
import java.util.Locale

/**
 * Configuration class for Afterpay SDK settings.
 *
 * @property buttonTheme The theme settings for Afterpay payment button.
 * @property config The main configuration settings for Afterpay.
 * @property options Additional checkout options for Afterpay.
 */
class AfterpaySDKConfig(
    val buttonTheme: ButtonTheme = ButtonTheme(),
    val config: AfterpayConfiguration,
    val options: CheckoutOptions? = null
) {
    /**
     * Theme settings for the Afterpay payment button.
     *
     * @property buttonText The text displayed on the payment button.
     * @property colorScheme The color scheme of the payment button.
     */
    data class ButtonTheme(
        val buttonText: AfterpayPaymentButton.ButtonText = AfterpayPaymentButton.ButtonText.DEFAULT,
        val colorScheme: AfterpayColorScheme = AfterpayColorScheme.BLACK_ON_MINT
    )

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

    /**
     * Main configuration settings for Afterpay.
     *
     * @property minimumAmount The minimum transaction amount allowed.
     * @property maximumAmount The maximum transaction amount allowed.
     * @property currency The currency for transactions.
     * @property language The language for localization, default is device's language.
     * @property country The country for localization, default is device's country.
     */
    data class AfterpayConfiguration(
        val minimumAmount: String? = null,
        val maximumAmount: String,
        val currency: String,
        val language: String = Locale.getDefault().language,
        val country: String = Locale.getDefault().country
    )
}

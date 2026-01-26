package com.paydock.feature.card.domain.model.integration

/**
 * Configuration for the Card Details Widget.
 *
 * This class defines the settings and behavior of the Card Details Widget, allowing customization of
 * cardholder information collection, action button text, save card options, and supported card schemes.
 *
 * @property accessToken The access token required for authenticating widget operations. This is mandatory for all requests.
 * @property gatewayId An optional identifier for the payment gateway. If not specified, the default gateway is used. Defaults to `null`.
 * @property collectCardholderName Specifies whether the widget should prompt the user to input the cardholder's name. Defaults to `true`.
 * @property allowSaveCard Configures whether users are allowed to save their card for future use. If `null`,
 * the save card option is disabled.
 * @property storeSecurityCode Specifies whether the security code (CVV) should be saved when tokenizing a card.
 * If `null`, the `store_ccv` parameter will not be sent in the tokenization request.
 * If `true`, `store_ccv` will be set to `true`.
 * If `false`, `store_ccv` will be set to `false`.
 * @property schemeSupport Configuration for supported card schemes and scheme validation behavior. Defaults to
 * [SupportedSchemeConfig] with no restrictions on card schemes and validation disabled.
 */
data class CardDetailsWidgetConfig(
    val accessToken: String,
    val gatewayId: String? = null,
    val collectCardholderName: Boolean = true,
    val allowSaveCard: SaveCardConfig? = null,
    val storeSecurityCode: Boolean? = null,
    val schemeSupport: SupportedSchemeConfig = SupportedSchemeConfig()
)
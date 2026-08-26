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
 * @property activePrimaryButton Specifies whether the primary button (e.g., Submit) should be enabled by default. If `true`, the button
 * is always enabled, and validation is performed upon clicking it. If `false`, the button remains disabled until
 * all fields are valid. Defaults to `true`.
 *   Defaults to true.
 * @property showSubmitButton Specifies whether the widget renders its own built-in primary (Submit) button. If `false`,
 * the widget hides its button entirely so a host app can supply its own trigger UI — see [CardDetailsWidgetState].
 * Defaults to `true`.
 */
data class CardDetailsWidgetConfig(
    val accessToken: String,
    val gatewayId: String? = null,
    val collectCardholderName: Boolean = true,
    val allowSaveCard: SaveCardConfig? = null,
    val storeSecurityCode: Boolean? = null,
    val schemeSupport: SupportedSchemeConfig = SupportedSchemeConfig(),
    val activePrimaryButton: Boolean = true,
    val showSubmitButton: Boolean = true
)

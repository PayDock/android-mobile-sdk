package com.paydock.feature.card.domain.model.integration

import com.paydock.core.MobileSDKConstants

/**
 * Configuration for the Card Details Widget.
 *
 * This class defines the settings and behavior of the Card Details Widget, allowing customization of
 * cardholder information collection, action button text, save card options, and supported card schemes.
 *
 * @property accessToken The access token required for authenticating widget operations. This is mandatory for all requests.
 * @property gatewayId An optional identifier for the payment gateway. If not specified, the default gateway is used. Defaults to `null`.
 * @property collectCardholderName Specifies whether the widget should prompt the user to input the cardholder's name. Defaults to `true`.
 * @property actionText The text displayed on the action button in the widget. Defaults to the value defined by
 * [MobileSDKConstants.CardDetailsConfig.DEFAULT_ACTION_TEXT].
 * @property showCardTitle Determines whether the widget displays a title for the card section above the input fields. Defaults to `true`.
 * @property allowSaveCard Configures whether users are allowed to save their card for future use. If `null`,
 * the save card option is disabled.
 * @property schemeSupport Configuration for supported card schemes and scheme validation behavior. Defaults to
 * [SupportedSchemeConfig] with no restrictions on card schemes and validation disabled.
 */
data class CardDetailsWidgetConfig(
    val accessToken: String,
    val gatewayId: String? = null,
    val collectCardholderName: Boolean = true,
    val actionText: String = MobileSDKConstants.CardDetailsConfig.DEFAULT_ACTION_TEXT,
    val showCardTitle: Boolean = true,
    val allowSaveCard: SaveCardConfig? = null,
    val schemeSupport: SupportedSchemeConfig = SupportedSchemeConfig()
)
package com.paydock.feature.card.domain.model.integration

import com.paydock.core.MobileSDKConstants
import com.paydock.feature.card.domain.model.integration.enums.CardScheme

/**
 * Configuration class for the Card Details Widget.
 *
 * This class defines the settings and behavior of the Card Details Widget,
 * including cardholder information, action text, save card options, and supported card schemes.
 *
 * @property accessToken The access token used for authenticating requests. This is required for all widget operations.
 * @property gatewayId An optional identifier for the payment gateway. If not provided, the default gateway is used. Default is `null`.
 * @property collectCardholderName Determines whether the widget should collect the cardholder's name. Default is `true`.
 * @property actionText The text to display for the action button. Defaults to the value defined by
 * [MobileSDKConstants.CardDetailsConfig.DEFAULT_ACTION_TEXT].
 * @property showCardTitle Indicates whether to display the card title above the input fields. Default is `true`.
 * @property allowSaveCard Configuration that determines if users are allowed to save their card for future use.
 * If `null`, saving cards is not allowed.
 * @property supportedSchemes A list of supported card schemes for the widget, or `null` if all schemes are supported.
 * This allows you to restrict the card types the widget accepts.
 */
data class CardDetailsWidgetConfig(
    val accessToken: String,
    val gatewayId: String? = null,
    val collectCardholderName: Boolean = true,
    val actionText: String = MobileSDKConstants.CardDetailsConfig.DEFAULT_ACTION_TEXT,
    val showCardTitle: Boolean = true,
    val allowSaveCard: SaveCardConfig? = null,
    val supportedSchemes: List<CardScheme>? = null
)
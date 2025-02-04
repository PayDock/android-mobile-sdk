package com.paydock.feature.card.domain.model.integration

import com.paydock.feature.card.domain.model.integration.enums.CardType

/**
 * Configuration for supported card schemes in the widget.
 *
 * @property supportedSchemes A set of supported card schemes for the widget. If `null`, all card schemes are accepted.
 * This property allows you to restrict the card types the widget will accept.
 * @property enableValidation A flag indicating whether validation of the card scheme against `supportedSchemes` is required.
 * When set to `false`, the widget does not enforce restrictions based on `supportedSchemes`, even if provided.
 */
data class SupportedSchemeConfig(
    val supportedSchemes: Set<CardType>? = null,
    val enableValidation: Boolean = false
)

package com.paydock.feature.card.domain.model.integration

/**
 * Configuration class for the Gift Card Widget.
 *
 * This class holds the necessary configuration parameters to initialize and customize the Gift Card Widget.
 *
 * @property accessToken The access token required to authenticate and interact with the gift card service.
 *   This token is essential for making API calls and performing gift card related operations.
 * @property storePin A boolean flag indicating whether the gift card PIN should be stored.
 *   If set to true, the widget may handle or store the PIN internally for future use or convenience.
 *   If set to false, the widget should not retain the PIN after use or initialization.
 *   Defaults to true.
 */
data class GiftCardWidgetConfig(
    val accessToken: String,
    val storePin: Boolean = true
)

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
 * @property activePrimaryButton Specifies whether the primary button (e.g., Submit) should be enabled by default. If `true`,
 *   the button is always enabled, and validation is performed upon clicking it. If `false`, the button remains disabled until
 *   all fields are valid. Defaults to true.
 * @property showSubmitButton Specifies whether the widget renders its own built-in primary (Add) button. If `false`,
 *   the widget hides its button entirely so a host app can supply its own trigger UI — see [GiftCardWidgetState].
 *   Defaults to `true`.
 */
data class GiftCardWidgetConfig(
    val accessToken: String,
    val storePin: Boolean = true,
    val activePrimaryButton: Boolean = true,
    val showSubmitButton: Boolean = true
)

package com.paydock.feature.src.domain.model.integration

import com.paydock.feature.src.domain.model.integration.meta.ClickToPayMeta

/**
 * Configuration data class for the Click to Pay widget.
 *
 * This class holds the necessary information to initialize and configure the Click to Pay widget.
 *
 * @property serviceId The service ID required for the Click to Pay integration.
 * @property accessToken The access token used for authenticating with the Click to Pay service.
 * @property meta Optional metadata associated with the Click to Pay transaction.
 */
data class ClickToPayWidgetConfig(
    val serviceId: String,
    val accessToken: String,
    val meta: ClickToPayMeta? = null,
)
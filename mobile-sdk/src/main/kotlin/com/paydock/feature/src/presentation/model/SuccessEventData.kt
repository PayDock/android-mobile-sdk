package com.paydock.feature.src.presentation.model

import com.paydock.feature.src.presentation.model.enum.EventDataType
import kotlinx.serialization.Serializable

/**
 * Represents additional data associated with a successful checkout event.
 *
 * @property type The type of event data.
 * @property data Additional data containing information about the successful checkout.
 */
@Serializable
internal data class SuccessEventData(
    val type: EventDataType = EventDataType.SUCCESS,
    val data: CheckoutData
)

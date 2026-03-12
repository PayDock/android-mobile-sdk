package com.paydock.feature.threeDS.integrated.domain.model.ui

import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.IntegratedEvent
import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.MPGS3dsStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing the event data for MPGS 3DS flows.
 *
 * This class contains details related to MPGS charge events such as charge ID,
 * status, widget ID, and additional information like purpose and message source.
 *
 * @property status The status of the charge event.
 * @property charge3dsId Charge 3DS ID for tracking the authentication process.
 * @property event The type of event ([IntegratedEvent]).
 * @property purpose An optional string to describe the purpose of the event.
 * @property messageSource Optional source of the message.
 * @property refId Reference ID for the event.
 * @property widgetId Widget ID associated with the event.
 */
@Serializable
internal data class MPGS3dsChargeEventData(
    val status: MPGS3dsStatus?,
    @SerialName("charge_3ds_id") val charge3dsId: String? = null,
    val event: IntegratedEvent,
    val purpose: String? = null,
    @SerialName("message_source") val messageSource: String? = null,
    @SerialName("ref_id") val refId: String = "",
    @SerialName("widget_id") val widgetId: String? = null
)
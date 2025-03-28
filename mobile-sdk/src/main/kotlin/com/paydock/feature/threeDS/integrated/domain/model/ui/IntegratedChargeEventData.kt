package com.paydock.feature.threeDS.integrated.domain.model.ui

import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.IntegratedEvent
import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.IntegratedStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing the event data for integrated 3DS flows.
 *
 * This class contains details related to integrated charge events such as charge ID,
 * status, widget ID, and additional information like purpose and message source.
 *
 * @property status The status of the charge event.
 * @property charge3dsId Charge 3DS ID for tracking the authentication process.
 * @property event The type of integrated 3DS event.
 * @property purpose An optional string to describe the purpose of the event.
 * @property messageSource Optional source of the message.
 * @property refId Reference ID for the event.
 * @property widgetId Widget ID associated with the event.
 */
@Serializable
internal data class IntegratedChargeEventData(
    val status: IntegratedStatus?,
    @SerialName("charge_3ds_id") val charge3dsId: String? = null,
    val event: IntegratedEvent,
    val purpose: String? = null,
    @SerialName("message_source") val messageSource: String? = null,
    @SerialName("ref_id") val refId: String = "",
    @SerialName("widget_id") val widgetId: String? = null
)
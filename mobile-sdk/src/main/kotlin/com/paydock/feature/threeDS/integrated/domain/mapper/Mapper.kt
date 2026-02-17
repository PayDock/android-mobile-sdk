package com.paydock.feature.threeDS.integrated.domain.mapper

import com.paydock.feature.threeDS.integrated.domain.model.integration.MPGS3dsResult
import com.paydock.feature.threeDS.integrated.domain.model.integration.enums.MPGS3dsEventType
import com.paydock.feature.threeDS.integrated.domain.model.ui.MPGS3dsEvent

/**
 * Converts an [MPGS3dsEvent] to a [MPGS3dsResult] entity.
 *
 * This function takes an MPGS 3DS event and extracts relevant information to
 * create a [MPGS3dsResult] object. It determines the type of the event (e.g., charge
 * authorization success, rejection, or additional data collection success/rejection)
 * and retrieves the associated charge ID.
 *
 * @return A [MPGS3dsResult] entity containing the event type and the charge ID.
 * @throws IllegalStateException if the data within the event is not properly formatted to be converted to a chargeIdProvider.
 *
 * @see MPGS3dsEvent
 * @see MPGS3dsResult
 * @see MPGS3dsEventType
 */
internal fun MPGS3dsEvent.asEntity(): MPGS3dsResult {
    val charge3dsId = this.data.charge3dsId
    val eventType = when (this) {
        is MPGS3dsEvent.ChargeAuthSuccessEvent -> MPGS3dsEventType.CHARGE_AUTH_SUCCESS
        is MPGS3dsEvent.ChargeAuthRejectEvent -> MPGS3dsEventType.CHARGE_AUTH_REJECT
        is MPGS3dsEvent.ChargeAuthCancelledEvent -> MPGS3dsEventType.CHARGE_AUTH_CANCELLATION
        is MPGS3dsEvent.AdditionalDataCollectSuccessEvent -> MPGS3dsEventType.ADDITIONAL_DATA_COLLECT_SUCCESS
        is MPGS3dsEvent.AdditionalDataCollectRejectEvent -> MPGS3dsEventType.ADDITIONAL_DATA_COLLECT_REJECT
        is MPGS3dsEvent.ChargeAuthEvent -> MPGS3dsEventType.CHARGE_AUTH
    }
    return MPGS3dsResult(eventType, charge3dsId)
}
package com.paydock.feature.threeDS.integrated.domain.mapper

import com.paydock.feature.threeDS.integrated.domain.model.integration.Integrated3DSResult
import com.paydock.feature.threeDS.integrated.domain.model.integration.enums.IntegratedEventType
import com.paydock.feature.threeDS.integrated.domain.model.ui.Integrated3DSEvent

/**
 * Converts an [Integrated3DSEvent] to a [Integrated3DSResult] entity.
 *
 * This function takes an integrated 3DS event and extracts relevant information to
 * create a [Integrated3DSResult] object. It determines the type of the event (e.g., charge
 * authorization success, rejection, or additional data collection success/rejection)
 * and retrieves the associated charge ID.
 *
 * @return A [Integrated3DSResult] entity containing the event type and the charge ID.
 * @throws IllegalStateException if the data within the event is not properly formatted to be converted to a chargeIdProvider.
 *
 * @see Integrated3DSEvent
 * @see Integrated3DSResult
 * @see IntegratedEventType
 */
internal fun Integrated3DSEvent.asEntity(): Integrated3DSResult {
    val charge3dsId = this.data.charge3dsId
    val eventType = when (this) {
        is Integrated3DSEvent.ChargeAuthSuccessEvent -> IntegratedEventType.CHARGE_AUTH_SUCCESS
        is Integrated3DSEvent.ChargeAuthRejectEvent -> IntegratedEventType.CHARGE_AUTH_REJECT
        is Integrated3DSEvent.ChargeAuthCancelledEvent -> IntegratedEventType.CHARGE_AUTH_CANCELLATION
        is Integrated3DSEvent.AdditionalDataCollectSuccessEvent -> IntegratedEventType.ADDITIONAL_DATA_COLLECT_SUCCESS
        is Integrated3DSEvent.AdditionalDataCollectRejectEvent -> IntegratedEventType.ADDITIONAL_DATA_COLLECT_REJECT
        is Integrated3DSEvent.ChargeAuthEvent -> IntegratedEventType.CHARGE_AUTH
    }
    return Integrated3DSResult(eventType, charge3dsId)
}
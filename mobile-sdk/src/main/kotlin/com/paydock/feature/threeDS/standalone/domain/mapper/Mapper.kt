package com.paydock.feature.threeDS.standalone.domain.mapper

import com.paydock.core.extensions.safeCastAs
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSResult
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.StandaloneEventType
import com.paydock.feature.threeDS.standalone.domain.model.ui.Standalone3DSEvent
import com.paydock.feature.threeDS.standalone.domain.model.ui.StandaloneChargeEventData
import com.paydock.feature.threeDS.standalone.domain.model.ui.asChargeIdProvider

/**
 * Converts a [Standalone3DSEvent] to a [Standalone3DSResult] entity.
 *
 * This function extracts relevant information from a [.Standalone3DSEvent]
 * and transforms it into a [Standalone3DSResult] object. It determines the event type
 * based on the specific subtype of the input event and extracts the charge ID
 * provider if available.
 *
 * @receiver The [Standalone3DSEvent] instance to be converted.
 * @return A [Standalone3DSResult] entity containing the extracted event type and charge ID.
 *         The charge ID will be null if it cannot be extracted from the event data.
 *
 * @see Standalone3DSResult
 * @see Standalone3DSEvent
 * @see StandaloneEventType
 * @see StandaloneChargeEventData
 */
internal fun Standalone3DSEvent.asEntity(): Standalone3DSResult {
    val chargeIdProvider = when (this) {
        is Standalone3DSEvent.ChargeErrorEvent -> this.data.asChargeIdProvider()
        else -> this.data.safeCastAs<StandaloneChargeEventData>()?.asChargeIdProvider()
    }
    val eventType = when (this) {
        is Standalone3DSEvent.ChargeAuthSuccessEvent -> StandaloneEventType.CHARGE_AUTH_SUCCESS
        is Standalone3DSEvent.ChargeAuthRejectEvent -> StandaloneEventType.CHARGE_AUTH_REJECT
        is Standalone3DSEvent.ChargeAuthDecoupledEvent -> StandaloneEventType.CHARGE_AUTH_DECOUPLED
        is Standalone3DSEvent.ChargeAuthInfoEvent -> StandaloneEventType.CHARGE_AUTH_INFO
        is Standalone3DSEvent.ChargeAuthChallengeEvent -> StandaloneEventType.CHARGE_AUTH_CHALLENGE
        is Standalone3DSEvent.ChargeErrorEvent -> StandaloneEventType.CHARGE_ERROR
    }
    return Standalone3DSResult(eventType, chargeIdProvider?.charge3dsId)
}
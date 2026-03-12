package com.paydock.feature.threeDS.integrated.domain.model.ui

import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.IntegratedEvent
import com.paydock.feature.threeDS.integrated.domain.utils.MPGS3dsEventSerializer
import kotlinx.serialization.Serializable

/**
 * Represents MPGS 3DS events that are handled through an external SDK.
 *
 * Events in this category are serialized using [MPGS3dsEventSerializer].
 */
@Serializable(with = MPGS3dsEventSerializer::class)
internal sealed class MPGS3dsEvent {

    /**
     * The type of MPGS 3DS event.
     */
    abstract val event: IntegratedEvent
    abstract val data: MPGS3dsChargeEventData

    /**
     * Represents a successful charge authentication event in the MPGS 3DS flow.
     *
     * @property event The event type ([IntegratedEvent.CHARGE_AUTH_SUCCESS]).
     * @property data Event data containing charge details.
     */
    @Serializable
    data class ChargeAuthSuccessEvent(
        override val event: IntegratedEvent = IntegratedEvent.CHARGE_AUTH_SUCCESS,
        override val data: MPGS3dsChargeEventData
    ) : MPGS3dsEvent()

    /**
     * Represents a charge authentication rejection event in the MPGS 3DS flow.
     *
     * @property event The event type ([IntegratedEvent.CHARGE_AUTH_REJECT]).
     * @property data Event data containing charge details.
     */
    @Serializable
    data class ChargeAuthRejectEvent(
        override val event: IntegratedEvent = IntegratedEvent.CHARGE_AUTH_REJECT,
        override val data: MPGS3dsChargeEventData
    ) : MPGS3dsEvent()

    @Serializable
    data class ChargeAuthCancelledEvent(
        override val event: IntegratedEvent = IntegratedEvent.CHARGE_AUTH_CANCELLED,
        override val data: MPGS3dsChargeEventData
    ) : MPGS3dsEvent()

    /**
     * Represents a successful additional data collection event in the MPGS 3DS flow.
     *
     * @property event The event type ([IntegratedEvent.ADDITIONAL_DATA_COLLECT_SUCCESS]).
     * @property data Event data containing collected information.
     */
    @Serializable
    data class AdditionalDataCollectSuccessEvent(
        override val event: IntegratedEvent = IntegratedEvent.ADDITIONAL_DATA_COLLECT_SUCCESS,
        override val data: MPGS3dsChargeEventData
    ) : MPGS3dsEvent()

    /**
     * Represents an additional data collection rejection event in the MPGS 3DS flow.
     *
     * @property event The event type ([IntegratedEvent.ADDITIONAL_DATA_COLLECT_REJECT]).
     * @property data Event data containing rejection details.
     */
    @Serializable
    data class AdditionalDataCollectRejectEvent(
        override val event: IntegratedEvent = IntegratedEvent.ADDITIONAL_DATA_COLLECT_REJECT,
        override val data: MPGS3dsChargeEventData
    ) : MPGS3dsEvent()

    /**
     * Represents a charge authentication event in the MPGS 3DS flow.
     *
     * @property event The event type ([IntegratedEvent.CHARGE_AUTH]).
     * @property data Event data containing authentication details.
     */
    @Serializable
    data class ChargeAuthEvent(
        override val event: IntegratedEvent = IntegratedEvent.CHARGE_AUTH,
        override val data: MPGS3dsChargeEventData
    ) : MPGS3dsEvent()
}
package com.paydock.feature.threeDS.integrated.domain.model.ui

import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.IntegratedEvent
import com.paydock.feature.threeDS.integrated.domain.utils.Integrated3DSEventSerializer
import kotlinx.serialization.Serializable

/**
 * Represents integrated 3DS events that are handled through an external SDK.
 *
 * Events in this category are serialized using [Integrated3DSEventSerializer].
 */
@Serializable(with = Integrated3DSEventSerializer::class)
internal sealed class Integrated3DSEvent {

    /**
     * The type of integrated 3DS event.
     */
    abstract val event: IntegratedEvent
    abstract val data: IntegratedChargeEventData

    /**
     * Represents a successful charge authentication event in the integrated 3DS flow.
     *
     * @property event The event type ([IntegratedEvent.CHARGE_AUTH_SUCCESS]).
     * @property data Event data containing charge details.
     */
    @Serializable
    data class ChargeAuthSuccessEvent(
        override val event: IntegratedEvent = IntegratedEvent.CHARGE_AUTH_SUCCESS,
        override val data: IntegratedChargeEventData
    ) : Integrated3DSEvent()

    /**
     * Represents a charge authentication rejection event in the integrated 3DS flow.
     *
     * @property event The event type ([IntegratedEvent.CHARGE_AUTH_REJECT]).
     * @property data Event data containing charge details.
     */
    @Serializable
    data class ChargeAuthRejectEvent(
        override val event: IntegratedEvent = IntegratedEvent.CHARGE_AUTH_REJECT,
        override val data: IntegratedChargeEventData
    ) : Integrated3DSEvent()

    @Serializable
    data class ChargeAuthCancelledEvent(
        override val event: IntegratedEvent = IntegratedEvent.CHARGE_AUTH_CANCELLED,
        override val data: IntegratedChargeEventData
    ) : Integrated3DSEvent()

    /**
     * Represents a successful additional data collection event in the integrated 3DS flow.
     *
     * @property event The event type ([IntegratedEvent.ADDITIONAL_DATA_COLLECT_SUCCESS]).
     * @property data Event data containing collected information.
     */
    @Serializable
    data class AdditionalDataCollectSuccessEvent(
        override val event: IntegratedEvent = IntegratedEvent.ADDITIONAL_DATA_COLLECT_SUCCESS,
        override val data: IntegratedChargeEventData
    ) : Integrated3DSEvent()

    /**
     * Represents an additional data collection rejection event in the integrated 3DS flow.
     *
     * @property event The event type ([IntegratedEvent.ADDITIONAL_DATA_COLLECT_REJECT]).
     * @property data Event data containing rejection details.
     */
    @Serializable
    data class AdditionalDataCollectRejectEvent(
        override val event: IntegratedEvent = IntegratedEvent.ADDITIONAL_DATA_COLLECT_REJECT,
        override val data: IntegratedChargeEventData
    ) : Integrated3DSEvent()

    /**
     * Represents a charge authentication event in the integrated 3DS flow.
     *
     * @property event The event type ([IntegratedEvent.CHARGE_AUTH]).
     * @property data Event data containing authentication details.
     */
    @Serializable
    data class ChargeAuthEvent(
        override val event: IntegratedEvent = IntegratedEvent.CHARGE_AUTH,
        override val data: IntegratedChargeEventData
    ) : Integrated3DSEvent()
}
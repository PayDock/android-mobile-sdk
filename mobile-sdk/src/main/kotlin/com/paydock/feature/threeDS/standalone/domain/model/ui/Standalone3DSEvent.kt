package com.paydock.feature.threeDS.standalone.domain.model.ui

import com.paydock.feature.threeDS.standalone.domain.model.ui.enums.StandaloneEvent
import com.paydock.feature.threeDS.standalone.domain.utils.Standalone3DSEventSerializer
import kotlinx.serialization.Serializable

/**
 * Represents standalone 3DS events that are not integrated with an external SDK.
 *
 * Events in this category are serialized using [Standalone3DSEventSerializer].
 */
@Serializable(with = Standalone3DSEventSerializer::class)
internal sealed class Standalone3DSEvent {

    /**
     * The type of standalone 3DS event.
     */
    abstract val event: StandaloneEvent
    abstract val data: Any

    /**
     * Represents a successful charge authentication event.
     *
     * @property event The event type ([StandaloneEvent.CHARGE_AUTH_SUCCESS]).
     * @property data Event data containing charge details.
     */
    @Serializable
    data class ChargeAuthSuccessEvent(
        override val event: StandaloneEvent = StandaloneEvent.CHARGE_AUTH_SUCCESS,
        override val data: StandaloneChargeEventData
    ) : Standalone3DSEvent()

    /**
     * Represents a charge authentication rejection event.
     *
     * @property event The event type ([StandaloneEvent.CHARGE_AUTH_REJECT]).
     * @property data Event data containing charge details.
     */
    @Serializable
    data class ChargeAuthRejectEvent(
        override val event: StandaloneEvent = StandaloneEvent.CHARGE_AUTH_REJECT,
        override val data: StandaloneChargeEventData
    ) : Standalone3DSEvent()

    /**
     * Represents a charge authentication challenge event.
     *
     * @property event The event type ([StandaloneEvent.CHARGE_AUTH_CHALLENGE]).
     * @property data Event data containing challenge details.
     */
    @Serializable
    data class ChargeAuthChallengeEvent(
        override val event: StandaloneEvent = StandaloneEvent.CHARGE_AUTH_CHALLENGE,
        override val data: StandaloneChargeEventData
    ) : Standalone3DSEvent()

    /**
     * Represents a decoupled charge authentication event.
     *
     * @property event The event type ([StandaloneEvent.CHARGE_AUTH_DECOUPLED]).
     * @property data Event data containing charge details.
     */
    @Serializable
    data class ChargeAuthDecoupledEvent(
        override val event: StandaloneEvent = StandaloneEvent.CHARGE_AUTH_DECOUPLED,
        override val data: StandaloneChargeEventData
    ) : Standalone3DSEvent()

    /**
     * Represents charge authentication information event.
     *
     * @property event The event type ([StandaloneEvent.CHARGE_AUTH_INFO]).
     * @property data Event data containing charge details.
     */
    @Serializable
    data class ChargeAuthInfoEvent(
        override val event: StandaloneEvent = StandaloneEvent.CHARGE_AUTH_INFO,
        override val data: StandaloneChargeEventData
    ) : Standalone3DSEvent()

    /**
     * Represents a charge error event.
     *
     * @property event The event type ([StandaloneEvent.CHARGE_ERROR]).
     * @property data Event data containing error details.
     */
    @Serializable
    data class ChargeErrorEvent(
        override val event: StandaloneEvent = StandaloneEvent.CHARGE_ERROR,
        override val data: ChargeErrorEventData
    ) : Standalone3DSEvent()
}

package com.paydock.feature.threeDS.presentation.model

import com.paydock.feature.threeDS.presentation.model.enum.Event
import com.paydock.feature.threeDS.presentation.utils.ThreeDSEventSerializer
import kotlinx.serialization.Serializable

/**
 * A sealed class representing various 3DS events that can occur during the authentication process.
 * Events include successful authentication, rejection, challenge, decoupled authentication, authentication information, and errors.
 */
@Serializable(with = ThreeDSEventSerializer::class)
internal sealed class ThreeDSEvent {

    /**
     * The type of the event.
     */
    abstract val event: Event

    /**
     * Serializable data class representing a successful charge authentication event.
     *
     * @property event The type of event (CHARGE_AUTH_SUCCESS).
     * @property data The event data containing information about the charge.
     */
    @Serializable
    data class ChargeAuthSuccessEvent(
        override val event: Event = Event.CHARGE_AUTH_SUCCESS,
        val data: ChargeEventData
    ) : ThreeDSEvent()

    /**
     * Serializable data class representing a charge authentication rejection event.
     *
     * @property event The type of event (CHARGE_AUTH_REJECT).
     * @property data The event data containing information about the charge.
     */
    @Serializable
    data class ChargeAuthRejectEvent(
        override val event: Event = Event.CHARGE_AUTH_REJECT,
        val data: ChargeEventData
    ) : ThreeDSEvent()

    /**
     * Serializable data class representing a charge authentication challenge event.
     *
     * @property event The type of event (CHARGE_AUTH_CHALLENGE).
     * @property data The event data containing information about the charge.
     */
    @Serializable
    data class ChargeAuthChallengeEvent(
        override val event: Event = Event.CHARGE_AUTH_CHALLENGE,
        val data: ChargeEventData
    ) : ThreeDSEvent()

    /**
     * Serializable data class representing a decoupled charge authentication event.
     *
     * @property event The type of event (CHARGE_AUTH_DECOUPLED).
     * @property data The event data containing information about the charge.
     */
    @Serializable
    data class ChargeAuthDecoupledEvent(
        override val event: Event = Event.CHARGE_AUTH_DECOUPLED,
        val data: ChargeEventData
    ) : ThreeDSEvent()

    /**
     * Serializable data class representing charge authentication information event.
     *
     * @property event The type of event (CHARGE_AUTH_INFO).
     * @property data The event data containing information about the charge.
     */
    @Serializable
    data class ChargeAuthInfoEvent(
        override val event: Event = Event.CHARGE_AUTH_INFO,
        val data: ChargeEventData
    ) : ThreeDSEvent()

    /**
     * Serializable data class representing a charge error event.
     *
     * @property event The type of event (CHARGE_ERROR).
     * @property data The event data containing error information.
     */
    @Serializable
    data class ChargeErrorEvent(
        override val event: Event = Event.CHARGE_ERROR,
        val data: ChargeErrorEventData
    ) : ThreeDSEvent()
}

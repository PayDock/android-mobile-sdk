package com.paydock.feature.threeDS.presentation.model.enum

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enum class representing different types of events related to 3D Secure (3DS) charge processing.
 *
 * Each enum constant corresponds to a specific event type, such as successful authentication,
 * rejection, challenge, decoupling, information, error, or an unknown event type.
 */
@Serializable
enum class Event {
    /**
     * Represents a successful 3DS charge authorization.
     */
    @SerialName("chargeAuthSuccess")
    CHARGE_AUTH_SUCCESS,

    /**
     * Represents a rejected 3DS charge authorization.
     */
    @SerialName("chargeAuthReject")
    CHARGE_AUTH_REJECT,

    /**
     * Represents a 3DS charge authorization with a challenge.
     */
    @SerialName("chargeAuthChallenge")
    CHARGE_AUTH_CHALLENGE,

    /**
     * Represents a decoupled 3DS charge authorization.
     */
    @SerialName("chargeAuthDecoupled")
    CHARGE_AUTH_DECOUPLED,

    /**
     * Represents an informational event related to a 3DS charge.
     */
    @SerialName("chargeAuthInfo")
    CHARGE_AUTH_INFO,

    /**
     * Represents an error event related to a 3DS charge.
     */
    @SerialName("error")
    CHARGE_ERROR
}
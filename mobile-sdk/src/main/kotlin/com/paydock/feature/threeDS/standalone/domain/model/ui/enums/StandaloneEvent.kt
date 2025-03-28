package com.paydock.feature.threeDS.standalone.domain.model.ui.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enum class representing various event types related to 3D Secure (3DS) charge processing.
 *
 * Each event type corresponds to a specific stage or outcome in the 3DS authentication flow,
 * such as successful authentication, rejection, challenge, decoupled authentication,
 * informational events, or errors.
 *
 * These events are serialized using `@SerialName` annotations to ensure proper
 * mapping between API responses and enum values.
 */
@Serializable
internal enum class StandaloneEvent {
    /**
     * Indicates that the 3DS charge authorization was successfully completed.
     */
    @SerialName("chargeAuthSuccess")
    CHARGE_AUTH_SUCCESS,

    /**
     * Indicates that the 3DS charge authorization was rejected.
     */
    @SerialName("chargeAuthReject")
    CHARGE_AUTH_REJECT,

    /**
     * Indicates that the 3DS charge authorization requires a user challenge step.
     */
    @SerialName("chargeAuthChallenge")
    CHARGE_AUTH_CHALLENGE,

    /**
     * Indicates that the 3DS charge authorization was completed using a decoupled flow.
     * This means authentication occurred asynchronously without direct user interaction.
     */
    @SerialName("chargeAuthDecoupled")
    CHARGE_AUTH_DECOUPLED,

    /**
     * Represents an informational event related to the 3DS charge process.
     * This event does not necessarily indicate success or failure.
     */
    @SerialName("chargeAuthInfo")
    CHARGE_AUTH_INFO,

    /**
     * Indicates that an error occurred during the 3DS charge authentication process
     * as well as any mapping/parsing failure.
     */
    @SerialName("error")
    CHARGE_ERROR
}

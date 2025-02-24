package com.paydock.feature.threeDS.integrated.domain.model.ui.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enum class representing different types of events related to integrated 3D Secure (3DS)
 * charge processing and additional data collection.
 *
 * These events cover various outcomes of 3DS authentication and data collection processes,
 * including success, rejection, and general authorization events.
 *
 * The `@SerialName` annotations ensure correct serialization and deserialization
 * when interacting with external APIs.
 */
@Serializable
internal enum class IntegratedEvent {

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
     * Indicates that the 3DS charge was cancelled.
     */
    @SerialName("chargeAuthCancelled")
    CHARGE_AUTH_CANCELLED,

    /**
     * Indicates that additional data collection was successfully completed.
     */
    @SerialName("additionalDataCollectSuccess")
    ADDITIONAL_DATA_COLLECT_SUCCESS,

    /**
     * Indicates that additional data collection was rejected.
     */
    @SerialName("additionalDataCollectReject")
    ADDITIONAL_DATA_COLLECT_REJECT,

    /**
     * Represents a general charge authorization event,
     * which may not specify success or failure.
     */
    @SerialName("chargeAuth")
    CHARGE_AUTH
}

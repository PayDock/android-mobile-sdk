package com.paydock.feature.threeDS.presentation.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an event related to 3D Secure (3DS) charge processing.
 *
 * This class is used to model events that may occur during the 3DS authentication process.
 * It includes information such as the event type and the associated 3DS charge ID.
 *
 * @param event The type of 3DS event, e.g., "chargeAuthSuccess", "chargeAuthReject", etc.
 * @param charge3dsId The unique identifier for the 3DS charge associated with the event.
 */
@Serializable
data class Charge3dsEvent(
    @SerialName("event") val event: String,
    @SerialName("charge3dsId") val charge3dsId: String
)

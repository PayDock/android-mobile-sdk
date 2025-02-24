package com.paydock.feature.threeDS.integrated.domain.model.integration

import com.paydock.feature.threeDS.integrated.domain.model.integration.enums.IntegratedEventType

/**
 * This class encapsulates the outcome of a 3D Secure authentication attempt. It provides details
 * about the event that occurred during the 3DS process and the associated Charge ID.
 *
 * @param event The type of event that occurred during 3DS processing. This indicates the
 *              status or result of the 3DS authentication. See the `EventType` enum for possible
 *              event values.
 * @param charge3dsId The unique identifier for the 3DS-related charge. This ID is essential for
 *                    correlating the 3DS authentication result with the corresponding charge in
 *                    the system. This value will be `null` if there is no associated charge.
 *                    Merchants can use this ID to retrieve the charge details from the system.
 */
data class Integrated3DSResult(
    val event: IntegratedEventType,
    val charge3dsId: String?
)

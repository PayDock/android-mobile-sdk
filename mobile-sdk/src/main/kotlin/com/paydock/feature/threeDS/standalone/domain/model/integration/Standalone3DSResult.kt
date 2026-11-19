package com.paydock.feature.threeDS.standalone.domain.model.integration

import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.Standalone3DSAuthenticationFlow
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.StandaloneEventType

/**
 * This class encapsulates the outcome of a 3D Secure authentication attempt. It provides details
 * about the event that occurred during the 3DS process and the associated Charge ID.
 *
 * [status] and [resultDescription] mirror the `status` and `result.description` fields of the web
 * SDK's `Canvas3ds` event payloads. They are optional and informational: existing integrations that only
 * read [event] and [charge3dsId] keep working unchanged.
 *
 * @param event The type of event that occurred during 3DS processing. This indicates the
 *              status or result of the 3DS authentication. See the `EventType` enum for possible
 *              event values.
 * @param charge3dsId The unique identifier for the 3DS-related charge. This ID is essential for
 *                    correlating the 3DS authentication result with the corresponding charge in
 *                    the system. This value will be `null` if there is no associated charge.
 *                    Merchants can use this ID to retrieve the charge details from the system.
 * @param status The authentication status string reported by the web SDK for this event, verbatim
 *               (e.g. `success`, `pending`, `rejected`, `error`). Informational only — use [event]
 *               to drive the flow. `null` when the event carries no status.
 * @param resultDescription A human-readable description of the outcome (the web SDK's `result.description`),
 *                          when the 3DS provider supplies one — typically only on frictionless or first-step
 *                          outcomes, e.g. `frictionless`. `null` otherwise, including on challenge results.
 * @param authenticationFlow How the authentication reached this result: set only on
 *                           [StandaloneEventType.CHARGE_AUTH_SUCCESS] and [StandaloneEventType.CHARGE_AUTH_REJECT],
 *                           `null` otherwise. It is worked out from the earlier events of the same [charge3dsId]:
 *                           [Standalone3DSAuthenticationFlow.CHALLENGE] or [Standalone3DSAuthenticationFlow.DECOUPLED]
 *                           after the latest challenge or decoupled event for that ID, else
 *                           [Standalone3DSAuthenticationFlow.FRICTIONLESS].
 */
data class Standalone3DSResult(
    val event: StandaloneEventType,
    val charge3dsId: String?,
    val status: String? = null,
    val resultDescription: String? = null,
    val authenticationFlow: Standalone3DSAuthenticationFlow? = null,
) {

    /**
     * Binary-compatibility bridge for code compiled against SDK versions before [status] and
     * [resultDescription] were added. Hidden from Kotlin sources, which resolve to the primary
     * constructor.
     */
    @Deprecated("Binary compatibility only", level = DeprecationLevel.HIDDEN)
    constructor(event: StandaloneEventType, charge3dsId: String?) : this(event, charge3dsId, null, null, null)

    /**
     * Binary-compatibility bridge for code compiled against SDK versions before [status] and
     * [resultDescription] were added. Hidden from Kotlin sources, which resolve to the generated `copy`.
     */
    @Deprecated("Binary compatibility only", level = DeprecationLevel.HIDDEN)
    fun copy(event: StandaloneEventType = this.event, charge3dsId: String? = this.charge3dsId): Standalone3DSResult =
        Standalone3DSResult(event, charge3dsId, status, resultDescription, authenticationFlow)

    /**
     * Binary-compatibility bridge for code compiled against SDK 5.7.x, before [authenticationFlow] was added.
     * Hidden from Kotlin sources, which resolve to the primary constructor. The default values are kept on purpose:
     * they make the compiler emit the same default-argument constructor 5.7.x callers link against.
     */
    @Deprecated("Binary compatibility only", level = DeprecationLevel.HIDDEN)
    constructor(
        event: StandaloneEventType,
        charge3dsId: String?,
        status: String? = null,
        resultDescription: String? = null,
    ) : this(event, charge3dsId, status, resultDescription, null)

    /**
     * Binary-compatibility bridge for code compiled against SDK 5.7.x, before [authenticationFlow] was added. Hidden
     * from Kotlin sources, which resolve to the generated `copy`; it keeps [authenticationFlow] unchanged. The default
     * values are kept on purpose: they make the compiler emit the same `copy$default` 5.7.x callers link against.
     */
    @Deprecated("Binary compatibility only", level = DeprecationLevel.HIDDEN)
    fun copy(
        event: StandaloneEventType = this.event,
        charge3dsId: String? = this.charge3dsId,
        status: String? = this.status,
        resultDescription: String? = this.resultDescription,
    ): Standalone3DSResult = Standalone3DSResult(event, charge3dsId, status, resultDescription, authenticationFlow)
}

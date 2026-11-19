package com.paydock.feature.threeDS.standalone.domain.model.integration.enums

/**
 * How a Standalone 3DS authentication reached its final result.
 *
 * Set on [com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSResult.authenticationFlow]
 * only for [StandaloneEventType.CHARGE_AUTH_SUCCESS] and [StandaloneEventType.CHARGE_AUTH_REJECT] results, worked out
 * from the earlier events of the same `charge3dsId`.
 */
enum class Standalone3DSAuthenticationFlow {
    /**
     * Frictionless: the authentication completed without any shopper step.
     */
    FRICTIONLESS,

    /**
     * Challenge: the shopper completed a challenge in the widget.
     */
    CHALLENGE,

    /**
     * Decoupled: the shopper approved the authentication outside the widget (e.g. in their banking app).
     */
    DECOUPLED
}

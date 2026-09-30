package com.paydock.feature.threeDS.standalone.domain.model.integration

import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.ChallengeCompletedSource
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.ChallengeLoadedReason

/**
 * Intermediate progress of a Standalone 3D Secure (3DS) authentication, delivered to the optional
 * `onProgress` callback of the Standalone3DSWidget.
 *
 * Progress events never replace the final result: the widget's `completion` callback still receives
 * the terminal outcome (success, reject or error). Use these events to drive your own UI, e.g. keep a
 * loader up until the bank's challenge page is visible and show a "Completing verification" state
 * once the shopper has finished the challenge.
 *
 * Typical order:
 * - Frictionless: no progress events, straight to the final result.
 * - Challenge: [ChallengeStarted] → [ChallengeLoaded] → [ChallengeCompleted] → final result.
 * - Decoupled: [Decoupled] → [ChallengeCompleted] → final result.
 *
 * New subtypes may be added in future minor versions as the 3DS flow gains steps — include an `else`
 * branch when using `when` on it.
 *
 * @property charge3dsId The 3DS charge identifier, if provided by the 3DS flow.
 */
sealed class Standalone3DSProgress {
    abstract val charge3dsId: String?

    /**
     * A challenge is required and the bank's challenge page is being loaded. It is not yet visible
     * to the shopper, so keep any loading indicator up until [ChallengeLoaded].
     *
     * @property charge3dsId The 3DS charge identifier, if provided by the 3DS flow.
     */
    data class ChallengeStarted(override val charge3dsId: String?) : Standalone3DSProgress()

    /**
     * The bank's challenge page is visible and the shopper can interact with it. Emitted once per
     * challenge, after [ChallengeStarted]. Never emitted for decoupled authentications.
     *
     * @property charge3dsId The 3DS charge identifier, if provided by the 3DS flow.
     * @property reason Whether the challenge page actually loaded or a safety timeout elapsed.
     *                  Informational only — treat both the same way.
     */
    data class ChallengeLoaded(
        override val charge3dsId: String?,
        val reason: ChallengeLoadedReason
    ) : Standalone3DSProgress()

    /**
     * The shopper finished the challenge (or approved a decoupled authentication) and the result is
     * being confirmed. Emitted at most once, always before the final result. A wrong OTP or a further
     * interactive step does not emit it.
     *
     * @property charge3dsId The 3DS charge identifier, if provided by the 3DS flow.
     * @property source How the completion was detected. Informational only — treat both the same way.
     */
    data class ChallengeCompleted(
        override val charge3dsId: String?,
        val source: ChallengeCompletedSource
    ) : Standalone3DSProgress()

    /**
     * The authentication is decoupled: the shopper must approve it outside of the widget,
     * e.g. in their banking app.
     *
     * @property charge3dsId The 3DS charge identifier, if provided by the 3DS flow.
     * @property description Shopper-facing text describing how to authenticate, if provided.
     *                       It should be shown to the shopper.
     */
    data class Decoupled(
        override val charge3dsId: String?,
        val description: String?
    ) : Standalone3DSProgress()
}

package com.paydock.sample.feature.checkout.models

/**
 * Phases of the standalone 3DS presentation in the checkout (mirrors the client-sdk playground modal).
 */
enum class Standalone3DSPhase {
    /** The 3DS token/charge is being created. */
    PREPARING,

    /** The widget is launched (fingerprinting / frictionless processing). */
    VERIFYING,

    /** A challenge is required; the bank's page is loading (still hidden). */
    CHALLENGE_LOADING,

    /** The bank's challenge page is visible. */
    CHALLENGE,

    /** The shopper finished the challenge; the result is being confirmed. */
    FINALIZING,

    /** The shopper must approve the payment in their banking app. */
    DECOUPLED,

    /** Authentication succeeded. */
    SUCCESS,

    /** Authentication was declined or failed. */
    FAILED
}

/**
 * State of the standalone 3DS sheet.
 *
 * @property phase The current phase.
 * @property decoupledDescription Shopper-facing description for [Standalone3DSPhase.DECOUPLED], if provided.
 * @property declined For [Standalone3DSPhase.FAILED]: `true` if the bank declined the authentication,
 *                    `false` if an error interrupted it.
 * @property challengeShown Whether a challenge page was announced in this attempt (keeps the sheet
 *                          expanded while finalizing; a decoupled flow stays compact).
 */
data class Standalone3DSFlowState(
    val phase: Standalone3DSPhase,
    val decoupledDescription: String? = null,
    val declined: Boolean = false,
    val challengeShown: Boolean = false
)

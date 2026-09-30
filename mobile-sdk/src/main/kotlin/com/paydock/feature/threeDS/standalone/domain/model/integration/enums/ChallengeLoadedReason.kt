package com.paydock.feature.threeDS.standalone.domain.model.integration.enums

/**
 * Why a Standalone 3DS challenge page was reported as loaded.
 *
 * @see com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSProgress.ChallengeLoaded
 */
enum class ChallengeLoadedReason {
    /**
     * The challenge page finished loading.
     */
    LOAD,

    /**
     * No load was observed within the safety timeout; the challenge is reported as loaded anyway
     * so a loading indicator is never held forever.
     */
    TIMEOUT,

    /**
     * The reason was missing or not recognised.
     */
    UNKNOWN;

    internal companion object {
        /**
         * Maps the raw value sent by the 3DS flow (e.g. `load`, `timeout`) to a [ChallengeLoadedReason].
         *
         * @param value The raw reason value.
         * @return The matching reason, or [UNKNOWN] if the value is missing or unrecognised.
         */
        fun fromValue(value: String?): ChallengeLoadedReason =
            entries.find { it != UNKNOWN && it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

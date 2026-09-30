package com.paydock.feature.threeDS.standalone.domain.model.integration.enums

/**
 * How the completion of a Standalone 3DS challenge (or decoupled authentication) was detected.
 *
 * @see com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSProgress.ChallengeCompleted
 */
enum class ChallengeCompletedSource {
    /**
     * Detected by polling the 3DS server for the authentication result (the usual case).
     */
    POLL,

    /**
     * Detected by the callback carrying the authentication outcome.
     */
    CALLBACK,

    /**
     * The source was missing or not recognised.
     */
    UNKNOWN;

    internal companion object {
        /**
         * Maps the raw value sent by the 3DS flow (e.g. `poll`, `callback`) to a [ChallengeCompletedSource].
         *
         * @param value The raw source value.
         * @return The matching source, or [UNKNOWN] if the value is missing or unrecognised.
         */
        fun fromValue(value: String?): ChallengeCompletedSource =
            entries.find { it != UNKNOWN && it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

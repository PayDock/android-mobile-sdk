package com.paydock.core

/**
 * Constants related to the Client SDK configuration.
 */
internal object ClientSDKConstants {

    /**
     * HTML Widget ID used to load Client SDK widgets into.
     */
    internal const val WIDGET_CONTAINER_ID = "widgetContainer"
    internal const val WIDGET_ID = "paydock"

    /**
     * The Client SDK version line (`vX.Y`) this Mobile SDK release supports. Without a pin, widgets load this line's
     * stable alias in production and sandbox, so sandbox runs exactly what production runs, and its candidate alias in
     * staging; a merchant may pin one exact release of the line with `MobileSDK.Builder.clientSdkVersion`. The release alias check
     * (`scripts/check-client-sdk-aliases.sh`) reads the line from this declaration.
     */
    internal const val SUPPORTED_LINE = "v1.150"

    /** The supported line's stable alias: the production and sandbox default. */
    internal const val STABLE_VERSION = "$SUPPORTED_LINE.stable"

    /** The supported line's candidate alias: the staging default. */
    internal const val CANDIDATE_VERSION = "$SUPPORTED_LINE.candidate"

    /**
     * An exact Client SDK release (`vX.Y.Z`): plain releases only, so `-beta` and any other suffix never match.
     * Groups 1 and 2 are the major and minor numbers, which together name the release's line.
     */
    internal val VERSION_PATTERN = Regex("""^v(\d+)\.(\d+)\.(\d+)$""")

    /**
     * Address of the Client SDK JavaScript library on the CDN.
     *
     * @param version An exact release (`vX.Y.Z`) or an alias of a line (`vX.Y.stable`, `vX.Y.candidate`).
     * @return The script address, `https://widget.paydock.com/sdk/<version>/widget.umd.min.js`.
     */
    internal fun libraryUrl(version: String): String = "https://widget.paydock.com/sdk/$version/widget.umd.min.js"

    /**
     * Constants for different environment configurations.
     */
    internal object Env {
        const val SANDBOX = "sandbox"
        const val PROD = "production"
        const val STAGING = "staging"
        const val STAGING_1 = "staging_1"
        const val STAGING_2 = "staging_2"
        const val STAGING_3 = "staging_3"
        const val STAGING_4 = "staging_4"
        const val STAGING_5 = "staging_5"
        const val STAGING_6 = "staging_6"
        const val STAGING_7 = "staging_7"
        const val STAGING_8 = "staging_8"
        const val STAGING_9 = "staging_9"
        const val STAGING_10 = "staging_10"
        const val STAGING_11 = "staging_11"
        const val STAGING_12 = "staging_12"
        const val STAGING_13 = "staging_13"
        const val STAGING_14 = "staging_14"
        const val STAGING_15 = "staging_15"
    }
}

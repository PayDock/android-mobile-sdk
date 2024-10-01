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
     * URL of the JavaScript library for the Client SDK.
     */
    object Library {
        internal const val PROD =
            "https://widget.paydock.com/sdk/v1.108.0/widget.umd.min.js"
        internal const val STAGING_SANDBOX =
            "https://widget.paydock.com/sdk/v1.108.0-beta/widget.umd.min.js"
    }

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

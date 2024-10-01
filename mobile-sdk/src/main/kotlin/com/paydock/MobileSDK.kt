package com.paydock

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.paydock.core.data.injection.MobileSDKKoinContext
import com.paydock.core.domain.mapper.mapToBaseUrl
import com.paydock.core.domain.model.Environment

/**
 * The main entry point for the Mobile SDK.
 *
 * This class represents the Mobile SDK and serves as the central access point for interacting with the SDK's features.
 *
 * @param context The Android application context used for initializing the Mobile SDK and its dependencies.
 * @param environment The environment for the Mobile SDK, which determines the API endpoint to use (e.g., production, sandbox, staging).
 * @param enableTestMode Flag to enable test mode. This is only allowed in non-production environments.
 * @param initialSdkTheme The initial theme to be applied across the Mobile SDK (colors, dimensions, and font).
 */
class MobileSDK(
    context: Context,
    val environment: Environment,
    internal val enableTestMode: Boolean,
    initialSdkTheme: MobileSDKTheme
) {

    // Private mutable state variable for the theme
    private var _sdkTheme by mutableStateOf(initialSdkTheme)

    // Expose a read-only state variable for external use
    val sdkTheme: MobileSDKTheme
        get() = _sdkTheme

    /**
     * Updates the SDK theme internally.
     *
     * @param newTheme The new theme to be applied to the Mobile SDK.
     */
    private fun updateSdkTheme(newTheme: MobileSDKTheme) {
        _sdkTheme = newTheme
        // Logic to update any necessary components when the theme changes
    }

    /**
     * Updates the SDK theme externally.
     *
     * @param newTheme The new theme to be applied to the Mobile SDK.
     */
    fun updateTheme(newTheme: MobileSDKTheme) {
        updateSdkTheme(newTheme)
    }

    internal var koinContext: MobileSDKKoinContext? = null

    internal val baseUrl: String

    init {
        // Initialize Koin context if not already initialized
        if (koinContext == null) {
            koinContext = MobileSDKKoinContext(context)
        }

        // Map environment to corresponding base URL
        baseUrl = environment.mapToBaseUrl()
    }

    /**
     * Builder class for constructing the MobileSDK instance.
     *
     * Allows for flexible configuration of environment, test mode, and theme.
     */
    class Builder {
        private var sdkTheme: MobileSDKTheme = MobileSDKTheme()
        private var environment: Environment = Environment.PRODUCTION
        private var enableTestMode: Boolean = false

        /**
         * Sets the environment for the SDK.
         *
         * @param environment The environment (production, sandbox, staging, etc.).
         */
        fun environment(environment: Environment) = apply { this.environment = environment }

        /**
         * Enables or disables test mode. This is only allowed for non-production environments.
         *
         * @param enable True to enable test mode, false to disable.
         */
        fun enableTestMode(enable: Boolean) = apply {
            if (environment != Environment.PRODUCTION) {
                this.enableTestMode = enable
            }
        }

        /**
         * Applies a custom theme to the Mobile SDK.
         *
         * @param theme The theme to be applied.
         */
        fun applyTheme(theme: MobileSDKTheme) = apply { this.sdkTheme = theme }

        /**
         * Builds and initializes the MobileSDK with the provided configuration.
         *
         * @param context The application context.
         * @return The initialized MobileSDK instance.
         */
        fun build(context: Context): MobileSDK {
            return initialize(context, environment, enableTestMode, sdkTheme)
        }
    }

    companion object {
        private var instance: MobileSDK? = null

        /**
         * Initializes the MobileSDK with the provided configuration.
         *
         * @param context The application context.
         * @param environment The environment to use (default: Environment.PRODUCTION).
         * @param sdkTheme The theme to be applied to SDK components.
         * @param enableTestMode Flag to enable test mode. This is only allowed in non-production environments.
         * @throws IllegalStateException if MobileSDK is already initialized.
         */
        @JvmStatic
        @Synchronized
        internal fun initialize(
            context: Context,
            environment: Environment = Environment.PRODUCTION,
            enableTestMode: Boolean = false,
            sdkTheme: MobileSDKTheme = MobileSDKTheme()
        ): MobileSDK {
            if (instance != null) {
                error(IllegalStateException("MobileSDK is already initialized."))
            }
            return MobileSDK(context, environment, enableTestMode, sdkTheme).also {
                instance = it
            }
        }

        /**
         * Checks if the MobileSDK has been initialized.
         *
         * @return True if initialized, false otherwise.
         */
        @JvmStatic
        fun isInitialised(): Boolean = instance != null

        /**
         * Gets the initialized MobileSDK instance.
         *
         * @return The MobileSDK instance.
         * @throws IllegalStateException if MobileSDK is not initialized.
         */
        @JvmStatic
        fun getInstance(): MobileSDK {
            return instance
                ?: error("MobileSDK not initialized. Call initialize() first.")
        }

        /**
         * Resets the MobileSDK instance. This is typically used for testing purposes.
         */
        @JvmStatic
        @VisibleForTesting
        internal fun reset() {
            instance = null
        }
    }
}

/**
 * Initializes the MobileSDK with the provided configuration using the application context.
 *
 * @param environment The environment to use (default: Environment.PRODUCTION).
 * @param enableTestMode Flag to enable test mode (default: false).
 * @param enableTestMode Flag to enable test mode. This is only allowed in non-production environments.
 * @param sdkTheme The theme to be applied to SDK components (default: MobileSDKTheme()).
 * @return The initialized MobileSDK instance.
 */
@Synchronized
fun Context.initializeMobileSDK(
    environment: Environment = Environment.PRODUCTION,
    enableTestMode: Boolean = false,
    sdkTheme: MobileSDKTheme = MobileSDKTheme()
): MobileSDK = MobileSDK.initialize(this, environment, enableTestMode, sdkTheme)

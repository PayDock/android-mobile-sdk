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
 * @param initialSdkTheme The initial theme to be applied across the Mobile SDK (colours, dimensions and font)
 */
class MobileSDK(
    context: Context,
    val environment: Environment,
    initialSdkTheme: MobileSDKTheme
) {

    // Private mutable state variable for the theme
    private var _sdkTheme by mutableStateOf(initialSdkTheme)

    // Expose a read-only state variable for external use
    val sdkTheme: MobileSDKTheme
        get() = _sdkTheme

    // Function to update the theme internally
    private fun updateSdkTheme(newTheme: MobileSDKTheme) {
        _sdkTheme = newTheme
        // Call any logic or updates you need to perform when the theme changes
        // For example, trigger recomposition of Composables using the theme
        // or apply theme-related logic here
    }

    // Public function to check if the MobileSDK has been initialised already
    fun isInitialised(): Boolean = instance != null

    // Public function to update the theme externally
    fun updateTheme(newTheme: MobileSDKTheme) {
        updateSdkTheme(newTheme)
    }

    internal var koinContext: MobileSDKKoinContext? = null

    internal val baseUrl: String

    init {
        // Check if the Koin application context for the Mobile SDK has been initialized.
        // If it is not initialized, create a new instance of MobileSDKKoinContext with the provided context.
        if (koinContext == null) {
            koinContext = MobileSDKKoinContext(context)
        }

        baseUrl = environment.mapToBaseUrl()
    }

    /**
     * Builder pattern for initializing the MobileSDK.
     *
     * @property environment The environment to use (default: Environment.PRODUCTION).
     */
    class Builder {
        private var sdkTheme: MobileSDKTheme = MobileSDKTheme()
        private var environment: Environment = Environment.PRODUCTION

        /**
         * Sets the environment for the SDK.
         */
        fun environment(environment: Environment) =
            apply { this.environment = environment }

        fun applyTheme(theme: MobileSDKTheme) = apply { this.sdkTheme = theme }

        /**
         * Builds and initializes the MobileSDK with the provided configuration.
         * @param context The application context.
         */
        fun build(context: Context): MobileSDK {
            return initialize(context, environment, sdkTheme)
        }
    }

    companion object {
        private var instance: MobileSDK? = null

        /**
         * Initializes the MobileSDK with the provided configuration.
         *
         * @param context The application context.
         * @param environment The environment to use (default: Environment.PRODUCTION).
         * @param sdkTheme The theme to be applied to SDK components
         * @throws IllegalStateException if MobileSDK is already initialized.
         */
        @JvmStatic
        @Synchronized
        internal fun initialize(
            context: Context,
            environment: Environment = Environment.PRODUCTION,
            sdkTheme: MobileSDKTheme = MobileSDKTheme()
        ): MobileSDK {
            if (instance != null) {
                error(IllegalStateException("MobileSDK is already initialized."))
            }
            return MobileSDK(context, environment, sdkTheme).let {
                instance = it
                it
            }
        }

        /**
         * Gets the instance of the MobileSDK.
         * @return The initialized MobileSDK instance.
         * @throws IllegalStateException if MobileSDK is not initialized. Call initialize() first.
         */
        @JvmStatic
        fun getInstance(): MobileSDK {
            return instance
                ?: error("MobileSDK not initialized. Call initialize() first.")
        }

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
 * @param sdkTheme The theme to be applied to SDK components
 */
@Synchronized
fun Context.initializeMobileSDK(
    environment: Environment = Environment.PRODUCTION,
    sdkTheme: MobileSDKTheme = MobileSDKTheme()
): MobileSDK = MobileSDK.initialize(this, environment, sdkTheme)
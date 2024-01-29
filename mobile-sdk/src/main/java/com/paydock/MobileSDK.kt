/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 4:15 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paydock

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.paydock.core.data.injection.MobileSDKKoinContext
import com.paydock.core.domain.model.Environment

/**
 * The main entry point for the Mobile SDK.
 *
 * This class represents the Mobile SDK and serves as the central access point for interacting with the SDK's features.
 *
 * @param context The Android application context used for initializing the Mobile SDK and its dependencies.
 * @param publicKey The public key used for authentication with the backend services.
 * @param environment The environment for the Mobile SDK, which determines the API endpoint to use (e.g., production, sandbox, staging).
 * @param initialSdkTheme The initial theme to be applied across the Mobile SDK (colours, dimensions and font)
 */
class MobileSDK(
    context: Context,
    internal val publicKey: String,
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

        // Determine the base URL based on the specified environment.
        baseUrl = when (environment) {
            // Use the production API endpoint for the PRODUCTION environment.
            Environment.PRODUCTION -> "api.paydock.com"

            // Use the sandbox API endpoint for the SANDBOX environment.
            Environment.SANDBOX -> "api-sandbox.paydock.com"

            // Use the staging API endpoint for the STAGING environment.
            Environment.STAGING -> "apista.paydock.com"
        }
    }

    /**
     * Builder pattern for initializing the MobileSDK.
     *
     * @param publicKey The public key to use for authentication.
     * @property environment The environment to use (default: Environment.PRODUCTION).
     */
    class Builder(private val publicKey: String) {
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
            return initialize(context, publicKey, environment, sdkTheme)
        }
    }

    companion object {
        private var instance: MobileSDK? = null

        /**
         * Initializes the MobileSDK with the provided configuration.
         * @param context The application context.
         * @param publicKey The public key to use for authentication.
         * @param environment The environment to use (default: Environment.PRODUCTION).
         * @param sdkTheme The theme to be applied to SDK components
         * @throws IllegalStateException if MobileSDK is already initialized.
         */
        @JvmStatic
        @Synchronized
        internal fun initialize(
            context: Context,
            publicKey: String,
            environment: Environment = Environment.PRODUCTION,
            sdkTheme: MobileSDKTheme = MobileSDKTheme()
        ): MobileSDK {
            if (instance != null) {
                error(IllegalStateException("MobileSDK is already initialized."))
            }
            return MobileSDK(context, publicKey, environment, sdkTheme).let {
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
 * @param publicKey The public key to use for authentication.
 * @param environment The environment to use (default: Environment.PRODUCTION).
 * @param sdkTheme The theme to be applied to SDK components
 */
@Synchronized
fun Context.initializeMobileSDK(
    publicKey: String,
    environment: Environment = Environment.PRODUCTION,
    sdkTheme: MobileSDKTheme = MobileSDKTheme()
): MobileSDK = MobileSDK.initialize(this, publicKey, environment, sdkTheme)
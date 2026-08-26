package com.paydock

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.paydock.binprocessor.data.refresh.BinDataRefreshCoordinator
import com.paydock.core.data.injection.MobileSDKKoinContext
import com.paydock.core.domain.mapper.mapToBaseUrl
import com.paydock.core.domain.model.Environment
import com.paydock.feature.afterpay.presentation.AfterpayBaseline
import java.util.Locale

/**
 * The main entry point for the Mobile SDK.
 *
 * This class represents the Mobile SDK and serves as the central access point for interacting with the SDK's features.
 *
 * @param context The Android application context used for initializing the Mobile SDK and its dependencies.
 * @param environment The environment for the Mobile SDK, which determines the API endpoint to use (e.g., production, sandbox, staging).
 * @param enableTestMode Flag to enable test mode. This is only allowed in non-production environments.
 */
class MobileSDK(
    context: Context,
    val environment: Environment,
    internal val enableTestMode: Boolean
) {

    internal var koinContext: MobileSDKKoinContext? = null

    internal val baseUrl: String

    init {
        // Map environment to corresponding base URL (needed before Koin resolves HttpClient)
        baseUrl = environment.mapToBaseUrl()
        // Register so getInstance() is available when Koin creates HttpClient/BinDataCacheManager
        registerInstance(this)
        // Initialize Koin context if not already initialized
        if (koinContext == null) {
            koinContext = MobileSDKKoinContext(context)
            koinContext?.koin?.get<BinDataRefreshCoordinator>()?.refreshAtSdkInit()
        }
    }

    /**
     * Builder class for constructing the MobileSDK instance.
     *
     * Allows for flexible configuration of environment, test mode, and theme.
     */
    class Builder {
        private var environment: Environment = Environment.PRODUCTION
        private var enableTestMode: Boolean = false
        private var afterpayLocale: Locale? = null

        /**
         * Sets the environment for the SDK.
         *
         * @param environment The environment (production, sandbox, staging, etc.).
         */
        fun environment(environment: Environment) = apply { this.environment = environment }

        /**
         * Sets the locale used to resolve the Afterpay payment button's branding (classic Afterpay
         * vs. Cash App Afterpay) at SDK initialisation. Useful for forcing a region — e.g. in
         * testing — without changing the device locale. When `null` (default), the device locale
         * is used.
         *
         * @param locale The Afterpay branding locale, or `null` to use the device locale.
         */
        fun afterpayLocale(locale: Locale?) = apply { this.afterpayLocale = locale }

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
         * Builds and initializes the MobileSDK with the provided configuration.
         *
         * @param context The application context.
         * @return The initialized MobileSDK instance.
         */
        fun build(context: Context): MobileSDK {
            return initialize(context, environment, enableTestMode, afterpayLocale)
        }
    }

    companion object {
        private var instance: MobileSDK? = null

        /** Called from init so getInstance() is available when Koin creates HttpClient (which needs baseUrl). */
        @JvmStatic
        internal fun registerInstance(sdk: MobileSDK) {
            instance = sdk
        }

        /**
         * Initializes the MobileSDK with the provided configuration.
         *
         * @param context The application context.
         * @param environment The environment to use (default: Environment.PRODUCTION).
         * @param enableTestMode Flag to enable test mode. This is only allowed in non-production environments.
         * @param afterpayLocale Locale used to resolve the Afterpay payment button's branding at
         *   initialisation. When `null` (default), the device locale is used.
         * @throws IllegalStateException if MobileSDK is already initialized.
         */
        @JvmStatic
        @Synchronized
        internal fun initialize(
            context: Context,
            environment: Environment = Environment.PRODUCTION,
            enableTestMode: Boolean = false,
            afterpayLocale: Locale? = null,
        ): MobileSDK {
            if (instance != null) {
                error(IllegalStateException("MobileSDK is already initialized."))
            }
            return MobileSDK(context, environment, enableTestMode).also {
                instance = it
                // Apply the Afterpay baseline configuration at SDK initialisation — before any UI
                // builds an Afterpay appearance (e.g. an app pre-building defaults at startup).
                // Afterpay's AfterpayPaymentButton.ButtonText enum captures its brand artwork on
                // first class-load from the configured locale, so configuring here ensures the
                // intended branding is locked in before that first reference. When afterpayLocale
                // is null, AfterpayBaseline falls back to the device locale. Safe no-op for apps
                // that never use Afterpay. See [AfterpayBaseline].
                AfterpayBaseline.ensureConfigured(afterpayLocale)
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
 * @param enableTestMode Flag to enable test mode (default: false). This is only allowed in non-production environments.
 * @param afterpayLocale Locale used to resolve the Afterpay payment button's branding at
 *   initialisation. When `null` (default), the device locale is used.
 * @return The initialized MobileSDK instance.
 */
@Synchronized
fun Context.initializeMobileSDK(
    environment: Environment = Environment.PRODUCTION,
    enableTestMode: Boolean = false,
    afterpayLocale: Locale? = null
): MobileSDK = MobileSDK.initialize(this, environment, enableTestMode, afterpayLocale)

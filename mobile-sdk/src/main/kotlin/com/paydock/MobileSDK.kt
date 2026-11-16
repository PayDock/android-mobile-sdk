package com.paydock

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.paydock.binprocessor.data.refresh.BinDataRefreshCoordinator
import com.paydock.core.ClientSDKConstants
import com.paydock.core.data.injection.MobileSDKKoinContext
import com.paydock.core.domain.error.exceptions.MobileSdkException
import com.paydock.core.domain.mapper.mapToBaseUrl
import com.paydock.core.domain.model.Environment
import com.paydock.core.telemetry.bindTelemetry
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
 * @param clientSdkVersion The exact Client SDK release (`vX.Y.Z` of the supported line) every widget loads, in every
 *   environment, or `null` (default) to load the supported line's stable alias in every environment.
 *   [Builder.clientSdkVersion] validates it.
 */
class MobileSDK @JvmOverloads constructor(
    context: Context,
    val environment: Environment,
    internal val enableTestMode: Boolean,
    val clientSdkVersion: String? = null,
) {

    internal var koinContext: MobileSDKKoinContext? = null

    internal val baseUrl: String

    /**
     * The Client SDK version the most recent widget page reported after loading (e.g. `"v1.150.2"`). Widgets load
     * an alias of the supported line unless pinned, so this names the release that actually ran. `null` until a
     * widget page has reported a version, e.g. when the loaded release does not report one.
     */
    @Volatile
    var loadedClientSdkVersion: String? = null
        internal set

    init {
        // Map environment to corresponding base URL (needed before Koin resolves HttpClient)
        baseUrl = environment.mapToBaseUrl()
        // Register so getInstance() is available when Koin creates HttpClient/BinDataCacheManager
        registerInstance(this)
        // Initialize Koin context if not already initialized
        if (koinContext == null) {
            koinContext = MobileSDKKoinContext(context)
            // Bind telemetry before any init-time background work, so Koin already resolves Telemetry there.
            koinContext?.koin?.let { bindTelemetry(it, environment, context) }
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
        private var clientSdkVersion: String? = null

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
         * Pins the exact Client SDK release every widget loads, in every environment, instead of the supported line's
         * stable alias. It must be a plain release (`vX.Y.Z`, no `-beta` or other suffix) of the line this Mobile SDK
         * supports; [build] refuses anything else. When `null` (default), widgets load the stable alias.
         *
         * @param version The exact Client SDK release, e.g. `"v1.150.1"`, or `null` for the stable alias.
         */
        fun clientSdkVersion(version: String?) = apply { this.clientSdkVersion = version }

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
         * @throws MobileSdkException.InvalidConfiguration if the [clientSdkVersion] pin is not a plain release
         *   (`vX.Y.Z`) of the supported Client SDK line; the message names the line. The SDK is not initialised.
         */
        fun build(context: Context): MobileSDK {
            clientSdkVersion?.let(::requireSupportedClientSdkVersion)
            return initialize(context, environment, enableTestMode, afterpayLocale, clientSdkVersion)
        }

        /**
         * Refuses [version] unless it matches [ClientSDKConstants.VERSION_PATTERN] and belongs to
         * [ClientSDKConstants.SUPPORTED_LINE], in that order.
         */
        private fun requireSupportedClientSdkVersion(version: String) {
            val line = ClientSDKConstants.SUPPORTED_LINE
            val match = ClientSDKConstants.VERSION_PATTERN.matchEntire(version)
                ?: throw MobileSdkException.InvalidConfiguration(
                    "clientSdkVersion $version is not a release (vX.Y.Z) of supported line $line"
                )
            val (major, minor) = match.destructured
            if ("v$major.$minor" != line) {
                throw MobileSdkException.InvalidConfiguration(
                    "clientSdkVersion $version is outside supported line $line"
                )
            }
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
         * @param clientSdkVersion The exact Client SDK release widgets load, already validated by [Builder.build],
         *   or `null` (default) for the environment's alias of the supported line.
         * @throws IllegalStateException if MobileSDK is already initialized.
         */
        @JvmStatic
        @Synchronized
        internal fun initialize(
            context: Context,
            environment: Environment = Environment.PRODUCTION,
            enableTestMode: Boolean = false,
            afterpayLocale: Locale? = null,
            clientSdkVersion: String? = null,
        ): MobileSDK {
            if (instance != null) {
                error(IllegalStateException("MobileSDK is already initialized."))
            }
            return MobileSDK(context, environment, enableTestMode, clientSdkVersion).also {
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

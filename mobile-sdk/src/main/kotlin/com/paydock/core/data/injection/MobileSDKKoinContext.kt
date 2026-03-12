package com.paydock.core.data.injection

import android.content.Context
import com.paydock.BuildConfig
import com.paydock.binprocessor.data.refresh.BinDataRefreshCoordinator
import com.paydock.core.injection.sdkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.loadKoinModules

/**
 * A helper class for managing the Koin application context associated with the Mobile SDK.
 *
 * If Koin was already started (e.g. by tests with mock modules), uses the existing instance.
 * This allows tests to load mock modules before [initializeMobileSDK] so that
 * [BinDataRefreshCoordinator] and [BinDataCacheManager] resolve with mocked dependencies.
 *
 * @property appContext The Android application context used for initializing the Koin application.
 */
internal class MobileSDKKoinContext(private val appContext: Context) {

    /**
     * The Koin instance associated with the Mobile SDK.
     * This property will be initialized as soon as the class is constructed.
     */
    var koin: Koin
        private set

    init {
        koin = try {
            // Use existing Koin if already started (e.g. by host app or tests with mock modules).
            // If our definitions are missing (e.g. host app started Koin), load sdkModule.
            // Skip load when test already registered our modules with mocks to avoid overriding them.
            val existingKoin = GlobalContext.get()
            if (existingKoin.getOrNull<BinDataRefreshCoordinator>() == null) {
                loadKoinModules(sdkModule)
            }
            existingKoin
        } catch (_: IllegalStateException) {
            // Koin not started yet, start it with SDK modules
            startKoin {
                androidContext(appContext)
                if (BuildConfig.DEBUG) androidLogger()
                modules(sdkModule)
            }.koin
        }
    }

    companion object {
        @Volatile
        private var instance: MobileSDKKoinContext? = null

        /**
         * Initializes the MobileSDKKoinContext with the given application context.
         * This should be called once when the application starts.
         *
         * @param context The application context.
         */
        @Synchronized
        fun init(context: Context) {
            if (instance == null) {
                instance = MobileSDKKoinContext(context)
            }
        }
    }
}
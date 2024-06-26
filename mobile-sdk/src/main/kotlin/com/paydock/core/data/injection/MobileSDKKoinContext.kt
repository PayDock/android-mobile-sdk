package com.paydock.core.data.injection

import android.content.Context
import com.paydock.BuildConfig
import com.paydock.core.data.injection.modules.sdkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.Koin
import org.koin.core.context.GlobalContext.startKoin

/**
 * A helper class for managing the Koin application context associated with the Mobile SDK.
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
        // Start Koin here
        koin = startKoin {
            androidContext(appContext)
            if (BuildConfig.DEBUG) androidLogger()
            modules(sdkModule)
        }.koin
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
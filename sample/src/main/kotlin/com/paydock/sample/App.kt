package com.paydock.sample

import android.app.Application
import com.paydock.MobileSDK
import com.paydock.sample.debug.DebugScreenRegistry
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Debug builds only (the release registry does nothing): runs before the SDK is built.
        DebugScreenRegistry.onAppCreate()
        MobileSDK.Builder()
            .environment(BuildConfig.SDK_ENVIRONMENT)
            // Set flag for non-production builds
            .enableTestMode(BuildConfig.ENABLE_TEST_MODE)
            // Force the Afterpay button branding to en-AU (classic Afterpay) regardless of the
            // device region. Omit this (or pass null) to follow the device locale instead.
            .afterpayLocale(Locale("en", "AU"))
            .build(this)
    }
}

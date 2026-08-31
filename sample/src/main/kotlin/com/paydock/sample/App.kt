package com.paydock.sample

import android.app.Application
import com.paydock.MobileSDK
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
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

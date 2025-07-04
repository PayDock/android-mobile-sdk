package com.paydock.sample

import android.app.Application
import com.paydock.MobileSDK
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileSDK.Builder()
            .environment(BuildConfig.SDK_ENVIRONMENT)
            // Set flag for non-production builds
            .enableTestMode(BuildConfig.ENABLE_TEST_MODE)
            .build(this)
    }
}

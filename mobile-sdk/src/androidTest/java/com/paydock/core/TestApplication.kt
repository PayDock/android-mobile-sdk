package com.paydock.core

import android.app.Application
import com.paydock.MobileSDK
import com.paydock.core.domain.model.Environment

internal class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileSDK
            .Builder()
            .environment(Environment.SANDBOX)
            .build(this)
    }
}
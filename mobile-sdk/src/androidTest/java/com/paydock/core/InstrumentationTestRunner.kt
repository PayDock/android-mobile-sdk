package com.paydock.core

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

internal class InstrumentationTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        classLoader: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(classLoader, TestApplication::class.java.name, context)
    }
}
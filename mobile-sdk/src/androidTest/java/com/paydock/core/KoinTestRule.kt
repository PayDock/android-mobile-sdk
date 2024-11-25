package com.paydock.core

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.core.module.Module

/**
 * private val instrumentedTestModule = module {
 *     factory<Something> { FakeSomething() }
 * }
 *
 * @get:Rule
 * val koinTestRule = KoinTestRule(
 *     modules = listOf(productionModule, instrumentedTestModule)
 * )
 */
internal class KoinTestRule(
    private val modules: List<Module>
) : TestWatcher() {
    override fun starting(description: Description) {
        startKoin {
            androidContext(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext)
            modules(modules)
        }
    }

    override fun finished(description: Description) {
        stopKoin()
    }
}
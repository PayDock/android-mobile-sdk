package com.paydock.core.injection

import android.content.Context
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.utils.TestDispatchersProviderImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.mockito.Mockito
import kotlin.coroutines.CoroutineContext

internal val testSdkModule = module {
    single { Mockito.mock(Context::class.java) }

    singleOf(::TestCoroutineScheduler)
    factory<CoroutineDispatcher> { StandardTestDispatcher(scheduler = get()) }
    factory<CoroutineContext> { StandardTestDispatcher(scheduler = get()) }
    single { TestScope(context = get()) }

    single<DispatchersProvider> { TestDispatchersProviderImpl }
}
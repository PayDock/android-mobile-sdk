package com.paydock.core.data.injection.modules

import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.data.util.DispatchersProviderImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module for providing dispatchers used for coroutines.
 * Provides different implementations of [CoroutineDispatcher] for various use cases.
 */
val dispatchersModule = module {
    /**
     * Provides a singleton instance of [DispatchersProviderImpl].
     * This implementation of [DispatchersProvider] provides custom dispatchers
     * for the application.
     */
    single<DispatchersProvider> { DispatchersProviderImpl }

    /**
     * Alternative way of providing the Main dispatcher using a named qualifier.
     * Provides a singleton instance of [Dispatchers.Main] with the name "Main".
     */
    single(named("Main")) { Dispatchers.Main }

    /**
     * Provides a singleton instance of [Dispatchers.Main.immediate] with the name "MainImmediate".
     * This dispatcher is used for immediate execution on the main thread, bypassing any scheduling.
     */
    single(named("MainImmediate")) { Dispatchers.Main.immediate }

    /**
     * Provides a singleton instance of [Dispatchers.IO] with the name "IO".
     * This dispatcher is optimized for IO-bound tasks, such as network or disk operations.
     */
    single(named("IO")) { Dispatchers.IO }

    /**
     * Provides a singleton instance of [Dispatchers.Default] with the name "Default".
     * This dispatcher is used for CPU-intensive tasks that don't block the main or IO threads.
     */
    single(named("Default")) { Dispatchers.Default }
}

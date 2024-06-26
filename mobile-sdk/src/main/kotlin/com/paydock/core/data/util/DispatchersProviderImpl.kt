package com.paydock.core.data.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Implementation of [DispatchersProvider] providing standard [CoroutineDispatcher] instances.
 */
object DispatchersProviderImpl : DispatchersProvider {

    /**
     * Returns the main thread dispatcher.
     *
     * @return The main thread [CoroutineDispatcher].
     */
    override fun getMain(): CoroutineDispatcher = Dispatchers.Main

    /**
     * Returns the main thread immediate dispatcher.
     *
     * @return The main thread immediate [CoroutineDispatcher].
     */
    override fun getMainImmediate(): CoroutineDispatcher = Dispatchers.Main.immediate

    /**
     * Returns the IO dispatcher.
     *
     * @return The IO [CoroutineDispatcher].
     */
    override fun getIO(): CoroutineDispatcher = Dispatchers.IO

    /**
     * Returns the default dispatcher.
     *
     * @return The default [CoroutineDispatcher].
     */
    override fun getDefault(): CoroutineDispatcher = Dispatchers.Default
}

package com.paydock.core.data.util

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Interface for providing different [CoroutineDispatcher] instances used for coroutines.
 * Defines functions to obtain dispatchers for IO, Main, MainImmediate, and Default threads.
 */
internal interface DispatchersProvider {
    /**
     * Returns the [CoroutineDispatcher] for IO-bound tasks.
     *
     * @return The [CoroutineDispatcher] for IO-bound tasks.
     */
    fun getIO(): CoroutineDispatcher

    /**
     * Returns the [CoroutineDispatcher] for the Main thread.
     *
     * @return The [CoroutineDispatcher] for the Main thread.
     */
    fun getMain(): CoroutineDispatcher

    /**
     * Returns the [CoroutineDispatcher] for the Main thread with immediate execution.
     *
     * @return The [CoroutineDispatcher] for the Main thread with immediate execution.
     */
    fun getMainImmediate(): CoroutineDispatcher

    /**
     * Returns the default [CoroutineDispatcher] for CPU-intensive tasks.
     *
     * @return The default [CoroutineDispatcher] for CPU-intensive tasks.
     */
    fun getDefault(): CoroutineDispatcher
}
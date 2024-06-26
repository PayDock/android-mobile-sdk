package com.paydock.core.presentation.ui

import android.os.Handler
import android.os.MessageQueue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paydock.core.data.util.DispatchersProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.android.HandlerDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Base class for ViewModels with coroutine support.
 *
 * @param dispatchers Provider for coroutine dispatchers (e.g., IO, Main, Main Immediate).
 */
abstract class BaseViewModel(
    dispatchers: DispatchersProvider
) : ViewModel() {

    // Coroutine dispatcher for IO operations
    private val io = dispatchers.getIO()

    /**
     * Use main if you need to perform a UI operation ASAP (as soon as possible) but not immediately; otherwise, use [mainImmediate].
     *
     * The Main dispatcher uses a [Handler] to post a [Runnable] to the [MessageQueue]. In other words, the operation or message
     * will be queued in the message queue and executed in the UI thread once the main looper dequeues it and reads it.
     *
     * Note: If the main dispatcher is called from the main thread, the operation will be performed immediately;
     * otherwise, it will be added to the queue as stated previously.
     *
     * Under the hood, coroutine checks If dispatch is required if [CoroutineDispatcher.isDispatchNeeded] is true it will call
     * the [CoroutineDispatcher.dispatch] method, which will result in posting a message to the handler queue. If dispatch isn't
     * required (if we're already in the UI thread or if we're using mainImmediate), it will immediately resume in the current thread.
     *
     * @see [MainCoroutineDispatcher], [HandlerDispatcher], [CoroutineDispatcher].
     * **/
    private val main = dispatchers.getMain()

    /**
     * Coroutine dispatcher for main thread operations with immediate dispatch behavior.
     *
     * Use mainImmediate if you need to perform an immediate operation in the UI; otherwise, use [main].
     *
     * @see [MainCoroutineDispatcher], [HandlerDispatcher], [CoroutineDispatcher].
     */
    private val mainImmediate = dispatchers.getMainImmediate()

    /**
     * Launches a coroutine on the IO dispatcher and returns the resulting [Job].
     */
    protected fun launchOnIO(block: suspend CoroutineScope.() -> Unit): Job =
        viewModelScope.launchOnIO(block)

    /**
     * Launches a coroutine on the main dispatcher and returns the resulting [Job].
     */
    protected fun launchOnMain(block: suspend CoroutineScope.() -> Unit): Job =
        viewModelScope.launchOnMain(block)

    /**
     * Launches a coroutine on the main immediate dispatcher and returns the resulting [Job].
     */
    protected fun launchOnMainImmediate(block: suspend CoroutineScope.() -> Unit): Job =
        viewModelScope.launchOnMainImmediate(block)

    /**
     * Extension function to launch a coroutine on the IO dispatcher and return the resulting [Job].
     */
    protected fun CoroutineScope.launchOnIO(block: suspend CoroutineScope.() -> Unit): Job =
        launch(io, block = block)

    /**
     * Extension function to launch a coroutine on the main dispatcher and return the resulting [Job].
     */
    protected fun CoroutineScope.launchOnMain(block: suspend CoroutineScope.() -> Unit): Job =
        launch(main, block = block)

    /**
     * Extension function to launch a coroutine on the main immediate dispatcher and return the resulting [Job].
     */
    protected fun CoroutineScope.launchOnMainImmediate(block: suspend CoroutineScope.() -> Unit): Job =
        launch(mainImmediate, block = block)

    /**
     * Suspends the coroutine until the provided block is executed on the IO dispatcher, and returns the result.
     */
    protected suspend fun <T> withContextIO(block: suspend CoroutineScope.() -> T): T =
        withContext(io, block)

    /**
     * Suspends the coroutine until the provided block is executed on the main dispatcher, and returns the result.
     */
    protected suspend fun <T> withContextMain(block: suspend CoroutineScope.() -> T): T =
        withContext(main, block)

    /**
     * Suspends the coroutine until the provided block is executed on the main immediate dispatcher, and returns the result.
     */
    protected suspend fun <T> withContextMainImmediate(block: suspend CoroutineScope.() -> T): T =
        withContext(mainImmediate, block)
}

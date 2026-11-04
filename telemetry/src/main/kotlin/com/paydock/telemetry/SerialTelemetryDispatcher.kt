package com.paydock.telemetry

import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

/**
 * The one telemetry worker (R_02): a single daemon thread named [THREAD_NAME], created by the executor on the first
 * task, never at construction. [post] drops a task instead of blocking when [capacity] tasks are already waiting.
 * Every task is wrapped, so a failing task never kills the worker, and neither method ever throws.
 */
internal class SerialTelemetryDispatcher(private val capacity: Int = DEFAULT_CAPACITY) : TelemetryDispatcher {

    private val executor = ScheduledThreadPoolExecutor(
        1,
        ThreadFactory { runnable -> Thread(runnable, THREAD_NAME).apply { isDaemon = true } }
    )

    override fun post(task: () -> Unit): Boolean = try {
        if (executor.queue.size >= capacity) {
            false
        } else {
            executor.execute(guarded(task))
            true
        }
    } catch (ignored: Throwable) {
        false
    }

    override fun postDelayed(delayMillis: Long, task: () -> Unit): Boolean = try {
        executor.schedule(guarded(task), delayMillis, TimeUnit.MILLISECONDS)
        true
    } catch (ignored: Throwable) {
        false
    }

    private fun guarded(task: () -> Unit): Runnable = Runnable {
        try {
            task()
        } catch (ignored: Throwable) {
            // A failing task never stops the worker.
        }
    }

    companion object {
        const val DEFAULT_CAPACITY = 1_000
        const val THREAD_NAME = "paydock-telemetry"
    }
}

package com.paydock.telemetry

/**
 * The process's link in the default uncaught-exception chain (spec § 7, D9, principle 5). Records with [recorder],
 * catching every Throwable, then in `finally` calls [previous] with the same thread and throwable; with no previous
 * handler, [fallback] (the JVM's own: print the stack trace to standard error). Re-entry guard (review fix): when it is
 * entered again on a thread it is already running on (a chain that loops back to it), it returns at once, recording
 * and forwarding nothing, so a cycle ends after one record.
 */
internal class ChainedCrashHandler(
    private val previous: Thread.UncaughtExceptionHandler?,
    @Volatile var recorder: CrashRecorder,
    private val fallback: (Thread, Throwable) -> Unit = { _, throwable -> throwable.printStackTrace() },
) : Thread.UncaughtExceptionHandler {

    /** Set on a thread while this handler runs on it: the re-entry guard. */
    private val running = ThreadLocal<Boolean>()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        if (running.get() == true) return // a chain that looped back: this pass already records and forwards
        running.set(true)
        try {
            recordAndForward(thread, throwable)
        } finally {
            running.remove()
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException") // recording must never stop the chain
    private fun recordAndForward(thread: Thread, throwable: Throwable) {
        try {
            recorder.record(thread, throwable)
        } catch (ignored: Throwable) {
            // An unrecorded crash: the previous handler still runs below.
        } finally {
            val next = previous
            if (next != null) next.uncaughtException(thread, throwable) else fallback(thread, throwable)
        }
    }
}

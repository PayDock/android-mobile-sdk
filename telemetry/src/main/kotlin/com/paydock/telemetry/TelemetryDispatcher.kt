package com.paydock.telemetry

/** One serial background worker. Never blocks the caller, never throws. */
internal interface TelemetryDispatcher {
    fun post(task: () -> Unit): Boolean // false when the task was dropped
    fun postDelayed(delayMillis: Long, task: () -> Unit): Boolean
}

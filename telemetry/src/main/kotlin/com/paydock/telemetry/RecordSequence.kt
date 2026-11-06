package com.paydock.telemetry

import java.util.concurrent.atomic.AtomicLong

/**
 * One per session, and the only source of record sequences: 1, 2, 3, … Thread-safe, so a crash handler can take an
 * ID off the worker without two records ever sharing one.
 */
internal class RecordSequence {
    private val last = AtomicLong(0)

    fun next(): Long = last.incrementAndGet()
}

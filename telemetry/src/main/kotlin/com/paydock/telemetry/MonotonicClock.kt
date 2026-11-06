package com.paydock.telemetry

/** A monotonic time in milliseconds for windows and retry times; unlike [Clock], a wall-clock jump never moves it. */
internal fun interface MonotonicClock {
    fun elapsed(): Long // ms since an arbitrary origin; only differences mean anything

    companion object {
        private const val NANOS_PER_MILLI = 1_000_000L

        val SYSTEM: MonotonicClock = MonotonicClock { System.nanoTime() / NANOS_PER_MILLI }
    }
}

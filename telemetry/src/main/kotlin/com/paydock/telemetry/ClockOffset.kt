package com.paydock.telemetry

/**
 * The device clock's offset from the gateway's clock (D20, spec § 6): server time minus device time, measured from
 * each response's `Date` header. 0 until the first measurement; one per runtime, shared by its transport and its
 * export runner, never persisted. Thread-safe.
 */
internal class ClockOffset(private val clock: Clock) {
    @Volatile
    var millis: Long = 0L
        private set

    /**
     * [serverDateMillis]: the response's `Date`, UTC epoch ms; read against the device clock now. A date outside
     * [MIN_SERVER_DATE_MILLIS, MAX_SERVER_DATE_MILLIS) is ignored and keeps the current offset (review fix: a lenient
     * parser accepts year 9999, whose offset would overflow every timestamp).
     */
    fun measure(serverDateMillis: Long) {
        if (serverDateMillis !in MIN_SERVER_DATE_MILLIS until MAX_SERVER_DATE_MILLIS) return
        millis = serverDateMillis - clock.now()
    }

    companion object {
        const val MIN_SERVER_DATE_MILLIS = 1_577_836_800_000L // 2020-01-01T00:00:00Z
        const val MAX_SERVER_DATE_MILLIS = 4_102_444_800_000L // 2100-01-01T00:00:00Z
    }
}

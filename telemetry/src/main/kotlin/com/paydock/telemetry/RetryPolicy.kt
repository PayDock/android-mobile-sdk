package com.paydock.telemetry

import kotlin.random.Random

/**
 * The back-off of a failed export (spec § 8): `d = min(max, base × 2^(n−1))`, jittered to `[d/2, d]`, and never
 * shorter than the server's `Retry-After`. Limits come from [TelemetryLimits].
 */
internal class RetryPolicy(private val random: Random = Random.Default) {

    fun delayMillis(failedAttempts: Int, retryAfterMillis: Long?): Long {
        val exponent = (failedAttempts - 1).coerceIn(0, MAX_EXPONENT)
        val delay = minOf(TelemetryLimits.RETRY_MAX_DELAY_MILLIS, TelemetryLimits.RETRY_BASE_DELAY_MILLIS shl exponent)
        val half = delay / 2
        return maxOf(half + random.nextLong(half + 1), retryAfterMillis ?: 0L)
    }

    private companion object {
        /** Far past the cap already, and far from a shift overflow. */
        const val MAX_EXPONENT = 20
    }
}

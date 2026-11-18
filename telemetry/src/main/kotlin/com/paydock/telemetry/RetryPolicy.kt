package com.paydock.telemetry

import kotlin.random.Random

/**
 * The back-off of a failed export (spec § 8): `d = min(max, base × 2^(n−1))`, jittered to `[d/2, d]`, and never
 * shorter than the server's `Retry-After` clamped to `[0, 1 h]`: `max(jitter(d), min(max(retryAfter, 0), 1 h))`.
 * A longer `Retry-After` waits [TelemetryLimits.RETRY_AFTER_MAX_MILLIS], so a bad answer can neither overflow the
 * runner's next attempt time nor stop sending for the process. Limits come from [TelemetryLimits].
 */
internal class RetryPolicy(private val random: Random = Random.Default) {

    fun delayMillis(failedAttempts: Int, retryAfterMillis: Long?): Long {
        val exponent = (failedAttempts - 1).coerceIn(0, MAX_EXPONENT)
        val delay = minOf(TelemetryLimits.RETRY_MAX_DELAY_MILLIS, TelemetryLimits.RETRY_BASE_DELAY_MILLIS shl exponent)
        val half = delay / 2
        val retryAfter = (retryAfterMillis ?: 0L).coerceIn(0L, TelemetryLimits.RETRY_AFTER_MAX_MILLIS)
        return maxOf(half + random.nextLong(half + 1), retryAfter)
    }

    private companion object {
        /** Far past the cap already, and far from a shift overflow. */
        const val MAX_EXPONENT = 20
    }
}

package com.paydock.telemetry

import com.paydock.telemetry.TelemetryLimits.RETRY_AFTER_MAX_MILLIS
import com.paydock.telemetry.TelemetryLimits.RETRY_BASE_DELAY_MILLIS
import com.paydock.telemetry.TelemetryLimits.RETRY_MAX_DELAY_MILLIS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Unit tests for [RetryPolicy]: exponential back-off with jitter, capped, never below `Retry-After`. */
internal class RetryPolicyTest {

    @Test
    fun delay_isJitteredWithinHalfToFullOfTheExponentialStep_cappedAtTheMaximum() {
        val policy = RetryPolicy(Random(7))
        val steps = listOf(5_000L, 10_000L, 20_000L, 40_000L, 60_000L, 60_000L, 60_000L)

        steps.forEachIndexed { index, step ->
            repeat(50) {
                val delay = policy.delayMillis(index + 1, null)
                assertTrue("attempt ${index + 1}: $delay", delay in step / 2..step)
            }
        }
        assertEquals(RETRY_BASE_DELAY_MILLIS, steps.first())
        assertEquals(RETRY_MAX_DELAY_MILLIS, steps.last())
        assertTrue(RetryPolicy(Random(7)).delayMillis(Int.MAX_VALUE, null) <= RETRY_MAX_DELAY_MILLIS)
    }

    @Test
    fun delay_isNeverShorterThanRetryAfter() {
        val policy = RetryPolicy(Random(7))

        assertEquals(7_000L, policy.delayMillis(1, 7_000L))
        assertEquals(120_000L, policy.delayMillis(5, 120_000L))
        assertTrue(policy.delayMillis(4, 0L) >= 20_000L)
    }

    /** MD-47 R_03: a `Retry-After` beyond an hour waits an hour; a negative one gives the jittered back-off. */
    @Test
    fun retryAfter_isCappedAtAnHour_andANegativeOneGivesTheJitter() {
        val hour = RETRY_AFTER_MAX_MILLIS

        assertEquals(3_600_000L, hour)
        assertEquals(hour, RetryPolicy(Random(7)).delayMillis(1, hour))
        assertEquals(hour, RetryPolicy(Random(7)).delayMillis(1, 2 * hour))
        assertEquals(hour, RetryPolicy(Random(7)).delayMillis(1, Long.MAX_VALUE))
        assertEquals(RetryPolicy(Random(7)).delayMillis(1, null), RetryPolicy(Random(7)).delayMillis(1, -1L))
        val jitter = RETRY_BASE_DELAY_MILLIS / 2..RETRY_BASE_DELAY_MILLIS
        assertTrue(RetryPolicy(Random(7)).delayMillis(1, Long.MIN_VALUE) in jitter)
    }

    @Test
    fun theSameSeed_givesTheSameDelays() {
        val first = RetryPolicy(Random(42))
        val second = RetryPolicy(Random(42))

        repeat(5) { assertEquals(first.delayMillis(it + 1, null), second.delayMillis(it + 1, null)) }
    }
}

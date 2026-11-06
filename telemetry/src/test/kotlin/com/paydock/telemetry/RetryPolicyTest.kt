package com.paydock.telemetry

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

    @Test
    fun theSameSeed_givesTheSameDelays() {
        val first = RetryPolicy(Random(42))
        val second = RetryPolicy(Random(42))

        repeat(5) { assertEquals(first.delayMillis(it + 1, null), second.delayMillis(it + 1, null)) }
    }
}

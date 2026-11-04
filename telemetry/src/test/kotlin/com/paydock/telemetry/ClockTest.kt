package com.paydock.telemetry

import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import kotlin.math.abs

/** The system clock is a UTC epoch count in milliseconds (R_11). */
internal class ClockTest {

    @Test
    fun system_isUtcEpochMillis() {
        val expected = Instant.now().toEpochMilli()

        val actual = Clock.SYSTEM.now()

        assertTrue("Clock.SYSTEM.now() = $actual, Instant.now() = $expected", abs(actual - expected) < 1_000L)
    }
}

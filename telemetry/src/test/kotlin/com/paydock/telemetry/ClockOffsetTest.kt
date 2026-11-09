package com.paydock.telemetry

import com.paydock.telemetry.TelemetryTestFakes.FakeClock
import com.paydock.telemetry.TelemetryTestFakes.T0
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [ClockOffset] (D20, R_08): server time minus device time, 0 until measured, the latest wins, and a
 * date outside 2020 to 2100 ignored.
 */
internal class ClockOffsetTest {

    private val clock = FakeClock(T0)

    @Test
    fun offset_isZeroUntilTheFirstMeasurement() {
        assertEquals(0L, ClockOffset(clock).millis)
    }

    @Test
    fun aServerAhead_givesAPositiveOffset() {
        val offset = ClockOffset(clock)

        offset.measure(T0 + 3_600_000)

        assertEquals(3_600_000L, offset.millis)
    }

    @Test
    fun aServerBehind_givesANegativeOffset() {
        val offset = ClockOffset(clock)

        offset.measure(T0 - 90_000)

        assertEquals(-90_000L, offset.millis)
    }

    @Test
    fun eachMeasurement_readsTheDeviceClockAtThatTime_andReplacesThePreviousOne() {
        val offset = ClockOffset(clock)
        offset.measure(T0 + 5_000)

        clock.nowMillis = T0 + 60_000
        offset.measure(T0 + 60_000)

        assertEquals(0L, offset.millis)
    }

    /** Review fix (R3): a date before 2020 or from 2100 on is ignored and keeps the current offset. */
    @Test
    fun aDateOutside2020To2100_isIgnored_andKeepsTheCurrentOffset() {
        val offset = ClockOffset(clock)
        offset.measure(T0 + 5_000)

        listOf(
            ClockOffset.MIN_SERVER_DATE_MILLIS - 1,
            ClockOffset.MAX_SERVER_DATE_MILLIS,
            YEAR_9999_MILLIS,
            0L,
            -1L,
            Long.MAX_VALUE,
            Long.MIN_VALUE
        ).forEach {
            offset.measure(it)
            assertEquals("$it", 5_000L, offset.millis)
        }
    }

    @Test
    fun theWindowEdges_areMeasured() {
        val offset = ClockOffset(clock)

        offset.measure(ClockOffset.MIN_SERVER_DATE_MILLIS)
        assertEquals(ClockOffset.MIN_SERVER_DATE_MILLIS - T0, offset.millis)

        offset.measure(ClockOffset.MAX_SERVER_DATE_MILLIS - 1)
        assertEquals(ClockOffset.MAX_SERVER_DATE_MILLIS - 1 - T0, offset.millis)
    }

    @Test
    fun theWindow_is2020To2100Utc() {
        assertEquals("2020-01-01T00:00:00.000Z", UtcTimestamp.iso8601(ClockOffset.MIN_SERVER_DATE_MILLIS))
        assertEquals("2100-01-01T00:00:00.000Z", UtcTimestamp.iso8601(ClockOffset.MAX_SERVER_DATE_MILLIS))
    }

    private companion object {
        const val YEAR_9999_MILLIS = 253_402_300_799_000L // 9999-12-31T23:59:59Z
    }
}

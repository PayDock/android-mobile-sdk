package com.paydock.telemetry

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit tests for [UtcTimestamp] (D20, R_10): its integer arithmetic against `java.time.Instant`, which only the tests
 * use, over epoch 0, leap days, year ends and the fixture times.
 */
internal class UtcTimestampTest {

    /** The reference: Instant's ISO-8601 text, always with three fraction digits. */
    private fun reference(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).truncatedTo(ChronoUnit.MILLIS).let { instant ->
            val text = instant.toString()
            when {
                instant.nano == 0 -> text.removeSuffix("Z") + ".000Z"
                else -> text
            }
        }

    private fun millis(iso: String): Long = Instant.parse(iso).toEpochMilli()

    @Test
    fun theCrashAnrFixtureTimes() {
        assertEquals("2026-11-03T03:58:41.000Z", UtcTimestamp.iso8601(1_793_678_321_000L)) // its occurred_at
        assertEquals("2026-11-05T03:58:41.000Z", UtcTimestamp.iso8601(1_793_851_121_000L)) // its time before D20
    }

    @Test
    fun epochZero() {
        assertEquals("1970-01-01T00:00:00.000Z", UtcTimestamp.iso8601(0L))
    }

    @Test
    fun leapDays_andTheDaysAroundThem() {
        assertEquals("2024-02-29T00:00:00.000Z", UtcTimestamp.iso8601(millis("2024-02-29T00:00:00Z")))
        assertEquals("2024-02-29T23:59:59.999Z", UtcTimestamp.iso8601(millis("2024-03-01T00:00:00Z") - 1))
        assertEquals("2024-03-01T00:00:00.000Z", UtcTimestamp.iso8601(millis("2024-03-01T00:00:00Z")))
        assertEquals("2000-02-29T12:00:00.000Z", UtcTimestamp.iso8601(millis("2000-02-29T12:00:00Z")))
        assertEquals("2100-03-01T00:00:00.000Z", UtcTimestamp.iso8601(millis("2100-02-28T00:00:00Z") + DAY))
    }

    @Test
    fun yearEnds() {
        listOf(1970, 1999, 2023, 2024, 2025, 2026, 2099, 2100).forEach { year ->
            val newYear = millis("${year + 1}-01-01T00:00:00Z")
            assertEquals("$year-12-31T23:59:59.999Z", UtcTimestamp.iso8601(newYear - 1))
            assertEquals("${year + 1}-01-01T00:00:00.000Z", UtcTimestamp.iso8601(newYear))
        }
    }

    @Test
    fun theFixtureTimes() {
        listOf(
            "2026-11-03T03:58:41.000Z",
            "2026-11-05T03:58:41.000Z",
            "2026-11-05T04:14:07.512Z",
            "2026-11-05T04:14:12.512Z"
        ).forEach { iso -> assertEquals(iso, UtcTimestamp.iso8601(millis(iso))) }
    }

    @Test
    fun beforeTheEpoch() {
        assertEquals("1969-12-31T23:59:59.999Z", UtcTimestamp.iso8601(-1L))
        assertEquals("1960-02-29T06:30:15.250Z", UtcTimestamp.iso8601(millis("1960-02-29T06:30:15.250Z")))
    }

    @Test
    fun everyDayOfFourCenturies_matchesInstant() {
        var epochMillis = millis("1900-01-01T00:00:00Z") + 37_123_456L
        val end = millis("2300-01-01T00:00:00Z")
        while (epochMillis < end) {
            assertEquals(reference(epochMillis), UtcTimestamp.iso8601(epochMillis))
            epochMillis += DAY + 1_001L
        }
    }

    private companion object {
        const val DAY = 86_400_000L
    }
}

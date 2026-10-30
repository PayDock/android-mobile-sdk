package com.paydock.telemetry

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for [TelemetryLevel.parse]: absent or blank is ALL (D3), a known name is that level, else OFF. */
internal class TelemetryLevelTest {

    @Test
    fun parse_absentOrBlank_isAll() {
        assertEquals(TelemetryLevel.ALL, TelemetryLevel.parse(null))
        assertEquals(TelemetryLevel.ALL, TelemetryLevel.parse(""))
        assertEquals(TelemetryLevel.ALL, TelemetryLevel.parse("  "))
    }

    @Test
    fun parse_levelName_isThatLevelCaseInsensitiveAndTrimmed() {
        assertEquals(TelemetryLevel.OFF, TelemetryLevel.parse("off"))
        assertEquals(TelemetryLevel.ERRORS, TelemetryLevel.parse("ERRORS"))
        assertEquals(TelemetryLevel.ALL, TelemetryLevel.parse(" all "))
    }

    @Test
    fun parse_unknownValue_isOff() {
        assertEquals(TelemetryLevel.OFF, TelemetryLevel.parse("verbose"))
    }
}

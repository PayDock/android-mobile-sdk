package com.paydock.telemetry

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [TelemetryActivation.isActive] (D22, R_02): the one-input rule. Telemetry is active only with an
 * endpoint that is not null or blank.
 */
internal class TelemetryActivationTest {

    @Test
    fun isActive_anEndpoint_isTrue() {
        assertTrue(TelemetryActivation.isActive("https://otlp.invalid/v1/logs"))
    }

    @Test
    fun isActive_noEndpoint_isFalse() {
        assertFalse(TelemetryActivation.isActive(null))
    }

    @Test
    fun isActive_blankEndpoint_isFalse() {
        listOf("", " ", "\t", "\n").forEach { endpoint ->
            assertFalse("endpoint='$endpoint'", TelemetryActivation.isActive(endpoint))
        }
    }
}

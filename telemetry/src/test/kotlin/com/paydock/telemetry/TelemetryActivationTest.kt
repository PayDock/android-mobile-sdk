package com.paydock.telemetry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [TelemetryActivation.isActive] (D22, R_02): the two-input rule. Telemetry is active only with
 * credentials that carry a non-blank endpoint and a level of ERRORS or ALL; an empty authorization is active.
 */
internal class TelemetryActivationTest {

    private val credentials = TelemetryCredentials(
        endpoint = "https://otlp.invalid/v1/logs",
        authorization = "synthetic-not-a-credential"
    )

    private val noAuthorization = TelemetryCredentials(endpoint = "https://otlp.invalid/v1/logs", authorization = "")

    @Test
    fun isActive_withAnEndpointAndALevelOtherThanOff_isTrue() {
        listOf(TelemetryLevel.ERRORS, TelemetryLevel.ALL).forEach { level ->
            assertTrue("level=$level", TelemetryActivation.isActive(credentials, level))
        }
    }

    @Test
    fun isActive_levelOff_isFalse() {
        assertFalse(TelemetryActivation.isActive(credentials, TelemetryLevel.OFF))
        assertFalse(TelemetryActivation.isActive(noAuthorization, TelemetryLevel.OFF))
    }

    @Test
    fun isActive_noCredentials_isFalseAtEveryLevel() {
        TelemetryLevel.entries.forEach { level ->
            assertFalse("level=$level", TelemetryActivation.isActive(null, level))
        }
    }

    @Test
    fun isActive_blankEndpoint_isFalseAtEveryLevel() {
        listOf("", "   ").forEach { endpoint ->
            val blank = TelemetryCredentials(endpoint = endpoint, authorization = "synthetic-not-a-credential")
            TelemetryLevel.entries.forEach { level ->
                assertFalse("endpoint='$endpoint', level=$level", TelemetryActivation.isActive(blank, level))
            }
        }
    }

    @Test
    fun isActive_emptyAuthorization_isActive() {
        assertTrue(TelemetryActivation.isActive(noAuthorization, TelemetryLevel.ALL))
        assertTrue(TelemetryActivation.isActive(noAuthorization, TelemetryLevel.ERRORS))
    }

    @Test
    fun isActive_isTrueOnlyWhenBothInputsHold() {
        val rows = listOf(null, credentials).flatMap { c -> TelemetryLevel.entries.map { c to it } }
        rows.forEach { (c, level) ->
            val expected = c != null && level != TelemetryLevel.OFF
            assertEquals("credentials=${c != null}, level=$level", expected, TelemetryActivation.isActive(c, level))
        }
    }
}

package com.paydock.telemetry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [TelemetryActivation.isActive] (RS-1): the full 16-row truth table of its four conditions.
 * Telemetry is active only when the build enables it, credentials exist, the environment is sandbox and the level
 * is not OFF.
 */
internal class TelemetryActivationTest {

    private val credentials = TelemetryCredentials(
        endpoint = "https://otlp.invalid/v1/logs",
        authorization = "synthetic-not-a-credential"
    )

    /** One combination of the four activation conditions. */
    private data class Row(
        val buildEnabled: Boolean,
        val hasCredentials: Boolean,
        val isSandbox: Boolean,
        val levelOn: Boolean,
    )

    private val booleans = listOf(false, true)

    /** Every combination of the four conditions. */
    private val truthTable: List<Row> = booleans.flatMap { build ->
        booleans.flatMap { creds ->
            booleans.flatMap { sandbox -> booleans.map { levelOn -> Row(build, creds, sandbox, levelOn) } }
        }
    }

    @Test
    fun isActive_isTrueOnlyWhenAllFourConditionsHold() {
        assertEquals(TRUTH_TABLE_ROWS, truthTable.distinct().size)
        truthTable.forEach { row ->
            val buildEnabled = row.buildEnabled
            val hasCredentials = row.hasCredentials
            val isSandbox = row.isSandbox
            val levelOn = row.levelOn
            val level = if (levelOn) TelemetryLevel.ALL else TelemetryLevel.OFF
            val expected = buildEnabled && hasCredentials && isSandbox && levelOn

            val actual = TelemetryActivation.isActive(
                buildEnabled = buildEnabled,
                credentials = if (hasCredentials) credentials else null,
                isSandbox = isSandbox,
                level = level
            )

            assertEquals(
                "buildEnabled=$buildEnabled, credentials=$hasCredentials, sandbox=$isSandbox, level=$level",
                expected,
                actual
            )
        }
    }

    @Test
    fun isActive_isTrueAtErrorsLevel() {
        assertTrue(
            TelemetryActivation.isActive(
                buildEnabled = true,
                credentials = credentials,
                isSandbox = true,
                level = TelemetryLevel.ERRORS
            )
        )
    }

    private companion object {
        const val TRUTH_TABLE_ROWS = 16
    }
}

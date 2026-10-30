package com.paydock.telemetry

import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for the [ActiveTelemetry] placeholder: it reports active, and `report` and `start` just return. */
internal class ActiveTelemetryTest {

    private val credentials = TelemetryCredentials(
        endpoint = "https://otlp.invalid/v1/logs",
        authorization = "synthetic-not-a-credential"
    )

    private val telemetry = ActiveTelemetry(credentials = credentials, level = TelemetryLevel.ALL)

    @Test
    fun isActive_isTrue() {
        assertTrue(telemetry.isActive)
    }

    @Test
    fun keepsItsCredentialsAndLevel() {
        assertSame(credentials, telemetry.credentials)
        assertSame(TelemetryLevel.ALL, telemetry.level)
    }

    @Test
    fun report_returns() {
        val error = TelemetryEvent.ErrorInfo.from(IllegalStateException("synthetic failure"))

        telemetry.report(TelemetryEvent.SdkError(error = error))

        assertTrue(telemetry.isActive)
    }

    @Test
    fun start_returnsAndIsIdempotent() {
        telemetry.start()
        telemetry.start()

        assertTrue(telemetry.isActive)
    }
}

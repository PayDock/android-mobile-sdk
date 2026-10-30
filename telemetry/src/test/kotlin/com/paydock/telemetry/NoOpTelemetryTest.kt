package com.paydock.telemetry

import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Unit tests for [NoOpTelemetry].
 *
 * The no-op is what SDK code is bound to while telemetry is disabled (RS-2): it reports nothing and starts
 * nothing. Its `= Unit` bodies make that a review fact; there is no live-thread snapshot here, because
 * `Thread.getAllStackTraces()` sees every thread of the test JVM and would flake. A release build always binds it:
 * its generated build values are disabled and empty.
 */
internal class NoOpTelemetryTest {

    private val failure = TelemetryEvent.ErrorInfo.from(IllegalStateException("synthetic failure"))

    private val oneOfEachEvent: List<TelemetryEvent> = listOf(
        TelemetryEvent.Crash(error = failure, source = TelemetryEvent.CrashSource.UNCAUGHT_HANDLER),
        TelemetryEvent.Crash(error = null, source = TelemetryEvent.CrashSource.EXIT_INFO, exitReason = 4),
        TelemetryEvent.UncaughtException(error = failure, threadName = "main"),
        TelemetryEvent.SdkError(error = failure, errorCode = "SdkException"),
        TelemetryEvent.NetworkFailure(
            method = TelemetryEvent.HttpMethod.POST,
            urlTemplate = "/v1/payment_sources/tokens",
            serverAddress = "api.example.test",
            failure = TelemetryEvent.NetworkError.HttpStatus(code = 400),
            apiError = TelemetryEvent.ApiError(status = "400", code = "validation_error")
        ),
        TelemetryEvent.NetworkFailure(
            method = TelemetryEvent.HttpMethod.GET,
            urlTemplate = "/v1/charges/{id}",
            serverAddress = "api.example.test",
            failure = TelemetryEvent.NetworkError.Transport(
                kind = TelemetryEvent.TransportKind.TIMEOUT,
                error = failure
            ),
            attempt = 2
        ),
        TelemetryEvent.NetworkFailure(
            method = TelemetryEvent.HttpMethod.POST,
            urlTemplate = "/v1/payment_sources/tokens",
            serverAddress = "api.example.test",
            failure = TelemetryEvent.NetworkError.Decode(statusCode = 200, error = failure)
        )
    )

    @Test
    fun isActive_isFalse() {
        assertFalse(NoOpTelemetry.isActive)
    }

    @Test
    fun report_acceptsEveryEventKindAndReturns() {
        oneOfEachEvent.forEach { NoOpTelemetry.report(it) }

        assertFalse(NoOpTelemetry.isActive)
    }

    @Test
    fun start_returnsAndIsIdempotent() {
        NoOpTelemetry.start()
        NoOpTelemetry.start()

        assertFalse(NoOpTelemetry.isActive)
    }
}

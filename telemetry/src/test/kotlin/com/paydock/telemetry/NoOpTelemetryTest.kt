package com.paydock.telemetry

import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Unit tests for [NoOpTelemetry].
 *
 * The no-op is what SDK code is bound to while telemetry is disabled (RS-2): it reports nothing and starts
 * nothing. Its `= Unit` bodies make that a review fact; there is no live-thread snapshot here, because
 * `Thread.getAllStackTraces()` sees every thread of the test JVM and would flake. The binding chooses it when the
 * collector URL lookup fails or returns a blank URL.
 */
internal class NoOpTelemetryTest {

    private val failure = TelemetryEvent.ErrorInfo.from(IllegalStateException("synthetic failure"))

    private val oneOfEachEvent: List<TelemetryEvent> = listOf(
        TelemetryEvent.Crash(
            error = failure,
            source = TelemetryEvent.CrashSource.UNCAUGHT_HANDLER,
            kind = TelemetryEvent.CrashKind.EXCEPTION
        ),
        TelemetryEvent.Crash(
            error = null,
            source = TelemetryEvent.CrashSource.EXIT_INFO,
            kind = TelemetryEvent.CrashKind.OS_KILL,
            reason = "LOW_MEMORY",
            occurredAt = 1_700_000_000_000L
        ),
        TelemetryEvent.UncaughtException(error = failure, threadName = "main"),
        TelemetryEvent.SdkError(error = failure, errorCode = "SdkException"),
        TelemetryEvent.NetworkFailure(
            method = TelemetryEvent.HttpMethod.POST,
            urlTemplate = "/v1/payment_sources/tokens",
            serverAddress = "api.example.test",
            failure = TelemetryEvent.NetworkError.HttpStatus(code = 400),
            apiError = TelemetryEvent.ApiError(status = 400, code = "validation_error")
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
        ),
        TelemetryEvent.WidgetOpened(widget = "CardDetailsWidget"),
        TelemetryEvent.PaymentStarted(widget = "CardDetailsWidget", paymentId = PAYMENT_ID),
        TelemetryEvent.PaymentFinished(
            widget = "CardDetailsWidget",
            paymentId = PAYMENT_ID,
            outcome = TelemetryEvent.PaymentOutcome.ERROR,
            errorCode = "TokenisingCardException",
            durationMs = 2_215L
        ),
        TelemetryEvent.Dropped(reason = TelemetryEvent.DroppedReason.RATE_LIMIT, count = 3)
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

    /** RS-8, MD-7: the no-op never touches the JVM's default uncaught-exception handler. */
    @Test
    fun start_leavesTheDefaultUncaughtExceptionHandlerUntouched() {
        val before = Thread.getDefaultUncaughtExceptionHandler()

        NoOpTelemetry.start()

        assertSame(before, Thread.getDefaultUncaughtExceptionHandler())
    }

    @Test
    fun start_returnsAndIsIdempotent() {
        NoOpTelemetry.start()
        NoOpTelemetry.start()

        assertFalse(NoOpTelemetry.isActive)
    }

    private companion object {
        const val PAYMENT_ID = "e4a1c8b7-3f29-4d06-b5e8-7c2d9f1a0b34"
    }
}

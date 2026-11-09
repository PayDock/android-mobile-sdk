package com.paydock.telemetry

import android.os.Looper
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
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

    @Test
    fun sendTestRecord_returnsAndDoesNothing() {
        NoOpTelemetry.sendTestRecord()
        NoOpTelemetry.sendTestRecord()

        assertFalse(NoOpTelemetry.isActive)
    }

    /**
     * Nothing posted, nothing installed. On the JVM every `android.os` call throws (the unmocked android.jar), so a
     * post to the main looper would fail this test; the handlers it could install are the same objects after.
     */
    @Test
    fun throwTestCrash_postsNothing_andInstallsNothing() {
        assertThrows(RuntimeException::class.java) { Looper.getMainLooper() }
        val thread = Thread.currentThread()
        val defaultBefore = Thread.getDefaultUncaughtExceptionHandler()
        val threadBefore = thread.uncaughtExceptionHandler

        NoOpTelemetry.throwTestCrash()
        NoOpTelemetry.throwTestCrash()

        assertSame(defaultBefore, Thread.getDefaultUncaughtExceptionHandler())
        assertSame(threadBefore, thread.uncaughtExceptionHandler)
    }

    /** RS-8, MD-7: the no-op never touches the JVM's default uncaught-exception handler. */
    @Test
    fun startAndThrowTestCrash_leaveTheDefaultUncaughtExceptionHandlerUntouched() {
        val before = Thread.getDefaultUncaughtExceptionHandler()

        NoOpTelemetry.start()
        NoOpTelemetry.throwTestCrash()

        assertSame(before, Thread.getDefaultUncaughtExceptionHandler())
    }

    @Test
    fun start_returnsAndIsIdempotent() {
        NoOpTelemetry.start()
        NoOpTelemetry.start()

        assertFalse(NoOpTelemetry.isActive)
    }
}

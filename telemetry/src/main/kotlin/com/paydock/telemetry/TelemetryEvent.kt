package com.paydock.telemetry

import java.util.Collections
import java.util.IdentityHashMap

/**
 * Something that happened in the SDK and becomes a record. Typed fields only, no free-form maps,
 * and never a raw Throwable: exceptions enter an event only as a sanitised [ErrorInfo].
 */
internal sealed interface TelemetryEvent {

    /**
     * The process died: a fatal uncaught exception seen by the chained handler (UNCAUGHT_HANDLER),
     * or an abnormal exit found on the next launch (EXIT_INFO).
     */
    data class Crash(
        val error: ErrorInfo?, // null when the OS report has no trace (an os_kill)
        val source: CrashSource,
        val kind: CrashKind, // paydock.crash.kind
        val reason: String? = null, // paydock.crash.reason: a name (ANR, CRASH_NATIVE, LOW_MEMORY), never an int
        val threadName: String? = null,
        val occurredAt: Long? = null, // UTC epoch ms from the OS report; null for an in-process crash
    ) : TelemetryEvent

    /**
     * An exception that escaped SDK-owned code (a coroutine or a callback) and was caught by the SDK's own
     * handler while the process kept running — unlike Crash(source = UNCAUGHT_HANDLER), which is the process dying.
     */
    data class UncaughtException(
        val error: ErrorInfo,
        val threadName: String? = null,
        val widget: String? = null,
    ) : TelemetryEvent

    data class SdkError(
        val error: ErrorInfo,
        val errorCode: String? = null, // SdkException subclass simple name
        val widget: String? = null,
        val apiError: ApiError? = null, // spec § 5.4 API codes when the error carries an API error body (R_03)
    ) : TelemetryEvent

    data class NetworkFailure(
        val method: HttpMethod,
        val urlTemplate: String, // path template, never real ids
        val serverAddress: String,
        val failure: NetworkError, // the status code and the failure kind cannot disagree
        val attempt: Int = 1,
        val apiError: ApiError? = null,
        val widget: String? = null,
    ) : TelemetryEvent

    /** A widget was shown (D19). */
    data class WidgetOpened(val widget: String) : TelemetryEvent

    /** A payment attempt started (D24, § 5.9). */
    data class PaymentStarted(val widget: String, val paymentId: String) : TelemetryEvent

    /** A payment attempt ended. durationMs is set by the reducer only; the SDK always passes null. */
    data class PaymentFinished(
        val widget: String,
        val paymentId: String,
        val outcome: PaymentOutcome,
        val errorCode: String? = null, // kept only when outcome == ERROR
        val durationMs: Long? = null,
    ) : TelemetryEvent

    /** How many records were dropped for one reason (spec § 5.8). Only the reducer creates it; never reported. */
    data class Dropped(val reason: DroppedReason, val count: Int) : TelemetryEvent

    /**
     * A sanitised exception: its fully qualified type, its stack frames and its cause, never a message,
     * toString() output, localized text or suppressed exceptions (spec § 5.5).
     */
    data class ErrorInfo(
        val type: String?, // throwable.javaClass.name; null only for frames without an exception (an ANR thread dump)
        val frames: List<String>, // "<class>.<method>(<file>:<line>)", at most MAX_FRAMES
        val cause: ErrorInfo? = null, // the chain, at most MAX_CAUSES deep
    ) {
        /** Ordered types down the cause chain, outermost cause first (paydock.exception.cause_types). */
        val causeTypes: List<String>
            get() = generateSequence(cause) { it.cause }.mapNotNull { it.type }.toList()

        companion object {
            const val MAX_FRAMES = 64
            const val MAX_CAUSES = 8

            /**
             * Never throws: on any failure it falls back to the type alone.
             *
             * Reads only the throwable's class, its stack frames and its cause chain: never its message,
             * localized message, toString() or suppressed exceptions. The chain stops after [MAX_CAUSES] causes
             * and at a cause already visited, because cause chains can be cyclic. Each frame is built from the
             * [StackTraceElement] fields, never its toString(), which can prefix class-loader and module names.
             */
            fun from(throwable: Throwable): ErrorInfo = try {
                fun framesOf(source: Throwable): List<String> = source.stackTrace.take(MAX_FRAMES).map { frame ->
                    val location = if (frame.isNativeMethod) {
                        "Native Method"
                    } else {
                        "${frame.fileName ?: "Unknown Source"}:${frame.lineNumber}"
                    }
                    "${frame.className}.${frame.methodName}($location)"
                }

                val visited = Collections.newSetFromMap(IdentityHashMap<Throwable, Boolean>())
                visited.add(throwable)
                val causes = mutableListOf<Throwable>()
                var next = throwable.cause
                while (next != null && causes.size < MAX_CAUSES && visited.add(next)) {
                    causes.add(next)
                    next = next.cause
                }
                val causeInfo = causes.foldRight(null as ErrorInfo?) { cause, inner ->
                    ErrorInfo(type = cause.javaClass.name, frames = framesOf(cause), cause = inner)
                }
                ErrorInfo(type = throwable.javaClass.name, frames = framesOf(throwable), cause = causeInfo)
            } catch (ignored: Throwable) {
                ErrorInfo(type = throwable.javaClass.name, frames = emptyList())
            }
        }
    }

    /** How a Paydock API call failed. A sealed type, so a status code and a transport failure never coexist. */
    sealed interface NetworkError {
        data class HttpStatus(val code: Int) : NetworkError // error.type http_4xx / http_5xx by code
        data class Transport(val kind: TransportKind, val error: ErrorInfo? = null) : NetworkError
        data class Decode(val statusCode: Int? = null, val error: ErrorInfo? = null) : NetworkError
    }

    /** Structured fields of a Paydock error body. Never its message text. */
    data class ApiError(
        val status: Int? = null, // spec § 5.4 (2026-11-06)
        val code: String? = null,
        val summaryCode: String? = null,
        val gatewayCodes: List<String> = emptyList(),
    )

    enum class CrashSource(val value: String) { UNCAUGHT_HANDLER("uncaught_handler"), EXIT_INFO("exit_info") }

    enum class CrashKind(val value: String) { EXCEPTION("exception"), ANR("anr"), NATIVE("native"), OS_KILL("os_kill") }

    enum class DroppedReason(val value: String) {
        RATE_LIMIT("rate_limit"),
        STORE_FULL("store_full"),
        TOO_OLD("too_old"),
        REJECTED("rejected"),
    }

    enum class TransportKind(val value: String) {
        TIMEOUT("timeout"),
        CONNECTION("connection"),
        TLS("tls"),
        UNKNOWN("unknown"),
    }

    /** How a payment attempt ended (paydock.payment.outcome, § 5.9). */
    enum class PaymentOutcome(val value: String) {
        SUCCESS("success"),
        DECLINED("declined"),
        ERROR("error"),
        CANCELLED("cancelled"),
    }

    enum class HttpMethod { GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS }
}

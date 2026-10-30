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
        val error: ErrorInfo?, // null for next-launch crashes reported from exit info
        val source: CrashSource,
        val exitReason: Int? = null, // ApplicationExitInfo reason, next-launch crashes only
        val threadName: String? = null,
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
        val errorCode: String? = null, // SdkException class where one exists
        val widget: String? = null,
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

    /**
     * A sanitised exception: its fully qualified type, its stack frames and its cause, never a message,
     * toString() output, localized text or suppressed exceptions (spec § 5.5).
     */
    data class ErrorInfo(
        val type: String, // throwable.javaClass.name
        val frames: List<String>, // "<class>.<method>(<file>:<line>)", at most MAX_FRAMES
        val cause: ErrorInfo? = null, // the chain, at most MAX_CAUSES deep
    ) {
        /** Ordered types down the cause chain, outermost cause first (paydock.exception.cause_types). */
        val causeTypes: List<String>
            get() = generateSequence(cause) { it.cause }.map { it.type }.toList()

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
        val status: String? = null,
        val code: String? = null,
        val summaryCode: String? = null,
        val gatewayCodes: List<String> = emptyList(),
    )

    enum class CrashSource { UNCAUGHT_HANDLER, EXIT_INFO }

    enum class TransportKind { TIMEOUT, CONNECTION, TLS, UNKNOWN }

    enum class HttpMethod { GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS }
}

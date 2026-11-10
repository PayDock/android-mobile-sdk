package com.paydock.telemetry

import com.paydock.telemetry.AttributeValue.Bool
import com.paydock.telemetry.AttributeValue.Num
import com.paydock.telemetry.AttributeValue.Str
import com.paydock.telemetry.TelemetryEvent.ErrorInfo
import com.paydock.telemetry.TelemetryEvent.NetworkError

/**
 * Builds the OTLP log record of a [PendingRecord] (spec § 5.2 to § 5.8). Pure: the same pending record and
 * connection give the same record. It mirrors `record()` of the golden fixtures' reference encoder, `generate.py`;
 * the attributes come out in [Allowlist.RECORD_KEYS] order, and the body is [RecordBody.of] them. Nothing here reads an
 * exception message.
 */
internal object RecordFactory {
    const val PAYDOCK_FRAME_PREFIX = "com.paydock."

    private const val LIST_SEPARATOR = ", "
    private const val FIRST_SERVER_ERROR = 500

    /** A record's event name and severity, the parts that depend on the event type. */
    private class Shape(val name: String, val severity: Severity)

    fun create(pending: PendingRecord, connection: NetworkConnectionType): TelemetryRecord {
        val event = pending.event
        val values = linkedMapOf<String, AttributeValue>(
            "session.id" to Str(pending.sessionId),
            "log.record.uid" to Str(pending.id),
            "network.connection.type" to Str(connection.value)
        )
        val shape = when (event) {
            is TelemetryEvent.Crash -> crashShape(event, values)
            is TelemetryEvent.UncaughtException ->
                errorShape("uncaught", event.error, code = null, event.threadName, event.widget, values)
            is TelemetryEvent.SdkError -> {
                event.apiError?.let { putApiError(it, values) }
                errorShape("sdk_error", event.error, event.errorCode, threadName = null, event.widget, values)
            }
            is TelemetryEvent.NetworkFailure -> networkShape(event, values)
            is TelemetryEvent.WidgetOpened -> {
                values["paydock.widget"] = Str(event.widget)
                Shape("paydock.widget.opened", Severity.INFO)
            }
            is TelemetryEvent.Dropped -> {
                values["paydock.dropped.reason"] = Str(event.reason.value)
                values["paydock.dropped.count"] = Num(event.count.toLong())
                Shape("paydock.telemetry.dropped", Severity.WARN)
            }
        }
        values["event.name"] = Str(shape.name)
        val attributes = Allowlist.RECORD_KEYS.keys.mapNotNull { key -> values[key]?.let { Attribute(key, it) } }
        return TelemetryRecord(
            id = pending.id,
            timeUnixMillis = (event as? TelemetryEvent.Crash)?.occurredAt ?: pending.createdAt,
            severity = shape.severity,
            body = RecordBody.of(attributes),
            attributes = attributes
        )
    }

    private fun crashShape(event: TelemetryEvent.Crash, values: MutableMap<String, AttributeValue>): Shape {
        putException(event.error, values)
        values["paydock.crash.kind"] = Str(event.kind.value)
        values["paydock.crash.source"] = Str(event.source.value)
        event.reason.nonEmpty()?.let { values["paydock.crash.reason"] = Str(it) }
        event.threadName.nonEmpty()?.let { values["thread.name"] = Str(it) }
        return Shape("paydock.crash", Severity.FATAL)
    }

    @Suppress("LongParameterList") // the error events' shared fields, each optional
    private fun errorShape(
        kind: String,
        error: ErrorInfo,
        code: String?,
        threadName: String?,
        widget: String?,
        values: MutableMap<String, AttributeValue>,
    ): Shape {
        putException(error, values)
        values["paydock.error.kind"] = Str(kind)
        code.nonEmpty()?.let { values["paydock.error.code"] = Str(it) }
        widget.nonEmpty()?.let { values["paydock.widget"] = Str(it) }
        threadName.nonEmpty()?.let { values["thread.name"] = Str(it) }
        return Shape("paydock.error", Severity.ERROR)
    }

    private fun networkShape(event: TelemetryEvent.NetworkFailure, values: MutableMap<String, AttributeValue>): Shape {
        val failure = event.failure
        val (status, errorType, error) = when (failure) {
            is NetworkError.HttpStatus ->
                Triple(failure.code, if (failure.code < FIRST_SERVER_ERROR) "http_4xx" else "http_5xx", null)
            is NetworkError.Transport -> Triple(null, failure.kind.value, failure.error)
            is NetworkError.Decode -> Triple(failure.statusCode, "decode", failure.error)
        }
        val method = event.method.name
        values["http.request.method"] = Str(method)
        values["url.template"] = Str(event.urlTemplate)
        values["server.address"] = Str(event.serverAddress)
        status?.let { values["http.response.status_code"] = Num(it.toLong()) }
        values["error.type"] = Str(errorType)
        event.apiError?.let { putApiError(it, values) }
        values["paydock.http.attempt"] = Num(event.attempt.toLong())
        event.widget.nonEmpty()?.let { values["paydock.widget"] = Str(it) }
        error?.let {
            it.type?.let { type -> values["exception.type"] = Str(type) }
            values["exception.stacktrace"] = Str(StackTraceFormatter.format(it))
        }
        if (failure is NetworkError.HttpStatus) {
            values["paydock.group.key"] = Str("http $method ${event.urlTemplate} ${failure.code}")
        }
        return Shape("paydock.network.failure", Severity.ERROR)
    }

    private fun putApiError(apiError: TelemetryEvent.ApiError, values: MutableMap<String, AttributeValue>) {
        apiError.status?.let { values["paydock.api.status"] = Num(it.toLong()) }
        apiError.code.nonEmpty()?.let { values["paydock.api.error.code"] = Str(it) }
        apiError.summaryCode.nonEmpty()?.let { values["paydock.api.error.summary_code"] = Str(it) }
        if (apiError.gatewayCodes.isNotEmpty()) {
            values["paydock.api.gateway.codes"] = Str(apiError.gatewayCodes.joinToString(LIST_SEPARATOR))
        }
    }

    /** The exception fields of a crash or an error; `paydock.exception.in_sdk` is false without an error. */
    private fun putException(error: ErrorInfo?, values: MutableMap<String, AttributeValue>) {
        if (error != null) {
            error.type?.let { values["exception.type"] = Str(it) }
            values["exception.stacktrace"] = Str(StackTraceFormatter.format(error))
            val causeTypes = error.causeTypes
            if (causeTypes.isNotEmpty()) {
                values["paydock.exception.cause_types"] = Str(causeTypes.joinToString(LIST_SEPARATOR))
            }
        }
        values["paydock.exception.in_sdk"] = Bool(error != null && isInSdk(error))
    }

    /** True when any frame anywhere in the chain is SDK code. */
    private fun isInSdk(error: ErrorInfo): Boolean =
        generateSequence(error) { it.cause }.any { info -> info.frames.any { it.startsWith(PAYDOCK_FRAME_PREFIX) } }

    private fun String?.nonEmpty(): String? = this?.takeIf { it.isNotEmpty() }
}

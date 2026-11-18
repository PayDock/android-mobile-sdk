package com.paydock.telemetry

/**
 * One OTLP/HTTP attempt per batch (spec § 8): encodes the batch with [OtlpLogEncoder], gzips it and sends it with
 * two headers, `Content-Encoding: gzip` and `Content-Type: application/json`, plus `Authorization: Basic …` when
 * there is a credential (the debug override); the SDK's collector gets none (D22). Then maps the transport's result.
 * Never throws.
 */
internal class OtlpExporter(
    private val transport: TelemetryTransport,
    private val credentials: TelemetryCredentials,
) : Exporter {

    @Suppress("TooGenericExceptionCaught", "SwallowedException") // export never throws: a failure is a retry
    override fun export(batch: ExportBatch): ExportResult = try {
        val payload = Gzip.compress(OtlpLogEncoder.encode(batch.resource, batch.records))
        map(transport.send(payload, headers()))
    } catch (ignored: Throwable) {
        ExportResult.Retry(null)
    }

    private fun headers(): Map<String, String> = linkedMapOf(CONTENT_ENCODING to GZIP, CONTENT_TYPE to JSON).apply {
        if (credentials.authorization.isNotBlank()) put(AUTHORIZATION, authorization())
    }

    /** `Basic <authorization>`, or the value as is when it already starts with `Basic `. */
    private fun authorization(): String {
        val value = credentials.authorization
        return if (value.startsWith(BASIC_PREFIX)) value else BASIC_PREFIX + value
    }

    private fun map(result: TransportResult): ExportResult = when (result) {
        is TransportResult.Success -> ExportResult.Success(result.rejectedRecords)
        is TransportResult.HttpError -> when {
            result.status == UNAUTHORIZED || result.status == FORBIDDEN -> ExportResult.Unauthorized
            result.status == PAYLOAD_TOO_LARGE -> ExportResult.TooLarge
            result.status == REQUEST_TIMEOUT ||
                result.status == TOO_MANY_REQUESTS ||
                result.status in SERVER_ERRORS -> ExportResult.Retry(result.retryAfterMillis)
            else -> ExportResult.Rejected
        }
        TransportResult.Timeout,
        TransportResult.ConnectionFailure,
        TransportResult.Failure -> ExportResult.Retry(null)
    }

    private companion object {
        const val CONTENT_ENCODING = "Content-Encoding"
        const val CONTENT_TYPE = "Content-Type"
        const val AUTHORIZATION = "Authorization"
        const val GZIP = "gzip"
        const val JSON = "application/json"
        const val BASIC_PREFIX = "Basic "
        const val UNAUTHORIZED = 401
        const val FORBIDDEN = 403
        const val REQUEST_TIMEOUT = 408
        const val PAYLOAD_TOO_LARGE = 413
        const val TOO_MANY_REQUESTS = 429
        val SERVER_ERRORS = 500..599
    }
}

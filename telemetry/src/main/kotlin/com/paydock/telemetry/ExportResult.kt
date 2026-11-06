package com.paydock.telemetry

/** What the exporter reports for one attempt of one batch (spec § 8). */
internal sealed interface ExportResult {
    /** Any 2xx; [rejectedRecords] is the gateway's partial-success count of records it refused. */
    data class Success(val rejectedRecords: Long = 0) : ExportResult

    /** No connection, timeout, failure, 408, 429, 5xx. */
    data class Retry(val retryAfterMillis: Long?) : ExportResult

    /** 401, 403. */
    data object Unauthorized : ExportResult

    /** 413. */
    data object TooLarge : ExportResult

    /** Any other non-2xx. */
    data object Rejected : ExportResult
}

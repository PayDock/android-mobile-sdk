package com.paydock.telemetry

/** The outcome of one send (spec § 8): a status, or the kind of failure that kept one from arriving. */
internal sealed interface TransportResult {
    /** Any 2xx. [rejectedRecords] is the gateway's partial-success count of refused records, 0 when absent. */
    data class Success(val status: Int, val rejectedRecords: Long = 0) : TransportResult

    /** Any other status; [retryAfterMillis] only from a non-negative integer `Retry-After` in seconds. */
    data class HttpError(val status: Int, val retryAfterMillis: Long?) : TransportResult

    data object Timeout : TransportResult

    /** DNS, refused, reset, TLS. */
    data object ConnectionFailure : TransportResult

    /** Anything else. */
    data object Failure : TransportResult
}

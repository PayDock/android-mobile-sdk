package com.paydock.telemetry

/** A reported event on its way to becoming a record (RecordFactory, A5). Typed fields only. */
internal data class PendingRecord(
    val id: String, // "<sessionId>-<sequence>", deterministic
    val sessionId: String,
    val event: TelemetryEvent,
    val firstSeenAt: Long, // UTC epoch milliseconds
    val occurrences: Int = 1, // paydock.occurrences
)

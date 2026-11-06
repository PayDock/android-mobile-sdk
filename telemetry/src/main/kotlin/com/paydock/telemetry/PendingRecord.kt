package com.paydock.telemetry

/** A reported event on its way to becoming a record ([RecordFactory]). Typed fields only. */
internal data class PendingRecord(
    val id: String, // "<sessionId>-<sequence>", sequence from RecordSequence (log.record.uid)
    val sessionId: String,
    val event: TelemetryEvent,
    val createdAt: Long, // UTC epoch ms, when the reducer created it
)

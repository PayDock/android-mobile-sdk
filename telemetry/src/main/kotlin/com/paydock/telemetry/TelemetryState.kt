package com.paydock.telemetry

/** The active path's immutable state. Changed only by [TelemetryReducer] (R_01). */
internal data class TelemetryState(
    val level: TelemetryLevel,
    val enabled: Boolean,
    val sessionId: String,
    val queued: Int = 0,
    val persisted: Int = 0,
    val lastExport: Long? = null, // UTC epoch milliseconds
    val flushScheduled: Boolean = false,
    val nextSequence: Long = 0,
    val collapsing: Map<String, PendingRecord> = emptyMap(), // fingerprint → record held in its window
)

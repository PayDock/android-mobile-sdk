package com.paydock.telemetry

/**
 * The active path's immutable state. Changed only by [TelemetryReducer] (R_01). It keeps counts, windows, notice
 * IDs and the names of the widgets opened this session only: the records themselves live on disk.
 */
internal data class TelemetryState(
    val enabled: Boolean,
    val sessionId: String,
    val pending: Int = 0, // records on disk not yet exported, as last counted
    val lastExport: Long? = null, // UTC epoch ms
    val flushScheduled: Boolean = false, // a 10 s timer is armed; only its own tick clears it
    val exportStopped: Boolean = false, // 401/403 or give-up: no export or timer for the rest of the process
    val windowStart: Long? = null, // monotonic ms; the current hour of the cap and the notices
    val createdInWindow: Int = 0, // records created this hour, notices excluded
    val notices: Map<TelemetryEvent.DroppedReason, DroppedNotice> = emptyMap(),
    val openedWidgets: Set<String> = emptySet(), // widgets recorded this session (D19, R_07)
)

package com.paydock.telemetry

/** The reducer's single input. Only the module creates signals; the facade accepts events, never intents. */
internal sealed interface TelemetryIntent {
    data class Reported(val event: TelemetryEvent) : TelemetryIntent
    data class Flush(val source: FlushSource) : TelemetryIntent
    data object AppBackgrounded : TelemetryIntent

    /** Records lost for [reason]: they become, or add to, that reason's notice. */
    data class RecordsDropped(val reason: TelemetryEvent.DroppedReason, val count: Int) : TelemetryIntent

    /** The number of records on disk after a flush. */
    data class PendingCounted(val count: Int) : TelemetryIntent

    /** Notices whose batch was accepted or rejected: the next drop of their reason starts a new notice. */
    data class NoticesExported(val ids: Set<String>) : TelemetryIntent

    /** 401/403 or give-up: no export or timer for the rest of the process. */
    data object ExportStopped : TelemetryIntent

    enum class FlushSource { TIMER, START, RETRY }
}

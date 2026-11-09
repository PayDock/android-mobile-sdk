package com.paydock.telemetry

/** What one read found: the crash events, oldest first, and the newest exit time seen (null when none). */
internal data class ExitReports(val events: List<TelemetryEvent.Crash>, val newestTimestamp: Long?)

package com.paydock.telemetry

/** Lifecycle, held only by the activation binding; SDK code never sees it. */
internal interface TelemetryRuntime {
    fun start()

    /** Stores and sends one test record: the debug-only path (R_16). [NoOpTelemetry] does nothing. */
    fun sendTestRecord()
}

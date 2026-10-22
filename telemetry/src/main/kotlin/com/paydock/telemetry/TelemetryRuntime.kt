package com.paydock.telemetry

/** Lifecycle, held only by the activation binding; SDK code never sees it. */
internal interface TelemetryRuntime {
    fun start()
}

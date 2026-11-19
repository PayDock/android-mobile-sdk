package com.paydock.telemetry

/** Lifecycle, held only by the activation binding; SDK code never sees it. Its one call is [start]. */
internal interface TelemetryRuntime {
    fun start()
}

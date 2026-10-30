package com.paydock.telemetry

/** Bound when telemetry is disabled. Does nothing: no thread, file, storage, client or registration (RS-2). */
internal object NoOpTelemetry : Telemetry, TelemetryRuntime {
    override val isActive: Boolean = false
    override fun report(event: TelemetryEvent) = Unit
    override fun start() = Unit
}

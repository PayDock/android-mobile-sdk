package com.paydock.telemetry

/**
 * Bound when activation passes. A placeholder until the core slice (MD-25) fills it: it reports active and does
 * nothing else — no thread, file, client or registration.
 */
internal class ActiveTelemetry(
    val credentials: TelemetryCredentials,
    val level: TelemetryLevel,
) : Telemetry, TelemetryRuntime {
    override val isActive: Boolean = true
    override fun report(event: TelemetryEvent) = Unit
    override fun start() = Unit
}

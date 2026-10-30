package com.paydock.telemetry

/** The only telemetry surface SDK code sees. Thread-safe, never throws, returns immediately. */
internal interface Telemetry {
    val isActive: Boolean
    fun report(event: TelemetryEvent)
}

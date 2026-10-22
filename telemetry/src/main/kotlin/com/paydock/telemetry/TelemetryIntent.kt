package com.paydock.telemetry

/** The reducer's single input. Only the module creates signals; the facade accepts events, never intents. */
internal sealed interface TelemetryIntent {
    data class Reported(val event: TelemetryEvent) : TelemetryIntent
    data object Flush : TelemetryIntent
    data object AppBackgrounded : TelemetryIntent
    data class LevelChanged(val level: TelemetryLevel) : TelemetryIntent
}

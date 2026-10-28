package com.paydock.telemetry

/** The one activation decision (RS-1). Pure: the SDK passes in what the module cannot see. */
internal object TelemetryActivation {
    fun isActive(
        buildEnabled: Boolean,
        credentials: TelemetryCredentials?,
        isSandbox: Boolean,
        level: TelemetryLevel,
    ): Boolean = buildEnabled && credentials != null && isSandbox && level != TelemetryLevel.OFF
}

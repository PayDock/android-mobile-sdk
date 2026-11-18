package com.paydock.telemetry

/** The one activation decision (D22): an endpoint and a level other than OFF. Pure. */
internal object TelemetryActivation {
    fun isActive(credentials: TelemetryCredentials?, level: TelemetryLevel): Boolean =
        credentials != null && credentials.endpoint.isNotBlank() && level != TelemetryLevel.OFF
}

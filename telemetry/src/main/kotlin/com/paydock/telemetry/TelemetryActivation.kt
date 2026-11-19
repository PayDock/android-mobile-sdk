package com.paydock.telemetry

/** The one activation decision (D22): a collector endpoint that is not blank. Pure. */
internal object TelemetryActivation {
    fun isActive(endpoint: String?): Boolean = !endpoint.isNullOrBlank()
}

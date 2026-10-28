package com.paydock.telemetry

/** Endpoint and authorization, or null when none are configured (D5). */
internal fun interface TelemetryCredentialsProvider {
    fun credentials(): TelemetryCredentials?
}

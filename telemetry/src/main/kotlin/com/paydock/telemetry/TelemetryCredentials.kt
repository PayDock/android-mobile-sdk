package com.paydock.telemetry

/** Where telemetry is sent and the authorization header value. Never prints the authorization. */
internal class TelemetryCredentials(val endpoint: String, val authorization: String) {
    override fun toString(): String = "TelemetryCredentials(endpoint=$endpoint, authorization=<redacted>)"
}

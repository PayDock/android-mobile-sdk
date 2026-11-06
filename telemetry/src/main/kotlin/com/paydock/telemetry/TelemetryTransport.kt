package com.paydock.telemetry

/** Sends one prepared request body. */
internal interface TelemetryTransport {
    /** Blocking; called on the worker only; never throws. Sends [payload] and [headers] unchanged. */
    fun send(payload: ByteArray, headers: Map<String, String>): TransportResult
}

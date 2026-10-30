package com.paydock.telemetry

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [TelemetryCredentials]: its string form names the endpoint and never the authorization. */
internal class TelemetryCredentialsTest {

    @Test
    fun toString_holdsTheEndpointAndNeverTheAuthorization() {
        val credentials = TelemetryCredentials(endpoint = ENDPOINT, authorization = AUTHORIZATION)

        val text = credentials.toString()

        assertTrue(text, text.contains(ENDPOINT))
        assertFalse("toString must not contain the authorization", text.contains(AUTHORIZATION))
    }

    private companion object {
        const val ENDPOINT = "https://otlp.invalid/v1/logs"
        const val AUTHORIZATION = "synthetic-not-a-credential-5d1e"
    }
}

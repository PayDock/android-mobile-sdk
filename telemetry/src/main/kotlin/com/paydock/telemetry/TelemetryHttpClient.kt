package com.paydock.telemetry

import com.paydock.core.network.NetworkClientBuilder
import com.paydock.core.network.addInterceptor
import io.ktor.client.HttpClient
import io.ktor.http.Url

/**
 * The transport's own client from the networking library: debug logging off, no retries, the request and response
 * timeouts at [TelemetryLimits.REQUEST_TIMEOUT_MILLIS], and [DropErrorBodyInterceptor] so an error status arrives
 * with an empty body. Never shared with the SDK's API client.
 */
internal object TelemetryHttpClient {
    private const val MILLIS_PER_SECOND = 1000.0

    fun create(endpoint: String): HttpClient {
        val url = Url(endpoint)
        val timeoutSeconds = TelemetryLimits.REQUEST_TIMEOUT_MILLIS / MILLIS_PER_SECOND
        return NetworkClientBuilder.create()
            .setBaseUrl("${url.protocol.name}://${url.host}")
            .setDebug(false)
            .setRequestTimeout(timeoutSeconds)
            .setResponseTimeout(timeoutSeconds)
            .addInterceptor(DropErrorBodyInterceptor)
            .build()
    }
}

package com.paydock.telemetry

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * The production wiring: [OtlpHttpTransport] on [TelemetryHttpClient.create], with the networking library's real
 * OkHttp engine and interceptors, against OkHttp's [MockWebServer].
 */
internal class TelemetryHttpClientTest {

    private val server = MockWebServer()
    private val payload = Gzip.compress("""{"resourceLogs":[]}""".toByteArray(Charsets.UTF_8))
    private val headers = mapOf(
        "Content-Encoding" to "gzip",
        "Content-Type" to "application/json"
    )

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun transport(): OtlpHttpTransport {
        val endpoint = server.url("/v1/logs").toString()
        return OtlpHttpTransport(endpoint, ClockOffset(Clock.SYSTEM)) { TelemetryHttpClient.create(endpoint) }
    }

    @Test
    fun send_429WithJsonErrorBody_isHttpErrorWithRetryAfter() {
        server.enqueue(
            MockResponse()
                .setResponseCode(429)
                .setHeader("Content-Type", "application/json")
                .setHeader("Retry-After", "7")
                .setBody("""{"status":429,"error":{"message":"rate limited","code":"too_many_requests"}}""")
        )

        assertEquals(TransportResult.HttpError(429, 7000), transport().send(payload, headers))
    }

    @Test
    fun send_503WithTextBody_isHttpError() {
        server.enqueue(MockResponse().setResponseCode(503).setBody("upstream unavailable"))

        assertEquals(TransportResult.HttpError(503, null), transport().send(payload, headers))
    }

    @Test
    fun send_204_isSuccess() {
        server.enqueue(MockResponse().setResponseCode(204))

        assertEquals(TransportResult.Success(204, 0), transport().send(payload, headers))
    }

    @Test
    fun send_200WithPartialSuccess_countsTheRejectedRecords() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"partialSuccess":{"rejectedLogRecords":"2"}}""")
        )

        assertEquals(TransportResult.Success(200, 2), transport().send(payload, headers))
    }

    @Test
    fun send_carriesExactlyTheGivenBytesAndHeaders() {
        server.enqueue(MockResponse().setResponseCode(204))

        transport().send(payload, headers)

        val request = server.takeRequest(5, TimeUnit.SECONDS)!!
        assertEquals("POST", request.method)
        assertEquals("/v1/logs", request.path)
        assertArrayEquals(payload, request.body.readByteArray())
        assertEquals("gzip", request.getHeader("Content-Encoding"))
        assertEquals("application/json", request.getHeader("Content-Type"))
        assertNull("never an Authorization header", request.getHeader("Authorization"))
    }

    @Test
    fun send_serverNeverAnswers_isTimeoutWithinTheOverallLimit() {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val started = System.nanoTime()
        val result = transport().send(payload, headers)
        val elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started)

        assertEquals(TransportResult.Timeout, result)
        assertTrue(
            "took $elapsedMillis ms",
            elapsedMillis < TelemetryLimits.REQUEST_TIMEOUT_MILLIS + TIMEOUT_MARGIN_MILLIS
        )
    }

    private companion object {
        const val TIMEOUT_MARGIN_MILLIS = 3_000L
    }
}

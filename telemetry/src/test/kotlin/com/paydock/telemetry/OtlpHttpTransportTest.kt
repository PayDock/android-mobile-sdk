package com.paydock.telemetry

import com.paydock.core.network.NetworkClientBuilder
import com.paydock.core.network.exceptions.ApiParseException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.headersOf
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Unit tests for [OtlpHttpTransport] on the networking library's client with a [MockEngine]: what goes out, and how
 * each outcome maps to a [TransportResult].
 */
internal class OtlpHttpTransportTest {

    private val json = """{"resourceLogs":[]}""".toByteArray(Charsets.UTF_8)
    private val payload = Gzip.compress(json)
    private val headers = mapOf(
        "Content-Encoding" to "gzip",
        "Content-Type" to "application/json",
        "Authorization" to "Basic synthetic-not-a-credential"
    )

    private val requests = mutableListOf<HttpRequestData>()
    private var factoryCalls = 0

    private fun transport(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): OtlpHttpTransport {
        val engine = MockEngine { request ->
            requests += request
            handler(request)
        }
        return OtlpHttpTransport(ENDPOINT) {
            factoryCalls++
            client(engine)
        }
    }

    private fun client(engine: MockEngine): HttpClient =
        NetworkClientBuilder.create().setBaseUrl(BASE_URL).setMockEngine(engine).build()

    private fun respondingWith(
        status: HttpStatusCode,
        body: String = "",
        vararg responseHeaders: Pair<String, String>,
    ) = transport {
        respond(
            body,
            status,
            headersOf(*responseHeaders.map { (name, value) -> name to listOf(value) }.toTypedArray())
        )
    }

    private fun throwing(error: Throwable) = transport { throw error }

    @Test
    fun send_postsThePayloadAndHeadersUnchanged() {
        val transport = respondingWith(HttpStatusCode.NoContent)

        transport.send(payload, headers)

        val request = requests.single()
        assertEquals(HttpMethod.Post, request.method)
        assertEquals(ENDPOINT, request.url.toString())
        assertArrayEquals("the transport must never compress", payload, (request.body as ByteArrayContent).bytes())
        assertEquals("gzip", request.headers["Content-Encoding"])
        assertEquals("Basic synthetic-not-a-credential", request.headers["Authorization"])
        // Ktor hands an engine the Content-Type on the body, not in the headers; the wire test checks the header.
        assertEquals(ContentType.Application.Json, request.body.contentType)
    }

    @Test
    fun send_204_isSuccessWithNoRejection() {
        assertEquals(TransportResult.Success(204, 0), respondingWith(HttpStatusCode.NoContent).send(payload, headers))
    }

    @Test
    fun send_200WithoutPartialSuccess_isSuccessWithNoRejection() {
        assertEquals(TransportResult.Success(200, 0), respondingWith(HttpStatusCode.OK, "{}").send(payload, headers))
    }

    @Test
    fun send_200WithEmptyBody_isSuccessWithNoRejection() {
        assertEquals(TransportResult.Success(200, 0), respondingWith(HttpStatusCode.OK).send(payload, headers))
    }

    @Test
    fun send_200WithPartialSuccessAsString_countsTheRejectedRecords() {
        val body = """{"partialSuccess":{"rejectedLogRecords":"3"}}"""

        assertEquals(TransportResult.Success(200, 3), respondingWith(HttpStatusCode.OK, body).send(payload, headers))
    }

    @Test
    fun send_200WithPartialSuccessAsNumber_countsTheRejectedRecords() {
        val body = """{"partialSuccess":{"rejectedLogRecords":3,"errorMessage":"entry too far behind"}}"""

        assertEquals(TransportResult.Success(200, 3), respondingWith(HttpStatusCode.OK, body).send(payload, headers))
    }

    @Test
    fun send_200WithMalformedBody_isSuccessWithNoRejection() {
        val malformed = listOf(
            """{"partialSuccess":""",
            """{"partialSuccess":{"rejectedLogRecords":"many"}}""",
            """{"partialSuccess":{"rejectedLogRecords":-2}}""",
            """["partialSuccess"]"""
        )

        malformed.forEach { body ->
            val transport = respondingWith(HttpStatusCode.OK, body)

            assertEquals(body, TransportResult.Success(200, 0), transport.send(payload, headers))
        }
    }

    @Test
    fun send_400_isHttpError() {
        assertEquals(
            TransportResult.HttpError(400, null),
            respondingWith(HttpStatusCode.BadRequest).send(payload, headers)
        )
    }

    @Test
    fun send_503_isHttpError() {
        assertEquals(
            TransportResult.HttpError(503, null),
            respondingWith(HttpStatusCode.ServiceUnavailable).send(payload, headers)
        )
    }

    @Test
    fun send_429WithRetryAfterSeconds_givesTheDelayInMillis() {
        val transport = respondingWith(HttpStatusCode.TooManyRequests, "", "Retry-After" to "7")

        assertEquals(TransportResult.HttpError(429, 7000), transport.send(payload, headers))
    }

    @Test
    fun send_429WithRetryAfterHttpDate_givesNoDelay() {
        val transport = respondingWith(HttpStatusCode.TooManyRequests, "", "Retry-After" to HTTP_DATE)

        assertEquals(TransportResult.HttpError(429, null), transport.send(payload, headers))
    }

    @Test
    fun retryAfterMillis_acceptsOnlyNonNegativeIntegerSeconds() {
        assertEquals(0L, OtlpHttpTransport.retryAfterMillis("0"))
        assertEquals(120_000L, OtlpHttpTransport.retryAfterMillis(" 120 "))
        listOf(null, "", "-1", "1.5", "7s", HTTP_DATE, "99999999999999999999").forEach {
            assertEquals(it, null, OtlpHttpTransport.retryAfterMillis(it))
        }
    }

    @Test
    fun send_engineThrowsATimeout_isTimeout() {
        val timeouts = listOf(
            SocketTimeoutException("read timed out"),
            HttpRequestTimeoutException(ENDPOINT, TelemetryLimits.REQUEST_TIMEOUT_MILLIS),
            ConnectTimeoutException("connect timed out")
        )

        timeouts.forEach { error ->
            assertEquals(error.toString(), TransportResult.Timeout, throwing(error).send(payload, headers))
        }
    }

    @Test
    fun send_engineThrowsAnIOException_isConnectionFailure() {
        val transport = throwing(IOException("connection reset"))

        assertEquals(TransportResult.ConnectionFailure, transport.send(payload, headers))
    }

    @Test
    fun send_engineThrowsAnythingElse_isFailure() {
        assertEquals(TransportResult.Failure, throwing(IllegalStateException("boom")).send(payload, headers))
    }

    @Test
    fun send_apiParseException_isHttpErrorWithItsStatus() {
        val error = ApiParseException(status = 502, errorBody = null)

        assertEquals(TransportResult.HttpError(502, null), throwing(error).send(payload, headers))
    }

    @Test
    fun send_clientFactoryThrows_isFailure() {
        val transport = OtlpHttpTransport(ENDPOINT) { throw IllegalStateException("no client") }

        assertEquals(TransportResult.Failure, transport.send(payload, headers))
    }

    @Test
    fun clientFactory_isCalledOnceAndOnlyOnTheFirstSend() {
        val transport = respondingWith(HttpStatusCode.NoContent)
        assertEquals("construction must not build the client", 0, factoryCalls)

        transport.send(payload, headers)
        transport.send(payload, headers)

        assertEquals(1, factoryCalls)
        assertEquals(2, requests.size)
    }

    private companion object {
        const val BASE_URL = "https://otlp.invalid"
        const val ENDPOINT = "https://otlp.invalid/v1/logs"
        const val HTTP_DATE = "Wed, 21 Oct 2026 07:28:00 GMT"
    }
}

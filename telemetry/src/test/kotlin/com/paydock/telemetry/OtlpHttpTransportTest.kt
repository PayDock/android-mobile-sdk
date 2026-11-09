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
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.Instant

/**
 * Unit tests for [OtlpHttpTransport] on the networking library's client with a [MockEngine]: what goes out, how
 * each outcome maps to a [TransportResult], and how every response's `Date` header measures the clock offset (D20).
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
    private val clock = TelemetryTestFakes.FakeClock(DEVICE_NOW)
    private val clockOffset = ClockOffset(clock)

    private fun transport(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): OtlpHttpTransport {
        val engine = MockEngine { request ->
            requests += request
            handler(request)
        }
        return OtlpHttpTransport(ENDPOINT, clockOffset) {
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
        val transport = OtlpHttpTransport(ENDPOINT, clockOffset) { throw IllegalStateException("no client") }

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

    // ---- the clock offset (D20) ----

    @Test
    fun aSuccessWithADate_measuresTheOffset() {
        val transport = respondingWith(HttpStatusCode.NoContent, "", "Date" to SERVER_DATE)

        assertEquals(TransportResult.Success(204, 0), transport.send(payload, headers))

        assertEquals(SERVER_MILLIS - DEVICE_NOW, clockOffset.millis)
    }

    @Test
    fun everyErrorStatusWithADate_measuresTheOffsetToo() {
        listOf(HttpStatusCode.BadRequest, HttpStatusCode.TooManyRequests, HttpStatusCode.ServiceUnavailable)
            .forEachIndexed { index, status ->
                clock.nowMillis = DEVICE_NOW + index
                val transport = respondingWith(status, "", "Date" to SERVER_DATE)

                assertEquals(status.value, (transport.send(payload, headers) as TransportResult.HttpError).status)
                assertEquals(status.toString(), SERVER_MILLIS - DEVICE_NOW - index, clockOffset.millis)
            }
    }

    @Test
    fun eachResponse_replacesTheOffset() {
        respondingWith(HttpStatusCode.NoContent, "", "Date" to SERVER_DATE).send(payload, headers)

        clock.nowMillis = SERVER_MILLIS + 120_000
        respondingWith(HttpStatusCode.NoContent, "", "Date" to SERVER_DATE).send(payload, headers)

        assertEquals(-120_000L, clockOffset.millis)
    }

    @Test
    fun aResponseWithoutOrWithAnUnparsableDate_keepsTheOffset() {
        respondingWith(HttpStatusCode.NoContent, "", "Date" to SERVER_DATE).send(payload, headers)
        val measured = clockOffset.millis

        clock.nowMillis = DEVICE_NOW + 999_999
        respondingWith(HttpStatusCode.NoContent).send(payload, headers)
        respondingWith(HttpStatusCode.ServiceUnavailable, "", "Date" to "yesterday").send(payload, headers)
        respondingWith(HttpStatusCode.OK, "{}", "Date" to "").send(payload, headers)

        assertEquals(measured, clockOffset.millis)
    }

    /** Review fix (R3): the lenient parser reads year 9999; the offset ignores it, so no timestamp can overflow. */
    @Test
    fun aParsableButAbsurdDate_keepsTheOffset() {
        respondingWith(HttpStatusCode.NoContent, "", "Date" to SERVER_DATE).send(payload, headers)
        val measured = clockOffset.millis

        val absurd = "Fri, 31 Dec 9999 23:59:59 GMT"
        assertEquals(253_402_300_799_000L, OtlpHttpTransport.serverDateMillis(absurd))
        respondingWith(HttpStatusCode.NoContent, "", "Date" to absurd).send(payload, headers)
        respondingWith(HttpStatusCode.NoContent, "", "Date" to "Mon, 01 Jan 1990 00:00:00 GMT").send(payload, headers)

        assertEquals(measured, clockOffset.millis)
    }

    @Test
    fun aSendEndingInAnException_measuresNothing() {
        listOf(IOException("connection reset"), SocketTimeoutException("read timed out"), IllegalStateException("boom"))
            .forEach { throwing(it).send(payload, headers) }

        assertEquals(0L, clockOffset.millis)
    }

    @Test
    fun serverDateMillis_readsAnHttpDateAsUtcEpochMillis() {
        assertEquals(784_111_777_000L, OtlpHttpTransport.serverDateMillis("Sun, 06 Nov 1994 08:49:37 GMT"))
        listOf(HTTP_DATE, SERVER_DATE, "Thu, 29 Feb 2024 23:59:59 GMT", "Thu, 01 Jan 1970 00:00:00 GMT").forEach {
            assertEquals(it, instantOf(it), OtlpHttpTransport.serverDateMillis(it))
        }
    }

    @Test
    fun serverDateMillis_isNullWhenAbsentOrUnparsable() {
        listOf(null, "", " ", "7", "yesterday", "2026-11-05T04:14:12Z", "Thu, 05 Nov 2026").forEach {
            assertNull(it, OtlpHttpTransport.serverDateMillis(it))
        }
    }

    /** The reference reading of an IMF-fixdate, through java.time, which only the tests use. */
    private fun instantOf(httpDate: String): Long {
        val parts = httpDate.split(" ")
        val month = MONTHS.indexOf(parts[2]) + 1
        val iso = "%s-%02d-%sT%sZ".format(parts[3], month, parts[1], parts[4])
        return Instant.parse(iso).toEpochMilli()
    }

    private companion object {
        const val BASE_URL = "https://otlp.invalid"
        const val ENDPOINT = "https://otlp.invalid/v1/logs"
        const val HTTP_DATE = "Wed, 21 Oct 2026 07:28:00 GMT"
        const val SERVER_DATE = "Thu, 05 Nov 2026 04:14:12 GMT"
        const val SERVER_MILLIS = 1_793_852_052_000L // SERVER_DATE
        const val DEVICE_NOW = SERVER_MILLIS - 3_600_000L // the device clock an hour behind
        val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    }
}

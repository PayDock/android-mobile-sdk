package com.paydock.core.telemetry

import com.paydock.core.BaseUnitTest
import com.paydock.core.network.NetworkClientBuilder
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.exceptions.ApiParseException
import com.paydock.telemetry.AttributeValue
import com.paydock.telemetry.TelemetryEvent
import com.paydock.telemetry.TelemetryEvent.NetworkError
import com.paydock.telemetry.TelemetryEvent.TransportKind
import com.paydock.telemetry.TelemetryRecord
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.SocketTimeoutException

/**
 * The network failure plugin on the real OkHttp engine, with the networking library's ApiErrorInterceptor, against
 * [MockWebServer] (the ApiErrorInterceptorTest setup, HTTP): an API error body gives its codes, an unreadable 5xx body
 * gives its status, and a server that stops answering gives a timeout. The client's timeouts are 1 s, set through the
 * builder, so the timeout case is short.
 */
internal class NetworkFailurePluginOkHttpTest : BaseUnitTest() {

    private val mockServer = MockWebServer()
    private val telemetry = RecordingTelemetry()
    private lateinit var client: HttpClient

    @Before
    fun setUp() {
        mockServer.start()
        client = NetworkClientBuilder.create()
            .setProtocol(URLProtocol.HTTP)
            .setBaseUrl("paydock.com")
            .setRequestTimeout(SHORT_TIMEOUT_SECONDS)
            .setResponseTimeout(SHORT_TIMEOUT_SECONDS)
            .build()
            .withNetworkFailureReporting(telemetry)
    }

    @After
    fun tearDown() {
        client.close()
        mockServer.shutdown()
    }

    private val tokensUrl: String
        get() = "http://${mockServer.hostName}:${mockServer.port}/v1/payment_sources/tokens"

    private fun failure(): TelemetryEvent.NetworkFailure {
        assertEquals(telemetry.events.toString(), 1, telemetry.events.size)
        return telemetry.events.single() as TelemetryEvent.NetworkFailure
    }

    private fun postTokens(): Throwable? = runBlocking { runCatching { client.post(tokensUrl) }.exceptionOrNull() }

    @Test
    fun a401WithAnApiBody_isHttp4xx_withTheApiCodes() {
        mockServer.enqueue(MockResponse().setResponseCode(HttpStatusCode.Unauthorized.value).setBody(API_BODY_401))

        val thrown = postTokens()

        assertTrue(thrown.toString(), thrown is ApiException)
        val event = failure()
        assertEquals(NetworkError.HttpStatus(401), event.failure)
        assertEquals(
            TelemetryEvent.ApiError(401, "synthetic_auth_error", "synthetic_summary", listOf("G01", "G02")),
            event.apiError
        )
        assertEquals("/v1/payment_sources/tokens", event.urlTemplate)
        assertEquals(mockServer.hostName, event.serverAddress)
        assertEquals(TelemetryEvent.HttpMethod.POST, event.method)
        assertEquals(1, event.attempt)
        assertEquals("http_4xx", NetworkFailureTestData.storedRecord(event).stringValue("error.type"))
    }

    @Test
    fun a500WithAnUnreadableBody_isHttp5xx_withoutApiCodes() {
        mockServer.enqueue(
            MockResponse().setResponseCode(HttpStatusCode.InternalServerError.value).setBody("<html>synthetic</html>")
        )

        val thrown = postTokens()

        assertTrue(thrown.toString(), thrown is ApiParseException)
        val event = failure()
        assertEquals(NetworkError.HttpStatus(500), event.failure)
        assertNull(event.apiError)
        assertEquals("http_5xx", NetworkFailureTestData.storedRecord(event).stringValue("error.type"))
    }

    @Test
    fun aServerThatStopsAnswering_isATimeout() {
        mockServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val thrown = postTokens()

        assertTrue(thrown.toString(), thrown is SocketTimeoutException)
        val event = failure()
        val transport = event.failure as NetworkError.Transport
        assertEquals(TransportKind.TIMEOUT, transport.kind)
        assertNull(event.apiError)
        assertEquals("timeout", NetworkFailureTestData.storedRecord(event).stringValue("error.type"))
    }

    private fun TelemetryRecord.stringValue(key: String): String? = (value(key) as? AttributeValue.Str)?.value

    private companion object {
        const val SHORT_TIMEOUT_SECONDS = 1.0

        const val API_BODY_401 = """{"status":401,""" +
            """"error":{"message":"synthetic","code":"synthetic_auth_error",""" +
            """"details":{"gateway_specific_code":"G01","messages":["synthetic"]}},""" +
            """"resource":{"type":"token"},""" +
            """"error_summary":{"message":"synthetic","code":"synthetic_summary",""" +
            """"details":{"gateway_specific_code":"G02","messages":["synthetic"]}}}"""
    }
}

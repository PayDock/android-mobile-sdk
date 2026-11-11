package com.paydock.core.telemetry

import com.paydock.core.network.NetworkClientBuilder
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.exceptions.ApiParseException
import com.paydock.core.telemetry.NetworkFailureTestData.ACCESS_TOKEN
import com.paydock.core.telemetry.NetworkFailureTestData.EMAIL
import com.paydock.core.telemetry.NetworkFailureTestData.HEX_ID
import com.paydock.core.telemetry.NetworkFailureTestData.HOST
import com.paydock.core.telemetry.NetworkFailureTestData.apiBody
import com.paydock.feature.wallet.data.dto.WalletConfigResponse
import com.paydock.telemetry.TelemetryEvent
import com.paydock.telemetry.TelemetryEvent.NetworkError
import com.paydock.telemetry.TelemetryEvent.TransportKind
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.pluginOrNull
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.http.path
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

/**
 * Unit tests for the network failure plugin (R_01 to R_04, R_07) on a client built as the SDK builds its API client,
 * with Ktor's [MockEngine]: each failure gives exactly one [TelemetryEvent.NetworkFailure] with the right facts, the
 * request's own result or exception is unchanged, a success or a cancellation reports nothing, and no ID, query,
 * header or body reaches the event or its record. With MockEngine the networking library's OkHttp interceptor does
 * not run, so an error status reaches Ktor as it is (research § Current behavior 5).
 */
internal class NetworkFailurePluginTest {

    private val telemetry = RecordingTelemetry()
    private var handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = {
        respond("{}", HttpStatusCode.OK, jsonHeaders)
    }
    private val engine = MockEngine.create { addHandler { request -> handler(request) } } as MockEngine
    private val client: HttpClient = NetworkClientBuilder.create()
        .setBaseUrl(HOST)
        .setMockEngine(engine)
        .build()
        .withNetworkFailureReporting(telemetry)

    private val networkFailures: List<TelemetryEvent.NetworkFailure>
        get() = telemetry.events.filterIsInstance<TelemetryEvent.NetworkFailure>()

    /** The same client without the plugin: what the caller gets when telemetry is inactive. */
    private val plainClient: HttpClient = NetworkClientBuilder.create().setBaseUrl(HOST).setMockEngine(engine).build()

    @After
    fun tearDown() {
        client.close()
        plainClient.close()
    }

    private fun failure(): TelemetryEvent.NetworkFailure {
        assertEquals(telemetry.events.toString(), 1, telemetry.events.size)
        return networkFailures.single()
    }

    /** A POST to the card tokens endpoint, as CardRepositoryImpl sends it. */
    private suspend fun postTokens(): HttpResponse = client.post {
        headers { append("x-access-token", ACCESS_TOKEN) }
        url { path("/v1/payment_sources/tokens") }
        contentType(ContentType.Application.Json)
        setBody("{}")
    }

    /** A GET of a wallet config, as WalletRepositoryImpl sends it. */
    private suspend fun getWalletConfig(): HttpResponse = client.get {
        headers { append("x-access-token", ACCESS_TOKEN) }
        url { path("/v1/gateways/$HEX_ID/wallet-config") }
    }

    private inline fun <reified T : Throwable> thrownBy(noinline block: suspend () -> Unit): T {
        val thrown = runBlocking { runCatching { block() }.exceptionOrNull() }
        assertNotNull("nothing was thrown", thrown)
        assertTrue("${thrown!!.javaClass.name} is not ${T::class.java.name}", thrown is T)
        return thrown as T
    }

    private fun assertFacts(
        event: TelemetryEvent.NetworkFailure,
        failure: NetworkError,
        method: TelemetryEvent.HttpMethod = TelemetryEvent.HttpMethod.POST,
        template: String = "/v1/payment_sources/tokens",
    ) {
        assertEquals(method, event.method)
        assertEquals(template, event.urlTemplate)
        assertEquals(HOST, event.serverAddress)
        assertEquals(failure, event.failure)
        assertEquals(1, event.attempt)
        assertNull(event.widget)
    }

    // region HTTP errors

    @Test
    fun a400WithAnApiBody_isOneHttp4xx_andTheCallerGetsKtorsException() {
        handler = { respond(Json.encodeBody(), HttpStatusCode.BadRequest, jsonHeaders) }

        val thrown = thrownBy<ClientRequestException> { postTokens() }

        assertEquals(400, thrown.response.status.value)
        assertFacts(failure(), NetworkError.HttpStatus(400))
        assertNull(failure().apiError)
    }

    @Test
    fun a503WithAnEmptyBody_isOneHttp5xx() {
        handler = { respond("", HttpStatusCode.ServiceUnavailable) }

        val thrown = thrownBy<ServerResponseException> { getWalletConfig() }

        assertEquals(503, thrown.response.status.value)
        assertFacts(
            failure(),
            NetworkError.HttpStatus(503),
            method = TelemetryEvent.HttpMethod.GET,
            template = "/v1/gateways/{id}/wallet-config"
        )
    }

    // endregion

    // region exceptions from the engine

    @Test
    fun anApiException_isOneHttpStatus_withTheApiCodes_andIsRethrownUnchanged() {
        val original = ApiException(apiBody("synthetic", status = 401))
        handler = { throw original }

        val thrown = thrownBy<ApiException> { postTokens() }

        assertSame(original, thrown)
        assertFacts(failure(), NetworkError.HttpStatus(401))
        assertEquals(TelemetryEvent.ApiError(401, "card_declined", "declined", listOf("05", "N7")), failure().apiError)
    }

    @Test
    fun anApiParseException_isOneHttpStatus_andIsRethrownUnchanged() {
        val original = ApiParseException(status = 502, errorMessage = "synthetic", errorBody = "<html/>")
        handler = { throw original }

        val thrown = thrownBy<ApiParseException> { postTokens() }

        assertSame(original, thrown)
        assertFacts(failure(), NetworkError.HttpStatus(502))
        assertNull(failure().apiError)
    }

    @Test
    fun transportFailures_areOneTransportEach_andAreRethrownUnchanged() {
        listOf(
            SocketTimeoutException("synthetic") to TransportKind.TIMEOUT,
            ConnectTimeoutException("synthetic") to TransportKind.TIMEOUT,
            UnknownHostException("synthetic") to TransportKind.CONNECTION,
            ConnectException("synthetic") to TransportKind.CONNECTION,
            SSLHandshakeException("synthetic") to TransportKind.TLS,
        ).forEach { (original, kind) ->
            telemetry.events.clear()
            handler = { throw original }

            val thrown = thrownBy<Throwable> { postTokens() }
            val thrownWithoutPlugin =
                thrownBy<Throwable> { plainClient.post { url { path("/v1/payment_sources/tokens") } } }

            // The same instance, or the same coroutine stack-trace recovery copy a client without the plugin gives.
            assertSame(original.javaClass.name, original, thrown.unrecovered(original))
            assertSame(original.javaClass.name, original, thrownWithoutPlugin.unrecovered(original))
            assertEquals(original.javaClass.name, thrownWithoutPlugin === original, thrown === original)
            val event = failure()
            val transport = event.failure as NetworkError.Transport
            assertEquals(original.javaClass.name, kind, transport.kind)
            assertEquals(TelemetryEvent.ErrorInfo.from(original).type, transport.error?.type)
            assertFacts(event, event.failure)
            assertNull(event.apiError)
        }
    }

    // endregion

    // region decode, success, cancellation

    @Test
    fun a200WhoseBodyFailsToDecode_isOneDecode_andTheCallerGetsTheSameException() {
        handler = { respond("not json", HttpStatusCode.OK, jsonHeaders) }

        val thrown = thrownBy<JsonConvertException> { getWalletConfig().body<WalletConfigResponse>() }

        val event = failure()
        val decode = event.failure as NetworkError.Decode
        assertEquals(200, decode.statusCode)
        assertEquals(TelemetryEvent.ErrorInfo.from(thrown).type, decode.error?.type)
        assertFacts(event, decode, method = TelemetryEvent.HttpMethod.GET, template = "/v1/gateways/{id}/wallet-config")
    }

    @Test
    fun a200_reportsNothing_andReturnsTheResponse() = runBlocking {
        handler = { respond("""{"ok":true}""", HttpStatusCode.OK, jsonHeaders) }

        val response = postTokens()

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("""{"ok":true}""", response.bodyAsText())
        assertTrue(telemetry.events.isEmpty())
    }

    @Test
    fun aCancelledCall_reportsNothing() = runBlocking {
        val entered = CompletableDeferred<Unit>()
        handler = {
            entered.complete(Unit)
            awaitCancellation()
        }

        val call = async(Dispatchers.IO) { postTokens() }
        withTimeout(TIMEOUT_MILLIS) { entered.await() }
        call.cancel()
        runCatching { call.await() }

        assertTrue(call.isCancelled)
        assertTrue(telemetry.events.toString(), telemetry.events.isEmpty())
    }

    // endregion

    // region what reaches the event and the record

    @Test
    fun anIdPathQueryTokenAndCardInTheBody_reachNeitherTheEventNorItsRecord() {
        val card = NetworkFailureTestData.cardLikeNumber()
        handler = { respond("", HttpStatusCode.BadGateway) }

        thrownBy<ServerResponseException> {
            client.post {
                headers { append("x-access-token", ACCESS_TOKEN) }
                url {
                    path("/v1/charges/wallet/$HEX_ID/decline")
                    parameters.append("email", EMAIL)
                }
                contentType(ContentType.Application.Json)
                setBody("""{"card_number":"$card","email":"$EMAIL"}""")
            }
        }

        val event = failure()
        assertFacts(event, NetworkError.HttpStatus(502), template = "/v1/charges/wallet/{id}/decline")
        val record = NetworkFailureTestData.storedRecord(event)
        val texts = listOf(event.toString(), record.toString(), record.body)
        listOf(HEX_ID, EMAIL, "email", "?", ACCESS_TOKEN, card).forEach { planted ->
            texts.forEach { text -> assertFalse("$planted in $text", text.contains(planted)) }
        }
        assertTrue(record.body, record.body.contains("/v1/charges/wallet/{id}/decline"))
    }

    @Test
    fun theRetryCountHeader_givesTheAttempt() {
        handler = { respond("", HttpStatusCode.ServiceUnavailable) }

        thrownBy<ServerResponseException> {
            client.get {
                headers { append("x-retry-count", "2") }
                url { path("/v1/gateways/$HEX_ID/wallet-config") }
            }
        }

        assertEquals(3, failure().attempt)
    }

    @Test
    fun theInstalledClient_hasThePlugin_andAPlainClientDoesNot() {
        assertNotNull(client.pluginOrNull(NetworkFailureReporting))
        assertNull(plainClient.pluginOrNull(NetworkFailureReporting))
    }

    // endregion

    /**
     * The thrown exception as the caller sees it: the original itself, or, when kotlinx.coroutines' stack trace
     * recovery (debug mode, assertions on) copied it on the way up, the copy whose cause is the original.
     */
    private fun Throwable.unrecovered(original: Throwable): Throwable =
        if (this !== original && javaClass == original.javaClass && cause === original) original else this

    private object Json {
        fun encodeBody(): String =
            """{"status":400,"error":{"message":"synthetic","code":"invalid_request"},"error_summary":""" +
                """{"message":"synthetic","code":"validation_error"}}"""
    }

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
        val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    }
}

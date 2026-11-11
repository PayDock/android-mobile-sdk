package com.paydock.core.telemetry

import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.exceptions.ApiParseException
import com.paydock.core.telemetry.NetworkFailureTestData.EMAIL
import com.paydock.core.telemetry.NetworkFailureTestData.apiBody
import com.paydock.telemetry.TelemetryEvent
import com.paydock.telemetry.TelemetryEvent.NetworkError
import com.paydock.telemetry.TelemetryEvent.TransportKind
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.ContentConvertException
import io.ktor.serialization.JsonConvertException
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.CancellationException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException

/**
 * Unit tests for [NetworkFailureFacts] (R_02 to R_04): each branch of classify, in its order, the API codes, and that
 * no message, body or error body text reaches a classified failure. Every exception here is synthetic.
 */
internal class NetworkFailureFactsTest {

    private val card = NetworkFailureTestData.cardLikeNumber()
    private val planted = "charge $card for $EMAIL failed"

    private fun classified(throwable: Throwable, statusCode: Int? = null): NetworkFailureFacts.Classified {
        val result = NetworkFailureFacts.classify(throwable, statusCode)
        assertNotNull(throwable.javaClass.name, result)
        return result!!
    }

    private fun transportKind(throwable: Throwable): TransportKind? =
        (classified(throwable).failure as? NetworkError.Transport)?.kind

    // region ordering and branches

    @Test
    fun aCancellation_isNeverReported() {
        assertNull(NetworkFailureFacts.classify(CancellationException("synthetic")))
        assertNull(NetworkFailureFacts.classify(kotlinx.coroutines.CancellationException("synthetic"), 200))
    }

    @Test
    fun anApiException_isItsBodyStatus_withTheApiCodes() {
        val result = classified(ApiException(apiBody("synthetic", status = 401)))

        assertEquals(NetworkError.HttpStatus(401), result.failure)
        assertEquals(TelemetryEvent.ApiError(401, "card_declined", "declined", listOf("05", "N7")), result.apiError)
    }

    @Test
    fun anApiException_isNotAConnectionFailure_thoughItIsAnIOException() {
        val exception = ApiException(apiBody("synthetic"))
        assertTrue(exception is IOException)

        assertEquals(NetworkError.HttpStatus(402), classified(exception).failure)
    }

    @Test
    fun anApiException_readsCodesLikeSdkErrorFacts_blankCodesNull_gatewayCodesDistinct() {
        val blank = classified(ApiException(apiBody("synthetic", code = " ", errorGateway = "", summaryGateway = null)))
        assertEquals(TelemetryEvent.ApiError(402, null, "declined", emptyList()), blank.apiError)

        val same = classified(ApiException(apiBody("synthetic", errorGateway = "05", summaryGateway = "05")))
        assertEquals(listOf("05"), same.apiError?.gatewayCodes)

        val noError = classified(ApiException(apiBody("synthetic", code = null)))
        assertEquals(TelemetryEvent.ApiError(402, null, "declined", listOf("N7")), noError.apiError)

        val body = apiBody("synthetic", status = 422)
        assertEquals(SdkErrorFacts.apiError(ApiException(body), null), classified(ApiException(body)).apiError)
    }

    @Test
    fun anApiParseException_isItsStatus_withoutApiCodes() {
        val result = classified(ApiParseException(status = 500, errorMessage = "synthetic", errorBody = "<html/>"))

        assertEquals(NetworkError.HttpStatus(500), result.failure)
        assertNull(result.apiError)
    }

    @Test
    fun aResponseException_isTheResponseStatus_withoutApiCodes() {
        val response = mockk<HttpResponse>(relaxed = true)
        every { response.status } returns HttpStatusCode.ServiceUnavailable

        val result = classified(ResponseException(response, "synthetic"))

        assertEquals(NetworkError.HttpStatus(503), result.failure)
        assertNull(result.apiError)
    }

    @Test
    fun aDecodeFailure_isDecode_withTheGivenStatus_andTheErrorInfo() {
        val response = mockk<HttpResponse>(relaxed = true)
        val failures = listOf(
            JsonConvertException("synthetic"),
            ContentConvertException("synthetic"),
            SerializationException("synthetic"),
            NoTransformationFoundException(response, String::class, Int::class),
        )
        failures.forEach { failure ->
            val result = classified(failure, statusCode = 200)
            assertEquals(
                failure.javaClass.name,
                NetworkError.Decode(200, TelemetryEvent.ErrorInfo.from(failure)),
                result.failure
            )
            assertNull(result.apiError)
        }
        assertEquals(NetworkError.Decode(null, TelemetryEvent.ErrorInfo.from(failures[0])), classified(failures[0]).failure)
    }

    @Test
    fun timeouts_areTimeouts_aConnectTimeoutIncluded() {
        val connectTimeout = ConnectTimeoutException("synthetic")
        assertTrue(connectTimeout is ConnectException)

        assertEquals(TransportKind.TIMEOUT, transportKind(connectTimeout))
        assertEquals(TransportKind.TIMEOUT, transportKind(SocketTimeoutException("synthetic")))
        assertEquals(TransportKind.TIMEOUT, transportKind(HttpRequestTimeoutException("https://$EMAIL", 1_000L)))
    }

    @Test
    fun tlsFailures_areTls() {
        assertEquals(TransportKind.TLS, transportKind(SSLHandshakeException("synthetic")))
        assertEquals(TransportKind.TLS, transportKind(SSLException("synthetic")))
    }

    @Test
    fun connectionFailures_areConnection() {
        listOf(
            UnknownHostException("synthetic"),
            ConnectException("synthetic"),
            NoRouteToHostException("synthetic"),
            SocketException("synthetic"),
        ).forEach { assertEquals(it.javaClass.name, TransportKind.CONNECTION, transportKind(it)) }
    }

    @Test
    fun anythingElse_isUnknown() {
        assertEquals(TransportKind.UNKNOWN, transportKind(IOException("synthetic")))
        assertEquals(TransportKind.UNKNOWN, transportKind(IllegalStateException("synthetic")))
    }

    @Test
    fun aTransportFailure_carriesTheErrorInfoOfTheThrowable() {
        val failure = SocketTimeoutException("synthetic").apply { initCause(IOException("synthetic cause")) }

        val result = classified(failure)

        assertEquals(NetworkError.Transport(TransportKind.TIMEOUT, TelemetryEvent.ErrorInfo.from(failure)), result.failure)
        assertNull(result.apiError)
    }

    @Test
    fun forStatus_isTheStatus_withoutApiCodes() {
        assertEquals(NetworkFailureFacts.Classified(NetworkError.HttpStatus(503), null), NetworkFailureFacts.forStatus(503))
        assertEquals(NetworkFailureFacts.Classified(NetworkError.HttpStatus(404), null), NetworkFailureFacts.forStatus(404))
    }

    // endregion

    // region no text

    @Test
    fun plantedText_inTheBodyErrorBodyOrMessages_reachesNoField() {
        val response = mockk<HttpResponse>(relaxed = true)
        every { response.status } returns HttpStatusCode.BadRequest
        val failures = listOf(
            ApiException(apiBody(planted)),
            ApiParseException(status = 400, errorMessage = planted, errorBody = """{"number":"$card","email":"$EMAIL"}"""),
            ResponseException(response, planted),
            JsonConvertException(planted, IOException(planted)),
            SocketTimeoutException(planted),
            ConnectTimeoutException(planted, IOException(planted)),
            SSLHandshakeException(planted),
            UnknownHostException(planted),
            IllegalStateException(planted),
        )
        failures.forEach { failure ->
            val text = classified(failure, statusCode = 200).toString()
            assertFalse("${failure.javaClass.name}: $text", text.contains(card))
            assertFalse("${failure.javaClass.name}: $text", text.contains(EMAIL))
            assertFalse("${failure.javaClass.name}: $text", text.contains("charge"))
        }
    }

    // endregion
}

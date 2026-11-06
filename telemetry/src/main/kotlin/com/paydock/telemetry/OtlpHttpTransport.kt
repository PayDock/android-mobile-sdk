package com.paydock.telemetry

import com.paydock.core.network.exceptions.ApiParseException
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.header
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.isSuccess
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * OTLP/HTTP to [endpoint] (spec § 8): one blocking POST per [send], within [TelemetryLimits.REQUEST_TIMEOUT_MILLIS]
 * overall, whatever the client's per-operation timeouts and connection retries do. The payload and headers go out
 * unchanged: the exporter gzips and sets the headers, never this class. The client is built by [clientFactory] on
 * the first send, never at construction. The response is streamed, never saved whole, so an error body, already
 * emptied by [DropErrorBodyInterceptor], is never read at all.
 *
 * A 2xx body is read, up to [MAX_BODY_BYTES], only for `partialSuccess.rejectedLogRecords`; no other body is read
 * and `partialSuccess.errorMessage` is never kept. [send] never throws.
 */
internal class OtlpHttpTransport(
    private val endpoint: String,
    clientFactory: () -> HttpClient, // called once, on the first send
) : TelemetryTransport {

    private val client: HttpClient by lazy(clientFactory)

    @Suppress("TooGenericExceptionCaught") // every throwable maps to a result: send never throws
    override fun send(payload: ByteArray, headers: Map<String, String>): TransportResult = try {
        val requestHeaders = headers
        runBlocking {
            withTimeout(TelemetryLimits.REQUEST_TIMEOUT_MILLIS) {
                client.preparePost(endpoint) {
                    expectSuccess = false
                    requestHeaders.forEach { (name, value) -> header(name, value) }
                    setBody(ByteArrayContent(payload, ContentType.Application.Json))
                }.execute(::result)
            }
        }
    } catch (error: Throwable) {
        failure(error)
    }

    private suspend fun result(response: HttpResponse): TransportResult {
        val status = response.status.value
        return if (response.status.isSuccess()) {
            TransportResult.Success(status, rejectedRecords(response))
        } else {
            TransportResult.HttpError(status, retryAfterMillis(response.headers[HttpHeaders.RetryAfter]))
        }
    }

    /** `partialSuccess.rejectedLogRecords`, a JSON string or number; 0 for an empty body or anything unparsable. */
    @Suppress("SwallowedException") // an unreadable body counts no rejection; nothing of it is kept
    private suspend fun rejectedRecords(response: HttpResponse): Long = try {
        val text = response.bodyAsChannel().readRemaining(MAX_BODY_BYTES).readByteArray().toString(Charsets.UTF_8)
        if (text.isBlank()) {
            0L
        } else {
            val partialSuccess = (Json.parseToJsonElement(text) as? JsonObject)?.get(PARTIAL_SUCCESS) as? JsonObject
            val rejected = partialSuccess?.get(REJECTED_LOG_RECORDS) as? JsonPrimitive
            rejected?.content?.toLongOrNull()?.takeIf { it >= 0 } ?: 0L
        }
    } catch (ignored: Exception) {
        0L
    }

    private fun failure(error: Throwable): TransportResult = when (error) {
        is TimeoutCancellationException,
        is HttpRequestTimeoutException,
        is SocketTimeoutException,
        is ConnectTimeoutException -> TransportResult.Timeout
        is ApiParseException -> TransportResult.HttpError(error.status, null)
        is IOException -> TransportResult.ConnectionFailure
        else -> TransportResult.Failure
    }

    companion object {
        const val MAX_BODY_BYTES = 65_536L

        private const val PARTIAL_SUCCESS = "partialSuccess"
        private const val REJECTED_LOG_RECORDS = "rejectedLogRecords"
        private const val MILLIS_PER_SECOND = 1_000L

        /** Non-negative integer seconds, times 1000; any other form, an HTTP-date included, is null. */
        fun retryAfterMillis(value: String?): Long? {
            val seconds = value?.trim()?.takeIf { it.isNotEmpty() && it.all(Char::isDigit) }?.toLongOrNull()
            return seconds?.takeIf { it <= Long.MAX_VALUE / MILLIS_PER_SECOND }?.let { it * MILLIS_PER_SECOND }
        }
    }
}

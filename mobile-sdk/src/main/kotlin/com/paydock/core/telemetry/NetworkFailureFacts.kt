package com.paydock.core.telemetry

import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.exceptions.ApiParseException
import com.paydock.telemetry.TelemetryEvent
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.ContentConvertException
import io.ktor.serialization.JsonConvertException
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.CancellationException
import javax.net.ssl.SSLException

/** What a failed call says, read from types, statuses and API codes only: never a body, header or message (R_02–R_04). */
internal object NetworkFailureFacts {

    /** A classified failure: the event's failure and the API codes (null when the error carries no API body). */
    data class Classified(val failure: TelemetryEvent.NetworkError, val apiError: TelemetryEvent.ApiError?)

    /**
     * Classifies a throwable raised by the API client, checked in this order:
     * - a java.util.concurrent.CancellationException → null (never reported);
     * - com.paydock.core.network.exceptions.ApiException → HttpStatus(error.status), apiError from its body
     *   (status, error?.code, summary.code, the distinct non-blank gateway codes of error.details and
     *   summary.details; blank codes become null; the same reading as SdkErrorFacts);
     * - com.paydock.core.network.exceptions.ApiParseException → HttpStatus(status), no apiError (errorBody is never
     *   read);
     * - io.ktor.client.plugins.ResponseException → HttpStatus(response.status.value), no apiError;
     * - a decode failure (io.ktor.serialization.JsonConvertException, ContentConvertException,
     *   kotlinx.serialization.SerializationException, io.ktor.client.call.NoTransformationFoundException) →
     *   Decode(statusCode, ErrorInfo.from(throwable)), statusCode as given;
     * - java.net.SocketTimeoutException, io.ktor.client.network.sockets.ConnectTimeoutException,
     *   io.ktor.client.plugins.HttpRequestTimeoutException → Transport(TIMEOUT, ErrorInfo.from(throwable)), checked
     *   before ConnectException because ConnectTimeoutException extends it;
     * - javax.net.ssl.SSLException → Transport(TLS, …);
     * - java.net.UnknownHostException, java.net.ConnectException, java.net.NoRouteToHostException,
     *   java.net.SocketException → Transport(CONNECTION, …);
     * - any other throwable → Transport(UNKNOWN, ErrorInfo.from(throwable)).
     * Never throws.
     */
    @Suppress("CyclomaticComplexMethod") // one branch per failure kind, in the documented order
    fun classify(throwable: Throwable, statusCode: Int? = null): Classified? = runCatching {
        when (throwable) {
            is CancellationException -> null
            is ApiException -> Classified(
                TelemetryEvent.NetworkError.HttpStatus(throwable.error.status),
                SdkErrorFacts.toApiError(throwable.error)
            )
            is ApiParseException -> forStatus(throwable.status)
            is ResponseException -> forStatus(throwable.response.status.value)
            is JsonConvertException,
            is ContentConvertException,
            is SerializationException,
            is NoTransformationFoundException -> Classified(
                TelemetryEvent.NetworkError.Decode(statusCode, TelemetryEvent.ErrorInfo.from(throwable)),
                null
            )
            is SocketTimeoutException,
            is ConnectTimeoutException,
            is HttpRequestTimeoutException -> transport(TelemetryEvent.TransportKind.TIMEOUT, throwable)
            is SSLException -> transport(TelemetryEvent.TransportKind.TLS, throwable)
            is UnknownHostException,
            is ConnectException,
            is NoRouteToHostException,
            is SocketException -> transport(TelemetryEvent.TransportKind.CONNECTION, throwable)
            else -> transport(TelemetryEvent.TransportKind.UNKNOWN, throwable)
        }
    }.getOrElse { transport(TelemetryEvent.TransportKind.UNKNOWN, throwable) }

    /** HttpStatus(code), no apiError: an error status that reached Ktor with a blank body. */
    fun forStatus(code: Int): Classified = Classified(TelemetryEvent.NetworkError.HttpStatus(code), null)

    /** ErrorInfo.from never throws, so neither does this. */
    private fun transport(kind: TelemetryEvent.TransportKind, throwable: Throwable): Classified =
        Classified(TelemetryEvent.NetworkError.Transport(kind, TelemetryEvent.ErrorInfo.from(throwable)), null)
}

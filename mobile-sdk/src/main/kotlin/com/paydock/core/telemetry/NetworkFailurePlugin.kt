package com.paydock.core.telemetry

import com.paydock.telemetry.Telemetry
import com.paydock.telemetry.TelemetryEvent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpMethod
import io.ktor.http.encodedPath
import io.ktor.http.isSuccess
import io.ktor.util.AttributeKey
import io.ktor.util.Attributes

/*
 * The network failure plugin on the SDK's Paydock API client (R_01 to R_04, R_07): one send hook for every exception
 * and every error status, one validation hook for a body that fails to decode. Each failed call is reported at most
 * once, from types, statuses, the path template and API codes only; the request's outcome is never changed.
 */

/** The request header the networking library's retry sets: the attempt is its value + 1. */
private const val RETRY_COUNT_HEADER = "x-retry-count"

/** Set on a request once the send hook has reported it, so the validation hook never reports it again. */
private val ReportedKey = AttributeKey<Unit>("PaydockNetworkFailureReported")

/** The plugin's one setting: the facade it reports to. */
internal class NetworkFailureReportingConfig {
    var telemetry: Telemetry? = null
}

/**
 * Reports one TelemetryEvent.NetworkFailure per failed call (R_01–R_04, R_07), with
 * method = HttpMethod by name (a method outside the enum reports nothing), urlTemplate = RouteTemplates.template(
 * request.url.encodedPath), serverAddress = request.url.host, attempt = the "x-retry-count" request header + 1, or
 * 1 when absent, widget = null.
 * - on(Send): proceed; a response whose status is not 2xx reports forStatus(status) and the call is returned
 *   unchanged; a throwable reports classify(throwable) and is rethrown unchanged.
 * Reporting runs inside runCatching and never changes the outcome.
 */
internal val NetworkFailureReporting: ClientPlugin<NetworkFailureReportingConfig> =
    createClientPlugin("NetworkFailureReporting", ::NetworkFailureReportingConfig) {
        val telemetry = pluginConfig.telemetry
        on(Send) { request ->
            val call = runCatching { proceed(request) }.getOrElse { throwable ->
                runCatching {
                    NetworkFailureFacts.classify(throwable)?.let { classified ->
                        request.attributes.put(ReportedKey, Unit)
                        telemetry?.reportNetworkFailure(
                            request.method,
                            request.url.encodedPath,
                            request.url.host,
                            request.headers[RETRY_COUNT_HEADER],
                            classified
                        )
                    }
                }
                throw throwable
            }
            runCatching {
                val status = call.response.status
                if (!status.isSuccess()) {
                    request.attributes.put(ReportedKey, Unit)
                    telemetry?.reportNetworkFailure(
                        request.method,
                        request.url.encodedPath,
                        request.url.host,
                        request.headers[RETRY_COUNT_HEADER],
                        NetworkFailureFacts.forStatus(status.value)
                    )
                }
            }
            call
        }
    }

/**
 * The guarded install (R_07): config { install(NetworkFailureReporting) { this.telemetry = telemetry };
 * HttpResponseValidator { handleResponseExceptionWithRequest { cause, request -> report only a decode failure:
 * classify(cause, request.call.response.status.value) when it gives Decode } } }. Returns the new client.
 */
internal fun HttpClient.withNetworkFailureReporting(telemetry: Telemetry): HttpClient = config {
    install(NetworkFailureReporting) { this.telemetry = telemetry }
    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, request ->
            runCatching {
                if (request.attributes.alreadyReported()) return@runCatching
                val status = runCatching { request.call.response.status.value }.getOrNull()
                val classified = NetworkFailureFacts.classify(cause, status)
                if (classified?.failure is TelemetryEvent.NetworkError.Decode) {
                    telemetry.reportNetworkFailure(
                        request.method,
                        request.url.encodedPath,
                        request.url.host,
                        request.headers[RETRY_COUNT_HEADER],
                        classified
                    )
                }
            }
        }
    }
}

private fun Attributes.alreadyReported(): Boolean = contains(ReportedKey)

/** Builds and reports the event; a method outside [TelemetryEvent.HttpMethod] reports nothing. */
private fun Telemetry.reportNetworkFailure(
    method: HttpMethod,
    encodedPath: String,
    host: String,
    retryCount: String?,
    classified: NetworkFailureFacts.Classified,
) {
    if (!isActive) return
    val eventMethod = TelemetryEvent.HttpMethod.entries.firstOrNull { it.name == method.value } ?: return
    report(
        TelemetryEvent.NetworkFailure(
            method = eventMethod,
            urlTemplate = RouteTemplates.template(encodedPath),
            serverAddress = host,
            failure = classified.failure,
            attempt = retryCount?.trim()?.toIntOrNull()?.takeIf { it >= 0 }?.plus(1) ?: 1,
            apiError = classified.apiError,
        )
    )
}

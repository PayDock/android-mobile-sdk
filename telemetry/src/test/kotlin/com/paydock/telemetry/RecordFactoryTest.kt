package com.paydock.telemetry

import com.paydock.telemetry.AttributeValue.Bool
import com.paydock.telemetry.AttributeValue.Num
import com.paydock.telemetry.AttributeValue.Str
import com.paydock.telemetry.TelemetryEvent.CrashKind
import com.paydock.telemetry.TelemetryEvent.CrashSource
import com.paydock.telemetry.TelemetryEvent.ErrorInfo
import com.paydock.telemetry.TelemetryEvent.HttpMethod
import com.paydock.telemetry.TelemetryEvent.NetworkError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [RecordFactory] (R_01, R_17), beyond what the golden fixtures cover: severities, the record time,
 * the in-SDK check, the network variants and the bodies. Events are synthetic and carry no message text.
 */
internal class RecordFactoryTest {

    private val sdkFrame = "com.paydock.feature.card.CardRepository.tokenise(CardRepository.kt:42)"
    private val appFrame = "com.merchant.shop.Checkout.pay(Checkout.kt:9)"

    private fun create(event: TelemetryEvent, connection: NetworkConnectionType = NetworkConnectionType.CELL) =
        RecordFactory.create(PendingRecord("$SESSION_ID-4", SESSION_ID, event, createdAt = CREATED_AT), connection)

    private fun network(failure: NetworkError, apiError: TelemetryEvent.ApiError? = null) =
        TelemetryEvent.NetworkFailure(
            method = HttpMethod.GET,
            urlTemplate = "/v1/charges/{id}",
            serverAddress = "api-sandbox.paydock.com",
            failure = failure,
            attempt = 3,
            apiError = apiError
        )

    private fun crash(kind: CrashKind, error: ErrorInfo? = null, reason: String? = null, occurredAt: Long? = null) =
        TelemetryEvent.Crash(error, CrashSource.EXIT_INFO, kind, reason = reason, occurredAt = occurredAt)

    @Test
    fun everyRecord_carriesItsIdsConnectionAndEventName() {
        val record = create(TelemetryEvent.WidgetOpened("CardDetailsWidget"))

        assertEquals("$SESSION_ID-4", record.id)
        assertEquals(
            listOf(
                Attribute("event.name", Str("paydock.widget.opened")),
                Attribute("session.id", Str(SESSION_ID)),
                Attribute("log.record.uid", Str("$SESSION_ID-4")),
                Attribute("network.connection.type", Str("cell")),
                Attribute("paydock.widget", Str("CardDetailsWidget"))
            ),
            record.attributes
        )
        assertEquals(Severity.INFO, record.severity)
        assertEquals("paydock.widget.opened · CardDetailsWidget", record.body)
    }

    @Test
    fun time_isTheCrashsOwnTimeElseCreatedAt() {
        assertEquals(OCCURRED_AT, create(crash(CrashKind.OS_KILL, occurredAt = OCCURRED_AT)).timeUnixMillis)
        assertEquals(CREATED_AT, create(crash(CrashKind.OS_KILL)).timeUnixMillis)
        assertEquals(CREATED_AT, create(TelemetryEvent.WidgetOpened("CardDetailsWidget")).timeUnixMillis)
    }

    @Test
    fun severity_isFatalForEveryCrashKind_errorForErrorsAndFailures_warnForDropped() {
        CrashKind.entries.forEach { kind -> assertEquals(kind.name, Severity.FATAL, create(crash(kind)).severity) }
        val error = ErrorInfo("java.lang.IllegalStateException", listOf(sdkFrame))
        assertEquals(Severity.ERROR, create(TelemetryEvent.SdkError(error)).severity)
        assertEquals(Severity.ERROR, create(TelemetryEvent.UncaughtException(error)).severity)
        assertEquals(Severity.ERROR, create(network(NetworkError.HttpStatus(500))).severity)
        assertEquals(
            Severity.WARN,
            create(TelemetryEvent.Dropped(TelemetryEvent.DroppedReason.STORE_FULL, 2)).severity
        )
    }

    @Test
    fun inSdk_isTrueForAnSdkFrameAnywhereInTheChain_andFalseWithoutAnError() {
        val causeInSdk = ErrorInfo("Outer", listOf(appFrame), cause = ErrorInfo("Inner", listOf(sdkFrame)))
        val outsideSdk = ErrorInfo("Outer", listOf(appFrame))

        assertEquals(Bool(true), create(TelemetryEvent.SdkError(causeInSdk)).value("paydock.exception.in_sdk"))
        assertEquals(Bool(false), create(TelemetryEvent.SdkError(outsideSdk)).value("paydock.exception.in_sdk"))
        assertEquals(Bool(false), create(crash(CrashKind.OS_KILL)).value("paydock.exception.in_sdk"))
    }

    @Test
    fun crash_omitsAReasonEqualToTheKind_andFallsBackToTheExceptionType() {
        val anr = create(crash(CrashKind.ANR, ErrorInfo(null, listOf(sdkFrame)), reason = "ANR"))
        val native = create(crash(CrashKind.NATIVE, reason = "CRASH_NATIVE"))
        val typed = create(crash(CrashKind.EXCEPTION, ErrorInfo("java.lang.Error", emptyList())))

        assertEquals("paydock.crash · anr", anr.body)
        assertNull(anr.value("exception.type"))
        assertEquals(Str("\tat $sdkFrame"), anr.value("exception.stacktrace"))
        assertEquals("paydock.crash · native · CRASH_NATIVE", native.body)
        assertEquals(Str("CRASH_NATIVE"), native.value("paydock.crash.reason"))
        assertEquals(Str("exit_info"), native.value("paydock.crash.source"))
        assertNull(native.value("exception.stacktrace"))
        assertEquals("paydock.crash · exception · java.lang.Error", typed.body)
    }

    @Test
    fun errors_carryTheirKindCodeWidgetAndThread() {
        val error = ErrorInfo("java.lang.IllegalStateException", listOf(sdkFrame))

        val uncaught = create(TelemetryEvent.UncaughtException(error, threadName = "worker-1", widget = "ZipWidget"))
        val sdkError = create(TelemetryEvent.SdkError(error, errorCode = "TokenisingCardException"))
        val bare = create(TelemetryEvent.SdkError(ErrorInfo(type = null, frames = emptyList())))

        assertEquals(Str("uncaught"), uncaught.value("paydock.error.kind"))
        assertEquals(Str("worker-1"), uncaught.value("thread.name"))
        assertEquals(Str("ZipWidget"), uncaught.value("paydock.widget"))
        assertEquals("paydock.error · uncaught · java.lang.IllegalStateException", uncaught.body)
        assertEquals(Str("sdk_error"), sdkError.value("paydock.error.kind"))
        assertEquals(Str("TokenisingCardException"), sdkError.value("paydock.error.code"))
        assertEquals("paydock.error · sdk_error · TokenisingCardException", sdkError.body)
        assertEquals("paydock.error · sdk_error", bare.body)
    }

    @Test
    fun sdkError_withAnApiError_carriesTheFourApiKeys_inAllowlistOrder() {
        val error = ErrorInfo("com.paydock.core.domain.error.exceptions.TokenisingException", listOf(sdkFrame))
        val apiError = TelemetryEvent.ApiError(
            status = 400,
            code = "invalid_request",
            summaryCode = "validation_error",
            gatewayCodes = listOf("05", "N7")
        )

        val record = create(
            TelemetryEvent.SdkError(error, errorCode = "TokenisingCardException", widget = "CardDetailsWidget", apiError)
        )
        val without = create(TelemetryEvent.SdkError(error, "TokenisingCardException", widget = "CardDetailsWidget"))

        assertEquals(Num(400), record.value("paydock.api.status"))
        assertEquals(Str("invalid_request"), record.value("paydock.api.error.code"))
        assertEquals(Str("validation_error"), record.value("paydock.api.error.summary_code"))
        assertEquals(Str("05, N7"), record.value("paydock.api.gateway.codes"))
        val order = Allowlist.RECORD_KEYS.keys.toList()
        val keys = record.attributes.map { it.key }
        assertEquals(keys.sortedBy { order.indexOf(it) }, keys)
        assertEquals(keys.distinct(), keys)
        assertEquals(
            listOf(
                "paydock.api.status",
                "paydock.api.error.code",
                "paydock.api.error.summary_code",
                "paydock.api.gateway.codes"
            ),
            keys.filter { it.startsWith("paydock.api.") }
        )
        assertEquals("paydock.error · sdk_error · TokenisingCardException", record.body)
        assertEquals(without.body, record.body)
        assertEquals(emptyList<String>(), without.attributes.map { it.key }.filter { it.startsWith("paydock.api.") })
        assertEquals(without.attributes, record.attributes.filterNot { it.key.startsWith("paydock.api.") })
    }

    @Test
    fun httpFailure_carriesStatusErrorTypeApiFieldsAndGroupKey() {
        val record = create(
            network(
                NetworkError.HttpStatus(404),
                TelemetryEvent.ApiError(status = 404, code = "not_found", gatewayCodes = listOf("A", "B"))
            )
        )

        assertEquals(Num(404), record.value("http.response.status_code"))
        assertEquals(Str("http_4xx"), record.value("error.type"))
        assertEquals(Num(404), record.value("paydock.api.status"))
        assertEquals(Str("not_found"), record.value("paydock.api.error.code"))
        assertNull(record.value("paydock.api.error.summary_code"))
        assertEquals(Str("A, B"), record.value("paydock.api.gateway.codes"))
        assertEquals(Num(3), record.value("paydock.http.attempt"))
        assertEquals(Str("http GET /v1/charges/{id} 404"), record.value("paydock.group.key"))
        assertEquals("paydock.network.failure · http_4xx 404 · GET /v1/charges/{id}", record.body)
        assertEquals(Str("http_5xx"), create(network(NetworkError.HttpStatus(500))).value("error.type"))
    }

    @Test
    fun transportFailure_carriesItsKindAndException_withoutStatusOrGroupKey() {
        val error = ErrorInfo("javax.net.ssl.SSLHandshakeException", listOf("okhttp3.RealCall.execute(RealCall.kt:1)"))

        val record = create(network(NetworkError.Transport(TelemetryEvent.TransportKind.TLS, error)))

        assertEquals(Str("tls"), record.value("error.type"))
        assertEquals(Str("javax.net.ssl.SSLHandshakeException"), record.value("exception.type"))
        assertEquals(
            Str("javax.net.ssl.SSLHandshakeException\n\tat okhttp3.RealCall.execute(RealCall.kt:1)"),
            record.value("exception.stacktrace")
        )
        assertNull(record.value("http.response.status_code"))
        assertNull(record.value("paydock.group.key"))
        assertNull(record.value("paydock.exception.in_sdk"))
        assertEquals("paydock.network.failure · tls · GET /v1/charges/{id}", record.body)
    }

    @Test
    fun decodeFailure_carriesItsStatusInTheAttributesAndTheBody_whenItHasOne() {
        val withStatus = create(network(NetworkError.Decode(statusCode = 200)))
        val withoutStatus = create(network(NetworkError.Decode()))

        assertEquals(Num(200), withStatus.value("http.response.status_code"))
        assertEquals(Str("decode"), withStatus.value("error.type"))
        assertNull(withStatus.value("paydock.group.key"))
        assertEquals("paydock.network.failure · decode 200 · GET /v1/charges/{id}", withStatus.body)
        assertNull(withoutStatus.value("http.response.status_code"))
        assertEquals("paydock.network.failure · decode · GET /v1/charges/{id}", withoutStatus.body)
    }

    @Test
    fun dropped_carriesReasonAndCount() {
        val record = create(TelemetryEvent.Dropped(TelemetryEvent.DroppedReason.TOO_OLD, 12))

        assertEquals(Str("too_old"), record.value("paydock.dropped.reason"))
        assertEquals(Num(12), record.value("paydock.dropped.count"))
        assertEquals("paydock.telemetry.dropped · too_old · 12 records", record.body)
        assertEquals(true, record.isDroppedNotice)
    }

    @Test
    fun attributes_followTheAllowlistOrder_eachKeyOnce() {
        val error = ErrorInfo("Outer", listOf(sdkFrame), cause = ErrorInfo("Inner", emptyList()))
        val record = create(TelemetryEvent.UncaughtException(error, threadName = "main", widget = "CardDetailsWidget"))

        val order = Allowlist.RECORD_KEYS.keys.toList()
        val keys = record.attributes.map { it.key }
        assertEquals(keys.sortedBy { order.indexOf(it) }, keys)
        assertEquals(keys.distinct(), keys)
        assertFalse(keys.any { it !in order })
        assertEquals(Str("Inner"), record.value("paydock.exception.cause_types"))
    }

    @Test
    fun create_isDeterministic() {
        val event = network(NetworkError.HttpStatus(503), TelemetryEvent.ApiError(status = 503))

        assertEquals(create(event), create(event))
    }

    private companion object {
        const val SESSION_ID = "b2f6c0a4-1d3e-4e8f-9a71-5c2d8e0f4b13"
        const val CREATED_AT = 1_793_852_047_512L
        const val OCCURRED_AT = 1_793_851_121_000L
    }
}

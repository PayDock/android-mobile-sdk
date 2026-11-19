package com.paydock.telemetry

import com.paydock.telemetry.AttributeValue.Bool
import com.paydock.telemetry.AttributeValue.Num
import com.paydock.telemetry.AttributeValue.Str
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [RecordBody], the one body rule (spec § 5.2): each record type's body read from its attributes
 * alone, absent details left out. Values are synthetic.
 */
internal class RecordBodyTest {

    private fun body(vararg attributes: Pair<String, AttributeValue>): String =
        RecordBody.of(attributes.map { (key, value) -> Attribute(key, value) })

    // ---- crash ----

    @Test
    fun crash_isTheKindThenTheReason() {
        assertEquals(
            "paydock.crash · native · CRASH_NATIVE",
            body(
                "event.name" to Str("paydock.crash"),
                "exception.type" to Str("java.lang.Error"),
                "paydock.crash.kind" to Str("native"),
                "paydock.crash.reason" to Str("CRASH_NATIVE")
            )
        )
    }

    @Test
    fun crash_fallsBackToTheExceptionType_andOmitsADetailEqualToTheKind() {
        assertEquals(
            "paydock.crash · exception · java.lang.Error",
            body(
                "event.name" to Str("paydock.crash"),
                "exception.type" to Str("java.lang.Error"),
                "paydock.crash.kind" to Str("exception")
            )
        )
        assertEquals(
            "paydock.crash · anr",
            body(
                "event.name" to Str("paydock.crash"),
                "paydock.crash.kind" to Str("anr"),
                "paydock.crash.reason" to Str("ANR")
            )
        )
    }

    @Test
    fun crash_withoutReasonOrType_isTheKindAlone_andWithoutAnyDetail_theNameAlone() {
        val crash = "event.name" to Str("paydock.crash")

        assertEquals("paydock.crash · os_kill", body(crash, "paydock.crash.kind" to Str("os_kill")))
        assertEquals("paydock.crash · CRASH_NATIVE", body(crash, "paydock.crash.reason" to Str("CRASH_NATIVE")))
        assertEquals("paydock.crash", body(crash))
    }

    // ---- error ----

    @Test
    fun error_isTheKindThenTheCodeElseTheExceptionType() {
        assertEquals(
            "paydock.error · sdk_error · TokenisingCardException",
            body(
                "event.name" to Str("paydock.error"),
                "exception.type" to Str("java.lang.IllegalStateException"),
                "paydock.error.kind" to Str("sdk_error"),
                "paydock.error.code" to Str("TokenisingCardException")
            )
        )
        assertEquals(
            "paydock.error · uncaught · java.lang.IllegalStateException",
            body(
                "event.name" to Str("paydock.error"),
                "exception.type" to Str("java.lang.IllegalStateException"),
                "paydock.error.kind" to Str("uncaught")
            )
        )
    }

    @Test
    fun error_withoutCodeOrType_isTheKindAlone() {
        assertEquals(
            "paydock.error · sdk_error",
            body("event.name" to Str("paydock.error"), "paydock.error.kind" to Str("sdk_error"))
        )
    }

    // ---- network failure ----

    @Test
    fun networkFailure_isTheErrorTypeAndStatus_thenTheMethodAndTemplate() {
        assertEquals(
            "paydock.network.failure · http_4xx 404 · GET /v1/charges/{id}",
            body(
                "event.name" to Str("paydock.network.failure"),
                "http.request.method" to Str("GET"),
                "url.template" to Str("/v1/charges/{id}"),
                "http.response.status_code" to Num(404),
                "error.type" to Str("http_4xx")
            )
        )
        assertEquals(
            "paydock.network.failure · tls · POST /v1/charges",
            body(
                "event.name" to Str("paydock.network.failure"),
                "http.request.method" to Str("POST"),
                "url.template" to Str("/v1/charges"),
                "error.type" to Str("tls")
            )
        )
    }

    @Test
    fun networkFailure_leavesOutTheRequestDetail_whenTheMethodOrTheTemplateIsAbsent() {
        assertEquals(
            "paydock.network.failure · http_5xx 503",
            body(
                "event.name" to Str("paydock.network.failure"),
                "http.request.method" to Str("GET"),
                "http.response.status_code" to Num(503),
                "error.type" to Str("http_5xx")
            )
        )
        assertEquals(
            "paydock.network.failure · decode",
            body(
                "event.name" to Str("paydock.network.failure"),
                "url.template" to Str("/v1/charges"),
                "error.type" to Str("decode")
            )
        )
    }

    // ---- widget opened, dropped notice ----

    @Test
    fun widgetOpened_isTheWidget() {
        assertEquals(
            "paydock.widget.opened · CardDetailsWidget",
            body("event.name" to Str("paydock.widget.opened"), "paydock.widget" to Str("CardDetailsWidget"))
        )
        assertEquals("paydock.widget.opened", body("event.name" to Str("paydock.widget.opened")))
    }

    @Test
    fun paymentStarted_isTheWidget() {
        assertEquals(
            "paydock.payment.started · PayPalWidget",
            body("event.name" to Str("paydock.payment.started"), "paydock.widget" to Str("PayPalWidget"))
        )
        assertEquals("paydock.payment.started", body("event.name" to Str("paydock.payment.started")))
    }

    @Test
    fun paymentFinished_isTheWidgetThenTheOutcome_thenTheErrorCodeWhenPresent() {
        assertEquals(
            "paydock.payment.finished · Standalone3DSWidget · declined",
            body(
                "event.name" to Str("paydock.payment.finished"),
                "paydock.widget" to Str("Standalone3DSWidget"),
                "paydock.payment.outcome" to Str("declined"),
                "paydock.payment.duration_ms" to Num(18_342)
            )
        )
        assertEquals(
            "paydock.payment.finished · Standalone3DSWidget · error · CHARGE_ERROR",
            body(
                "event.name" to Str("paydock.payment.finished"),
                "paydock.widget" to Str("Standalone3DSWidget"),
                "paydock.error.code" to Str("CHARGE_ERROR"),
                "paydock.payment.outcome" to Str("error")
            )
        )
        assertEquals(
            "paydock.payment.finished · error",
            body("event.name" to Str("paydock.payment.finished"), "paydock.payment.outcome" to Str("error"))
        )
    }

    @Test
    fun droppedNotice_isTheReasonThenTheCount() {
        assertEquals(
            "paydock.telemetry.dropped · too_old · 12 records",
            body(
                "event.name" to Str("paydock.telemetry.dropped"),
                "paydock.dropped.reason" to Str("too_old"),
                "paydock.dropped.count" to Num(12)
            )
        )
        assertEquals(
            "paydock.telemetry.dropped · 12 records",
            body("event.name" to Str("paydock.telemetry.dropped"), "paydock.dropped.count" to Num(12))
        )
    }

    // ---- names ----

    @Test
    fun anUnknownEventName_isTheNameAlone() {
        assertEquals(
            "paydock.unknown",
            body("event.name" to Str("paydock.unknown"), "paydock.widget" to Str("CardDetailsWidget"))
        )
    }

    @Test
    fun noEventName_isBlank() {
        assertEquals("", body())
        assertEquals("", body("paydock.widget" to Str("CardDetailsWidget"), "paydock.dropped.count" to Num(3)))
        assertEquals("", body("event.name" to Bool(true)))
    }

    @Test
    fun aValueOfTheWrongType_isAbsent() {
        assertEquals(
            "paydock.telemetry.dropped · too_old",
            body(
                "event.name" to Str("paydock.telemetry.dropped"),
                "paydock.dropped.reason" to Str("too_old"),
                "paydock.dropped.count" to Str("12")
            )
        )
    }
}

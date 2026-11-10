package com.paydock.core.telemetry

import com.paydock.core.domain.error.exceptions.CardDetailsException
import com.paydock.core.domain.error.exceptions.GooglePayException
import com.paydock.core.domain.error.exceptions.PayPalException
import com.paydock.core.network.dto.error.ApiError
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorDetails
import com.paydock.core.network.dto.error.ErrorMessage
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.dto.error.ErrorSummaryDetails
import com.paydock.core.network.exceptions.ApiException
import com.paydock.telemetry.Telemetry
import com.paydock.telemetry.TelemetryEvent
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the SDK's telemetry calls (R_01 to R_03, R_06, R_08): the event each call reports, the inactive
 * facade, a failing facade, and planted card-like numbers that never reach an event. The number is computed at run
 * time, so no card-shaped literal is committed.
 */
internal class SdkTelemetryTest {

    private val card = cardLikeNumber()

    private fun body(text: String) = ApiErrorResponse(
        status = 402,
        error = ApiError(
            message = text,
            code = "card_declined",
            details = ErrorDetails(
                gatewaySpecificCode = "05",
                gatewaySpecificDescription = text,
                paramName = text,
                description = text,
                path = text,
                messages = listOf(
                    ErrorMessage.StringMessage(text),
                    ErrorMessage.GatewayError(
                        gatewaySpecificCode = "05",
                        gatewaySpecificDescription = text,
                        description = text,
                        statusCode = "05",
                        statusCodeDescription = text
                    )
                ),
                statusCode = "05",
                statusCodeDescription = text
            )
        ),
        summary = ErrorSummary(
            message = text,
            code = "declined",
            statusCode = "05",
            statusCodeDescription = text,
            details = ErrorSummaryDetails(
                gatewaySpecificCode = "N7",
                gatewaySpecificDescription = text,
                messages = listOf(text),
                description = text,
                statusCode = "05",
                statusCodeDescription = text
            )
        )
    )

    @Test
    fun reportSdkError_reportsOneSdkError_withTheFacts() {
        val telemetry = RecordingTelemetry()
        val original = ApiException(body("synthetic"))
        val failure = CardDetailsException.TokenisingCardException(body("synthetic"))

        telemetry.reportSdkError(failure, SdkWidget.CARD_DETAILS, cause = original)

        assertEquals(
            listOf(
                TelemetryEvent.SdkError(
                    error = SdkErrorFacts.errorInfo(failure, original),
                    errorCode = "TokenisingCardException",
                    widget = "CardDetailsWidget",
                    apiError = TelemetryEvent.ApiError(
                        status = 402,
                        code = "card_declined",
                        summaryCode = "declined",
                        gatewayCodes = listOf("05", "N7")
                    )
                )
            ),
            telemetry.events
        )
        assertEquals(listOf(ApiException::class.java.name), telemetry.sdkErrors.single().error.causeTypes)
    }

    @Test
    fun reportSdkError_withoutWidgetOrCause_reportsTheThrowableAlone() {
        val telemetry = RecordingTelemetry()
        val failure = IllegalStateException()

        telemetry.reportSdkError(failure, widget = null)

        val event = telemetry.sdkErrors.single()
        assertEquals(TelemetryEvent.ErrorInfo.from(failure), event.error)
        assertEquals(TelemetryEvent.SdkError(error = event.error), event)
    }

    @Test
    fun reportWidgetOpened_reportsOneWidgetOpened() {
        val telemetry = RecordingTelemetry()

        telemetry.reportWidgetOpened(SdkWidget.ZIP)

        assertEquals(listOf(TelemetryEvent.WidgetOpened("ZipWidget")), telemetry.events)
        assertEquals(1, telemetry.widgetsOpened.size)
    }

    @Test
    fun theInactiveFacade_isNeverCalled() {
        val telemetry = RecordingTelemetry(isActive = false)

        telemetry.reportSdkError(CardDetailsException.TokenisingCardException(body("synthetic")), SdkWidget.CARD_DETAILS)
        telemetry.reportSdkError(IllegalStateException(), widget = null, cause = IllegalArgumentException())
        telemetry.reportWidgetOpened(SdkWidget.CARD_DETAILS)

        assertTrue(telemetry.events.isEmpty())
    }

    @Test
    fun aCoroutineCancellation_buildsNothing() {
        val telemetry = RecordingTelemetry()

        telemetry.reportSdkError(kotlin.coroutines.cancellation.CancellationException(), SdkWidget.ADDRESS_DETAILS)
        telemetry.reportSdkError(
            java.util.concurrent.CancellationException(),
            widget = null,
            cause = IllegalStateException()
        )

        assertTrue(telemetry.events.isEmpty())
    }

    @Test
    fun aCancellationWrappedAsAnSdkExceptionCause_reportsNothing() {
        val telemetry = RecordingTelemetry()
        val cancellation = kotlin.coroutines.cancellation.CancellationException()

        // B: the view model maps the cancellation and hands it in as the cause (PP2, AF3, CP1 and others).
        telemetry.reportSdkError(PayPalException.UnknownException("synthetic"), SdkWidget.PAYPAL, cause = cancellation)
        // The cancellation deeper along the cause's chain.
        telemetry.reportSdkError(
            PayPalException.UnknownException("synthetic"),
            SdkWidget.PAYPAL,
            cause = IllegalStateException(java.io.IOException(cancellation))
        )
        // The cancellation along the reported throwable's own chain.
        telemetry.reportSdkError(IllegalStateException(java.io.IOException(cancellation)), SdkWidget.CARD_DETAILS)

        assertTrue(telemetry.events.isEmpty())
    }

    @Test
    fun reportSdkErrorWhileActive_reportsFromAnActiveCoroutine() = runBlocking<Unit> {
        val telemetry = RecordingTelemetry()

        telemetry.reportSdkErrorWhileActive(
            PayPalException.UnknownException("synthetic"),
            SdkWidget.PAYPAL,
            cause = IllegalStateException()
        )

        val event = telemetry.sdkErrors.single()
        assertEquals("UnknownException", event.errorCode)
        assertEquals(SdkWidget.PAYPAL, event.widget)
        assertEquals(IllegalStateException::class.java.name, event.error.cause?.type)
    }

    @Test
    fun reportSdkErrorWhileActive_fromACancelledCoroutine_reportsNothing() = runBlocking<Unit> {
        val telemetry = RecordingTelemetry()

        launch {
            // A mapper has already turned the cancellation into an SdkException without a cause: only the
            // coroutine's own state still tells.
            cancel()
            telemetry.reportSdkErrorWhileActive(CardDetailsException.UnknownException("synthetic"), SdkWidget.CARD_DETAILS)
        }.join()

        assertTrue(telemetry.events.isEmpty())
    }

    @Test
    fun anSdkCancellationException_stillReports() {
        val telemetry = RecordingTelemetry()

        telemetry.reportSdkError(PayPalException.CancellationException("synthetic"), SdkWidget.PAYPAL)

        val event = telemetry.sdkErrors.single()
        assertEquals("CancellationException", event.errorCode)
        assertEquals(SdkWidget.PAYPAL, event.widget)
        assertEquals(PayPalException.CancellationException::class.java.name, event.error.type)
    }

    @Test
    fun aFailingFacade_neverThrowsToTheCaller() {
        val throwingReport = object : Telemetry {
            override val isActive: Boolean = true
            override fun report(event: TelemetryEvent) = error("synthetic facade failure")
        }
        val throwingIsActive = object : Telemetry {
            override val isActive: Boolean
                get() = error("synthetic facade failure")
            override fun report(event: TelemetryEvent) = Unit
        }

        listOf(throwingReport, throwingIsActive).forEach { telemetry ->
            telemetry.reportSdkError(IllegalStateException(), SdkWidget.ZIP, cause = IllegalArgumentException())
            telemetry.reportWidgetOpened(SdkWidget.ZIP)
        }
    }

    @Test
    fun aCardLikeNumber_inAnExceptionMessage_orAnyApiMessageField_reachesNoEventField() {
        val telemetry = RecordingTelemetry()
        val planted = "declined for $card"

        // B: a merchant-facing exception built from an original, each carrying the number in its messages.
        telemetry.reportSdkError(
            GooglePayException.ResultException(planted),
            SdkWidget.GOOGLE_PAY,
            cause = GooglePayException.TokenisingGooglePayException(body(planted))
        )
        // A: an SdkException holding an API body whose every message field carries the number.
        telemetry.reportSdkError(CardDetailsException.TokenisingCardException(body(planted)), SdkWidget.CARD_DETAILS)
        // An ApiException deep in a raw throwable's chain, and a parse error carrying the number in its body text.
        telemetry.reportSdkError(IllegalStateException(planted, ApiException(body(planted))), SdkWidget.CARD_DETAILS)
        telemetry.reportSdkError(CardDetailsException.ParseException(planted, errorBody = planted), widget = null)

        assertEquals(4, telemetry.sdkErrors.size)
        assertTrue(telemetry.sdkErrors.all { it.apiError != null || it.errorCode == "ParseException" })
        telemetry.events.forEach { event ->
            val text = event.toString()
            assertFalse("a planted number reached an event: ${event::class.simpleName}", text.contains(card))
            assertFalse("planted text reached an event", text.contains("declined for"))
        }
    }

    /**
     * A card-like number: 16 digits from a non-issuer prefix that pass Luhn, computed at run time so no card-shaped
     * literal is committed (as `TelemetryTestFakes.cardLikeNumber()` in the telemetry module).
     */
    private fun cardLikeNumber(): String {
        val payload = "990000000000123"
        val sum = payload.reversed().mapIndexed { index, char ->
            val digit = char - '0'
            if (index % 2 == 0) (digit * 2).let { if (it > 9) it - 9 else it } else digit
        }.sum()
        return payload + (10 - sum % 10) % 10
    }
}

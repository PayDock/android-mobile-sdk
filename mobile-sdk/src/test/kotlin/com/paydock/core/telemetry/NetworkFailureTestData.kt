package com.paydock.core.telemetry

import com.paydock.core.network.dto.error.ApiError
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorDetails
import com.paydock.core.network.dto.error.ErrorMessage
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.dto.error.ErrorSummaryDetails
import com.paydock.telemetry.Allowlist
import com.paydock.telemetry.NetworkConnectionType
import com.paydock.telemetry.PendingRecord
import com.paydock.telemetry.RecordFactory
import com.paydock.telemetry.TelemetryEvent
import com.paydock.telemetry.TelemetryRecord

/**
 * Synthetic values for the network failure tests. Every ID and token is obviously synthetic; the card-like number is
 * computed at run time, so no card-shaped literal is committed (as `TelemetryTestFakes.cardLikeNumber()` does in the
 * telemetry module).
 */
internal object NetworkFailureTestData {

    /** The API host of the SANDBOX environment: a bare host, as the SDK's base URL is. */
    const val HOST = "api-sandbox.paydock.com"

    /** A 24-character hex ID, shaped like a Paydock resource ID. */
    const val HEX_ID = "0000aaaa1111bbbb2222cccc"

    /** A token-like path value, shaped like a setup token. */
    const val TOKEN_LIKE = "SYNTH3TIC0SETUP0TOKEN7"

    /** A synthetic access token for the x-access-token header. */
    const val ACCESS_TOKEN = "synthetic-access-token-0001"

    const val EMAIL = "someone@example.com"

    /** 16 digits from a non-issuer prefix that pass Luhn. */
    fun cardLikeNumber(): String {
        val payload = "990000000000123"
        val sum = payload.reversed().mapIndexed { index, char ->
            val digit = char - '0'
            if (index % 2 == 0) (digit * 2).let { if (it > 9) it - 9 else it } else digit
        }.sum()
        return payload + (10 - sum % 10) % 10
    }

    /** An API error body carrying [text] in every message field, and codes only in the code fields; no error without [code]. */
    fun apiBody(
        text: String,
        status: Int = 402,
        code: String? = "card_declined",
        summaryCode: String = "declined",
        errorGateway: String? = "05",
        summaryGateway: String? = "N7",
    ) = ApiErrorResponse(
        status = status,
        error = code?.let {
            ApiError(
                message = text,
                code = it,
                details = ErrorDetails(
                    gatewaySpecificCode = errorGateway,
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
            )
        },
        summary = ErrorSummary(
            message = text,
            code = summaryCode,
            statusCode = "05",
            statusCodeDescription = text,
            details = ErrorSummaryDetails(
                gatewaySpecificCode = summaryGateway,
                gatewaySpecificDescription = text,
                messages = listOf(text),
                description = text,
                statusCode = "05",
                statusCodeDescription = text
            )
        )
    )

    /** The record the telemetry module stores for [event]: built by RecordFactory, then filtered by the allowlist. */
    fun storedRecord(event: TelemetryEvent): TelemetryRecord = Allowlist.filter(
        RecordFactory.create(
            PendingRecord("$SESSION_ID-1", SESSION_ID, event, createdAt = T0),
            NetworkConnectionType.WIFI
        )
    )

    private const val SESSION_ID = "00000000-0000-4000-8000-000000000101"
    private const val T0 = 1_793_852_047_512L
}

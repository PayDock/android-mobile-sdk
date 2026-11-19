package com.paydock.telemetry

import com.paydock.telemetry.TelemetryTestFakes.FakeTransport
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.zip.GZIPInputStream

/**
 * Unit tests for [OtlpExporter]: the gzipped body, the two headers (never an `Authorization` header, D22) and the
 * status mapping (spec § 8).
 */
internal class OtlpExporterTest {

    private val batch = ExportBatch(
        TelemetryTestFakes.resourceAttributes,
        listOf(TelemetryTestFakes.record(1), TelemetryTestFakes.record(2, event = TelemetryTestFakes.crash()))
    )

    private fun exportWith(result: TransportResult): ExportResult = OtlpExporter(FakeTransport(result)).export(batch)

    @Test
    fun export_sendsTheGzippedEncodedBatch_withExactlyTheTwoHeaders() {
        val transport = FakeTransport()

        OtlpExporter(transport).export(batch)

        val request = transport.sent.single()
        assertArrayEquals(
            OtlpLogEncoder.encode(batch.resource, batch.records),
            GZIPInputStream(request.payload.inputStream()).use { it.readBytes() }
        )
        assertEquals(mapOf("Content-Encoding" to "gzip", "Content-Type" to "application/json"), request.headers)
    }

    @Test
    fun export_neverSendsAnAuthorizationHeader_whateverTheResponse() {
        val results = listOf(
            TransportResult.Success(200),
            TransportResult.Success(200, rejectedRecords = 1),
            TransportResult.HttpError(400, null),
            TransportResult.HttpError(401, null),
            TransportResult.HttpError(413, null),
            TransportResult.HttpError(429, 1_000),
            TransportResult.HttpError(503, null),
            TransportResult.Timeout,
            TransportResult.ConnectionFailure,
            TransportResult.Failure
        )
        results.forEach { result ->
            val transport = FakeTransport(result)

            OtlpExporter(transport).export(batch)
            OtlpExporter(transport).export(batch)

            transport.sent.forEach { request ->
                assertFalse(
                    "result=$result",
                    request.headers.keys.any { it.equals("Authorization", ignoreCase = true) }
                )
            }
            assertEquals(2, transport.sent.size)
        }
    }

    @Test
    fun success_mapsToSuccess_withThePartialSuccessCount() {
        assertEquals(ExportResult.Success(0), exportWith(TransportResult.Success(204)))
        assertEquals(ExportResult.Success(0), exportWith(TransportResult.Success(200)))
        assertEquals(ExportResult.Success(3), exportWith(TransportResult.Success(200, rejectedRecords = 3)))
    }

    @Test
    fun unauthorizedAndForbidden_mapToUnauthorized() {
        assertEquals(ExportResult.Unauthorized, exportWith(TransportResult.HttpError(401, null)))
        assertEquals(ExportResult.Unauthorized, exportWith(TransportResult.HttpError(403, 7_000)))
    }

    @Test
    fun payloadTooLarge_mapsToTooLarge() {
        assertEquals(ExportResult.TooLarge, exportWith(TransportResult.HttpError(413, null)))
    }

    @Test
    fun requestTimeoutTooManyRequestsAndServerErrors_mapToRetry_withRetryAfter() {
        assertEquals(ExportResult.Retry(null), exportWith(TransportResult.HttpError(408, null)))
        assertEquals(ExportResult.Retry(7_000), exportWith(TransportResult.HttpError(429, 7_000)))
        assertEquals(ExportResult.Retry(null), exportWith(TransportResult.HttpError(500, null)))
        assertEquals(ExportResult.Retry(2_000), exportWith(TransportResult.HttpError(503, 2_000)))
        assertEquals(ExportResult.Retry(null), exportWith(TransportResult.HttpError(599, null)))
    }

    @Test
    fun tooManyRequestsAndGatewayErrors_mapToRetry() {
        listOf(429, 502, 503, 504).forEach { status ->
            assertEquals("$status", ExportResult.Retry(null), exportWith(TransportResult.HttpError(status, null)))
            assertEquals("$status", ExportResult.Retry(3_000), exportWith(TransportResult.HttpError(status, 3_000)))
        }
    }

    @Test
    fun badRequest_mapsToRejected() {
        assertEquals(ExportResult.Rejected, exportWith(TransportResult.HttpError(400, null)))
        assertEquals(ExportResult.Rejected, exportWith(TransportResult.HttpError(400, 5_000)))
    }

    @Test
    fun anyOtherStatus_mapsToRejected() {
        listOf(300, 400, 404, 409, 422, 600).forEach { status ->
            assertEquals("$status", ExportResult.Rejected, exportWith(TransportResult.HttpError(status, null)))
        }
    }

    @Test
    fun transportFailures_mapToRetryWithoutRetryAfter() {
        assertEquals(ExportResult.Retry(null), exportWith(TransportResult.Timeout))
        assertEquals(ExportResult.Retry(null), exportWith(TransportResult.ConnectionFailure))
        assertEquals(ExportResult.Retry(null), exportWith(TransportResult.Failure))
    }

    @Test
    fun aThrowingTransport_isARetry_neverAThrow() {
        val throwing = object : TelemetryTransport {
            override fun send(payload: ByteArray, headers: Map<String, String>): TransportResult =
                error("synthetic transport failure")
        }

        assertEquals(ExportResult.Retry(null), OtlpExporter(throwing).export(batch))
    }
}

package com.paydock.telemetry

import com.paydock.core.network.NetworkClientBuilder
import com.paydock.telemetry.TelemetryTestFakes.FakeClock
import com.paydock.telemetry.TelemetryTestFakes.FakeDeviceContextProvider
import com.paydock.telemetry.TelemetryTestFakes.FakeMonotonic
import com.paydock.telemetry.TelemetryTestFakes.InMemoryStore
import com.paydock.telemetry.TelemetryTestFakes.SentRequest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.headersOf
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

/**
 * The clock offset end to end (D20, R_08): one [ClockOffset] shared by a real [OtlpHttpTransport] on a [MockEngine]
 * and the [ExportRunner]. Flush 1 goes out uncorrected and measures the response's `Date`; flush 2 sends corrected
 * times.
 */
internal class ClockOffsetWiringTest {

    private val deviceClock = FakeClock(DEVICE_NOW)
    private val clockOffset = ClockOffset(deviceClock)
    private val store = InMemoryStore()
    private val sent = mutableListOf<SentRequest>()

    private val transport = OtlpHttpTransport(TelemetryTestFakes.ENDPOINT, clockOffset) {
        val engine = MockEngine { request ->
            sent += SentRequest((request.body as ByteArrayContent).bytes(), emptyMap(), Thread.currentThread().name)
            respond("", HttpStatusCode.OK, headersOf("Date", SERVER_DATE))
        }
        NetworkClientBuilder.create().setBaseUrl(BASE_URL).setMockEngine(engine).build()
    }

    private val runner = ExportRunner(
        store = store,
        exporter = OtlpExporter(transport),
        deviceContext = FakeDeviceContextProvider(),
        clockOffset = clockOffset,
        monotonic = FakeMonotonic(),
        retryPolicy = RetryPolicy(Random(7)),
        scheduleRetry = {}
    )

    private fun record(sequence: Long, createdAt: Long) = TelemetryTestFakes.record(
        sequence,
        event = TelemetryTestFakes.sdkError("test.Wired$sequence"),
        createdAt = createdAt
    ).also(store::save)

    private fun SentRequest.times(): List<String> = logRecords.map { it.getValue("timeUnixNano").jsonPrimitive.content }

    private fun nanos(millis: Long): String = (millis * 1_000_000).toString()

    @Test
    fun theOffsetMeasuredOnFlush1_correctsTheTimesSentOnFlush2() {
        val first = record(1, createdAt = DEVICE_NOW - 1_000)

        runner.flush(DEVICE_NOW, TelemetryLimits.BATCH_MAX_RECORDS)

        assertEquals(listOf(nanos(first.timeUnixMillis)), sent.single().times())
        assertEquals(SERVER_MILLIS - DEVICE_NOW, clockOffset.millis)

        deviceClock.nowMillis = DEVICE_NOW + 2_000
        val second = record(2, createdAt = DEVICE_NOW + 1_500)
        runner.flush(deviceClock.nowMillis, TelemetryLimits.BATCH_MAX_RECORDS)

        assertEquals(2, sent.size)
        assertEquals(listOf(nanos(second.timeUnixMillis + OFFSET)), sent[1].times())
        assertEquals(listOf(null), sent[1].recordValues(SendStamp.OCCURRED_AT))
        assertEquals(listOf(second.id), sent[1].recordIds())
        assertEquals(0, store.count())
    }

    @Test
    fun aLateRecordOnFlush2_carriesTheCorrectedSendTimeAndOccurredAt() {
        record(1, createdAt = DEVICE_NOW - 1_000)
        runner.flush(DEVICE_NOW, TelemetryLimits.BATCH_MAX_RECORDS)

        val late = record(2, createdAt = DEVICE_NOW - TelemetryLimits.ORDERING_WINDOW_MILLIS - 1)
        runner.flush(DEVICE_NOW, TelemetryLimits.BATCH_MAX_RECORDS)

        assertEquals(listOf(nanos(DEVICE_NOW + OFFSET)), sent[1].times())
        assertEquals(
            listOf(UtcTimestamp.iso8601(late.timeUnixMillis + OFFSET)),
            sent[1].recordValues(SendStamp.OCCURRED_AT)
        )
    }

    private companion object {
        const val BASE_URL = "https://otlp.invalid"
        const val SERVER_DATE = "Thu, 05 Nov 2026 04:14:12 GMT"
        const val SERVER_MILLIS = 1_793_852_052_000L // SERVER_DATE
        const val OFFSET = 3_600_000L
        const val DEVICE_NOW = SERVER_MILLIS - OFFSET // the device clock an hour behind
    }
}

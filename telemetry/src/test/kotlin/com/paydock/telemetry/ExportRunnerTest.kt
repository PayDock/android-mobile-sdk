package com.paydock.telemetry

import com.paydock.telemetry.AttributeValue.Str
import com.paydock.telemetry.TelemetryLimits.BATCH_MAX_BYTES
import com.paydock.telemetry.TelemetryLimits.BATCH_MAX_RECORDS
import com.paydock.telemetry.TelemetryLimits.RECORD_MAX_AGE_MILLIS
import com.paydock.telemetry.TelemetryLimits.RETRY_MAX_ATTEMPTS
import com.paydock.telemetry.TelemetryTestFakes.FakeDeviceContextProvider
import com.paydock.telemetry.TelemetryTestFakes.FakeMonotonic
import com.paydock.telemetry.TelemetryTestFakes.FakeTransport
import com.paydock.telemetry.TelemetryTestFakes.InMemoryStore
import com.paydock.telemetry.TelemetryTestFakes.SESSION_ID
import com.paydock.telemetry.TelemetryTestFakes.T0
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.zip.GZIPInputStream
import kotlin.random.Random

/**
 * Unit tests for [ExportRunner] (R_10, R_12, R_13, R_18): a fake store, a fake transport behind the real
 * [OtlpExporter], a fixed clock, a fake monotonic clock and a seeded random. Nothing sleeps: a retry is a recorded
 * delay, and time moves by setting the monotonic clock.
 */
internal class ExportRunnerTest {

    private val now = T0 + 1_000
    private val store = InMemoryStore()
    private val monotonic = FakeMonotonic(10_000)
    private val retries = mutableListOf<Long>()

    private fun runner(
        transport: TelemetryTransport,
        deviceContext: DeviceContextProvider = FakeDeviceContextProvider(),
    ) = ExportRunner(
        store = store,
        exporter = OtlpExporter(transport, TelemetryTestFakes.credentials),
        deviceContext = deviceContext,
        monotonic = monotonic,
        retryPolicy = RetryPolicy(Random(3)),
        scheduleRetry = { retries += it }
    )

    /** Each sequence its own error type, so the duplicate gate never merges two of them. */
    private fun record(sequence: Long, sessionId: String = SESSION_ID, createdAt: Long = T0 + sequence) =
        TelemetryTestFakes.record(sequence, sessionId, TelemetryTestFakes.sdkError("test.Error$sequence"), createdAt)

    private fun saveRecords(range: LongRange): List<TelemetryRecord> = range.map { record(it).also(store::save) }

    private fun ids(range: LongRange): List<String> = range.map { "$SESSION_ID-$it" }

    private fun FakeTransport.sentIds(): List<List<String?>> = sent.map { it.recordIds() }

    /** One retryable answer: the records stay, one retry is scheduled, and a flush before it sends nothing. */
    private fun assertRetried(answer: TransportResult, expectedDelay: LongRange) {
        val records = saveRecords(1L..3L)
        val transport = FakeTransport(answer)
        val runner = runner(transport)

        val report = runner.flush(now, BATCH_MAX_RECORDS)

        assertEquals(FlushReport(pending = 3), report)
        assertEquals(records, store.oldest(10))
        assertEquals(1, retries.size)
        assertTrue("${retries.single()} in $expectedDelay", retries.single() in expectedDelay)

        monotonic.elapsedMillis += retries.single() - 1
        assertEquals(FlushReport(pending = 3), runner.flush(now, BATCH_MAX_RECORDS))
        assertEquals(1, transport.sent.size)

        monotonic.elapsedMillis += 1
        assertEquals(FlushReport(pending = 0), runner.flush(now, BATCH_MAX_RECORDS))
        assertEquals(listOf(ids(1L..3L), ids(1L..3L)), transport.sentIds())
    }

    @Test
    fun aNetworkError_isRetriedLater() {
        assertRetried(TransportResult.ConnectionFailure, 2_500L..5_000L)
    }

    @Test
    fun aTimeout_isRetriedLater() {
        assertRetried(TransportResult.Timeout, 2_500L..5_000L)
    }

    @Test
    fun a408_isRetriedLater() {
        assertRetried(TransportResult.HttpError(408, null), 2_500L..5_000L)
    }

    @Test
    fun a429_isRetriedAfterItsRetryAfter() {
        assertRetried(TransportResult.HttpError(429, 7_000), 7_000L..7_000L)
    }

    @Test
    fun a5xx_isRetriedLater() {
        assertRetried(TransportResult.HttpError(503, null), 2_500L..5_000L)
    }

    @Test
    fun a401_stopsSendingForTheProcess_andKeepsTheRecords() {
        val records = saveRecords(1L..3L)
        val transport = FakeTransport(TransportResult.HttpError(401, null))
        val runner = runner(transport)

        val report = runner.flush(now, BATCH_MAX_RECORDS)

        assertEquals(FlushReport(pending = 3, stopped = true), report)
        assertEquals(records, store.oldest(10))
        assertTrue(retries.isEmpty())

        monotonic.elapsedMillis += 1_000_000
        assertEquals(FlushReport(pending = 3), runner.flush(now, BATCH_MAX_RECORDS))
        assertEquals(1, transport.sent.size)
    }

    @Test
    fun a403_stopsSendingForTheProcess() {
        saveRecords(1L..2L)
        val transport = FakeTransport(TransportResult.HttpError(403, null))
        val runner = runner(transport)

        assertTrue(runner.flush(now, BATCH_MAX_RECORDS).stopped)
        runner.flush(now, BATCH_MAX_RECORDS)

        assertEquals(1, transport.sent.size)
        assertEquals(2, store.count())
    }

    @Test
    fun a413_splitsTheBatch_thenRejectsASingleRecord() {
        saveRecords(1L..4L)
        val transport = FakeTransport(
            TransportResult.HttpError(413, null), // 1..4
            TransportResult.Success(200), // 1, 2
            TransportResult.HttpError(413, null), // 3, 4
            TransportResult.HttpError(413, null), // 3 alone: rejected
            TransportResult.Success(200) // 4
        )

        val report = runner(transport).flush(now, BATCH_MAX_RECORDS)

        assertEquals(FlushReport(rejected = 1, pending = 0), report)
        assertEquals(
            listOf(ids(1L..4L), ids(1L..2L), ids(3L..4L), ids(3L..3L), ids(4L..4L)),
            transport.sentIds()
        )
        assertTrue(retries.isEmpty())
    }

    @Test
    fun a400_removesTheBatch_andCountsItRejected() {
        saveRecords(1L..3L)
        val transport = FakeTransport(TransportResult.HttpError(400, null))

        val report = runner(transport).flush(now, BATCH_MAX_RECORDS)

        assertEquals(FlushReport(rejected = 3, pending = 0), report)
        assertTrue(retries.isEmpty())
    }

    @Test
    fun aPartialSuccessOf3_deletesTheBatch_andCounts3Rejected() {
        saveRecords(1L..5L)
        val transport = FakeTransport(TransportResult.Success(200, rejectedRecords = 3))

        val report = runner(transport).flush(now, BATCH_MAX_RECORDS)

        assertEquals(FlushReport(rejected = 3, pending = 0), report)
    }

    @Test
    fun aPartialSuccess_countsAtMostTheBatchsSources() {
        saveRecords(1L..2L)
        val transport = FakeTransport(TransportResult.Success(200, rejectedRecords = 10))

        assertEquals(FlushReport(rejected = 2, pending = 0), runner(transport).flush(now, BATCH_MAX_RECORDS))
    }

    @Test
    fun aNoticeSentInABatch_appearsInExportedNoticeIds() {
        saveRecords(1L..2L)
        val notice = TelemetryTestFakes.record(
            3,
            event = TelemetryEvent.Dropped(TelemetryEvent.DroppedReason.RATE_LIMIT, 4)
        ).also(store::save)
        val transport = FakeTransport()

        val report = runner(transport).flush(now, BATCH_MAX_RECORDS)

        assertEquals(setOf(notice.id), report.exportedNoticeIds)
        assertEquals(0, report.pending)
    }

    @Test
    fun aRejectedNotice_alsoAppearsInExportedNoticeIds_whileARetriedOneDoesNot() {
        val notice = TelemetryTestFakes.record(
            1,
            event = TelemetryEvent.Dropped(TelemetryEvent.DroppedReason.STORE_FULL, 2)
        ).also(store::save)

        val retried = runner(FakeTransport(TransportResult.HttpError(503, null))).flush(now, BATCH_MAX_RECORDS)
        assertTrue(retried.exportedNoticeIds.isEmpty())

        val rejected = runner(FakeTransport(TransportResult.HttpError(400, null))).flush(now, BATCH_MAX_RECORDS)
        assertEquals(setOf(notice.id), rejected.exportedNoticeIds)
    }

    @Test
    fun theRunnerGivesUp_afterFiveAttempts() {
        saveRecords(1L..2L)
        val transport = FakeTransport(otherwise = TransportResult.HttpError(500, null))
        val runner = runner(transport)

        val reports = (1..RETRY_MAX_ATTEMPTS).map {
            runner.flush(now, BATCH_MAX_RECORDS).also { monotonic.elapsedMillis += retries.lastOrNull() ?: 0L }
        }

        assertEquals(List(RETRY_MAX_ATTEMPTS - 1) { false } + true, reports.map { it.stopped })
        assertEquals(RETRY_MAX_ATTEMPTS - 1, retries.size)
        assertEquals(RETRY_MAX_ATTEMPTS, transport.sent.size)

        monotonic.elapsedMillis += 1_000_000
        assertEquals(FlushReport(pending = 2), runner.flush(now, BATCH_MAX_RECORDS))
        assertEquals(RETRY_MAX_ATTEMPTS, transport.sent.size)
    }

    @Test
    fun aSuccess_resetsTheFailedAttempts() {
        saveRecords(1L..2L)
        val failures = Array<TransportResult>(RETRY_MAX_ATTEMPTS - 1) { TransportResult.Timeout }
        val transport = FakeTransport(*failures, TransportResult.Success(204), *failures)
        val runner = runner(transport)

        repeat(RETRY_MAX_ATTEMPTS) {
            runner.flush(now, BATCH_MAX_RECORDS)
            monotonic.elapsedMillis += 60_000
        }
        saveRecords(3L..4L)
        val reports = (1 until RETRY_MAX_ATTEMPTS).map {
            runner.flush(now, BATCH_MAX_RECORDS).also { monotonic.elapsedMillis += 60_000 }
        }

        assertTrue(reports.none { it.stopped })
        assertEquals(2, store.count())
    }

    @Test
    fun batches_holdAtMostMaxRecords() {
        saveRecords(1L..75L)
        val transport = FakeTransport()

        runner(transport).flush(now, BATCH_MAX_RECORDS)

        assertEquals(listOf(50, 25), transport.sent.map { it.logRecords.size })
        assertEquals(ids(1L..75L), transport.sentIds().flatten())

        saveRecords(76L..100L)
        runner(transport).flush(now, 10)
        assertEquals(listOf(10, 10, 5), transport.sent.drop(2).map { it.logRecords.size })
    }

    @Test
    fun batches_holdAtMost256KB_andALargerRecordGoesAlone() {
        val stack = "\tat test.Frame.run(Frame.kt:1)\n"
        (1L..40L).forEach { store.save(record(it).with("exception.stacktrace", Str("$it$stack".repeat(400)))) }
        val huge = record(41).with("exception.stacktrace", Str("41$stack".repeat(10_000))).also(store::save)
        store.save(record(42))
        val transport = FakeTransport()

        val report = runner(transport).flush(now, BATCH_MAX_RECORDS)

        assertEquals(FlushReport(pending = 0), report)
        val sizes = transport.sent.map { request ->
            GZIPInputStream(request.payload.inputStream()).use { it.readBytes() }.size
        }
        assertTrue(transport.sent.size > 2)
        transport.sent.dropLast(2).forEachIndexed { index, _ -> assertTrue("$sizes", sizes[index] <= BATCH_MAX_BYTES) }
        assertEquals(listOf(huge.id), transport.sent[transport.sent.size - 2].recordIds())
        assertTrue(sizes[sizes.size - 2] > BATCH_MAX_BYTES)
        assertEquals(listOf("$SESSION_ID-42"), transport.sent.last().recordIds())
        assertEquals(ids(1L..42L), transport.sentIds().flatten())
    }

    @Test
    fun acceptedBatches_areDeleted_whileAFailedBatchKeepsItsFiles() {
        saveRecords(1L..4L)
        val transport = FakeTransport(TransportResult.Success(200), TransportResult.HttpError(503, null))

        val report = runner(transport).flush(now, 2)

        assertEquals(FlushReport(pending = 2), report)
        assertEquals(ids(3L..4L), store.oldest(10).map { it.id })
        assertEquals(ids(1L..2L), store.removed)
        assertEquals(1, retries.size)
    }

    @Test
    fun tooOldRecords_areDroppedAtFlush_andNeverSent() {
        saveRecords(1L..2L)
        store.save(record(3, createdAt = now - RECORD_MAX_AGE_MILLIS - 1))
        val transport = FakeTransport()

        val report = runner(transport).flush(now, BATCH_MAX_RECORDS)

        assertEquals(FlushReport(tooOld = 1, pending = 0), report)
        assertEquals(listOf(ids(1L..2L)), transport.sentIds())
    }

    @Test
    fun nothingIsSent_withoutTheInstallationId() {
        saveRecords(1L..3L)
        val transport = FakeTransport()
        val noInstallation =
            FakeDeviceContextProvider(TelemetryTestFakes.resourceAttributes.copy(appInstallationId = " "))

        val report = runner(transport, noInstallation).flush(now, BATCH_MAX_RECORDS)

        assertEquals(FlushReport(pending = 3), report)
        assertTrue(transport.sent.isEmpty())
    }

    @Test
    fun aRecordWithoutSessionId_isNeverSent_andCountedRejected() {
        saveRecords(1L..2L)
        val bare = record(3).let { it.copy(attributes = it.attributes.filterNot { a -> a.key == "session.id" }) }
        store.save(bare)
        val transport = FakeTransport()

        val report = runner(transport).flush(now, BATCH_MAX_RECORDS)

        assertEquals(FlushReport(rejected = 1, pending = 0), report)
        assertEquals(listOf(ids(1L..2L)), transport.sentIds())
        assertEquals(listOf(TelemetryTestFakes.INSTALLATION_ID), transport.sent.map { it.resourceValue("app.installation.id") })
        assertEquals(List(2) { SESSION_ID }, transport.sent.single().recordValues("session.id"))
    }

    @Test
    fun earlierRunRecords_goFirst() {
        saveRecords(1L..3L)
        val earlierSession = TelemetryTestFakes.uuid(77)
        val earlier = (1L..2L).map { record(it, earlierSession, createdAt = T0 - 60_000 + it).also(store::save) }
        val transport = FakeTransport()

        runner(transport).flush(now, 2)

        assertEquals(earlier.map { it.id }, transport.sent.first().recordIds())
        assertEquals(listOf(earlierSession, earlierSession), transport.sent.first().recordValues("session.id"))
    }

    @Test
    fun duplicates_areSentOnceWithTheirCount_andEverySourceIsRemoved() {
        val first = TelemetryTestFakes.record(1, event = TelemetryTestFakes.sdkError("test.Same"))
        val second = TelemetryTestFakes.record(2, event = TelemetryTestFakes.sdkError("test.Same"))
        store.save(first)
        store.save(second)
        val transport = FakeTransport()

        runner(transport).flush(now, BATCH_MAX_RECORDS)

        assertEquals(listOf(listOf(first.id)), transport.sentIds())
        assertEquals(listOf("2"), transport.sent.single().recordValues("paydock.occurrences"))
        assertEquals(0, store.count())
    }

    @Test
    fun anEmptyStore_sendsNothing() {
        val transport = FakeTransport()

        assertEquals(FlushReport(pending = 0), runner(transport).flush(now, BATCH_MAX_RECORDS))
        assertTrue(transport.sent.isEmpty())
        assertFalse(retries.isNotEmpty())
    }

    @Test
    fun aThrowingStore_neverThrows() {
        val throwing = object : DurableStore by store {
            override fun prune(now: Long): PruneResult = error("synthetic store failure")
        }
        val runner = ExportRunner(
            throwing,
            OtlpExporter(FakeTransport(), TelemetryTestFakes.credentials),
            FakeDeviceContextProvider(),
            monotonic,
            RetryPolicy(Random(3)),
            scheduleRetry = { retries += it }
        )
        saveRecords(1L..2L)

        assertEquals(FlushReport(pending = 2), runner.flush(now, BATCH_MAX_RECORDS))
    }
}

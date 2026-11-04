package com.paydock.telemetry

import com.paydock.telemetry.TelemetryTestFakes.FakeDeviceContextProvider
import com.paydock.telemetry.TelemetryTestFakes.RecordingExporter
import com.paydock.telemetry.TelemetryTestFakes.RecordingQueue
import com.paydock.telemetry.TelemetryTestFakes.RecordingStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [TelemetryEffectHandler] (R_02, R_12): every effect reaches its port, and no failure spreads. */
internal class TelemetryEffectHandlerTest {

    private val queue = RecordingQueue()
    private val store = RecordingStore()
    private val exporter = RecordingExporter()
    private val deviceContext = FakeDeviceContextProvider()
    private val scheduled = mutableListOf<Long>()

    private val handler = TelemetryEffectHandler(queue, store, exporter, deviceContext) { scheduled += it }

    private fun record(sequence: Int, sessionId: String = "session-1") = PendingRecord(
        id = "$sessionId-$sequence",
        sessionId = sessionId,
        event = TelemetryTestFakes.sdkError(),
        firstSeenAt = 1_700_000_000_000L + sequence
    )

    @Test
    fun enqueue_offersTheRecordToTheQueue() {
        handler.run(listOf(TelemetryEffect.Enqueue(record(0))))

        assertEquals(listOf(record(0)), queue.offered)
    }

    @Test
    fun persist_savesTheRecordToTheStore() {
        handler.run(listOf(TelemetryEffect.Persist(record(0))))

        assertEquals(listOf(record(0)), store.saved)
    }

    @Test
    fun scheduleFlush_callsScheduleFlushWithTheDelay() {
        handler.run(listOf(TelemetryEffect.ScheduleFlush(TelemetryReducer.FLUSH_INTERVAL_MILLIS)))

        assertEquals(listOf(TelemetryReducer.FLUSH_INTERVAL_MILLIS), scheduled)
    }

    @Test
    fun drop_touchesNoPort() {
        handler.run(TelemetryEffect.DropReason.entries.map { TelemetryEffect.Drop(it) })

        assertTrue(queue.offered.isEmpty())
        assertTrue(store.saved.isEmpty())
        assertTrue(exporter.batches.isEmpty())
        assertTrue(scheduled.isEmpty())
    }

    @Test
    fun export_handsTheExporterABatchWithTheInstallationId_andRecordsWithTheirSessionId() {
        val records = (0 until 3).map { record(it) }
        records.forEach { queue.offer(it) }

        handler.run(listOf(TelemetryEffect.Export(TelemetryReducer.MAX_BATCH)))

        val batch = exporter.batches.single()
        assertEquals(ExportBatch(TelemetryTestFakes.resourceAttributes, records), batch)
        assertEquals(TelemetryTestFakes.INSTALLATION_ID, batch.resource.appInstallationId)
        assertEquals(TelemetryTestFakes.INSTALLATION_ID, batch.resource.asMap()["app.installation.id"])
        assertTrue(batch.records.all { it.sessionId == "session-1" })
    }

    @Test
    fun export_drainsAtMostMaxRecords_andSkipsAnEmptyQueue() {
        (0 until 5).forEach { queue.offer(record(it)) }

        handler.run(List(4) { TelemetryEffect.Export(2) })

        assertEquals(listOf(2, 2, 1), exporter.batches.map { it.records.size })
    }

    @Test
    fun aThrowingPort_doesNotStopTheFollowingEffects() {
        val failingQueue = object : RecordQueue {
            override fun offer(record: PendingRecord) = error("synthetic queue failure")
            override fun drain(max: Int): List<PendingRecord> = error("synthetic queue failure")
            override fun size(): Int = 0
        }
        val failingStore = object : DurableStore {
            override fun save(record: PendingRecord) = error("synthetic store failure")
            override fun loadAll(): List<PendingRecord> = emptyList()
            override fun remove(ids: Collection<String>) = Unit
            override fun prune(now: Long) = Unit
        }
        val handler = TelemetryEffectHandler(failingQueue, failingStore, exporter, deviceContext) { scheduled += it }

        handler.run(
            listOf(
                TelemetryEffect.Persist(record(0)),
                TelemetryEffect.Enqueue(record(0)),
                TelemetryEffect.Export(TelemetryReducer.MAX_BATCH),
                TelemetryEffect.ScheduleFlush(1L)
            )
        )

        assertEquals(listOf(1L), scheduled)
    }

    @Test
    fun aThrowingScheduler_doesNotStopTheFollowingEffects() {
        val handler = TelemetryEffectHandler(queue, store, exporter, deviceContext) {
            error("synthetic scheduler failure")
        }

        handler.run(listOf(TelemetryEffect.ScheduleFlush(1L), TelemetryEffect.Persist(record(0))))

        assertEquals(listOf(record(0)), store.saved)
    }
}

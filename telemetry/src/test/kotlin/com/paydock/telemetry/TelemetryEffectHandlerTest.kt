package com.paydock.telemetry

import com.paydock.telemetry.TelemetryEvent.DroppedReason
import com.paydock.telemetry.TelemetryIntent.RecordsDropped
import com.paydock.telemetry.TelemetryTestFakes.FakeClock
import com.paydock.telemetry.TelemetryTestFakes.FakeDeviceContextProvider
import com.paydock.telemetry.TelemetryTestFakes.FakeMonotonic
import com.paydock.telemetry.TelemetryTestFakes.FakeTransport
import com.paydock.telemetry.TelemetryTestFakes.InMemoryStore
import com.paydock.telemetry.TelemetryTestFakes.SESSION_ID
import com.paydock.telemetry.TelemetryTestFakes.T0
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.random.Random

/**
 * Unit tests for [TelemetryEffectHandler] (R_02, R_06 to R_10, R_12): every effect reaches its port, the follow-up
 * intents come back in order, and no failure spreads: a failed effect is reported in the outcome, never thrown.
 */
internal class TelemetryEffectHandlerTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val clock = FakeClock(T0)
    private val deviceContext = FakeDeviceContextProvider()
    private val scheduled = mutableListOf<Long>()

    private fun handler(
        store: DurableStore = InMemoryStore(),
        transport: TelemetryTransport = FakeTransport(),
        schedule: (Long) -> Unit = { scheduled += it },
    ): TelemetryEffectHandler {
        val runner = ExportRunner(
            store = store,
            exporter = OtlpExporter(transport, TelemetryTestFakes.credentials),
            deviceContext = deviceContext,
            clockOffset = ClockOffset(clock),
            monotonic = FakeMonotonic(),
            retryPolicy = RetryPolicy(Random(1)),
            scheduleRetry = {}
        )
        return TelemetryEffectHandler(store, runner, deviceContext, clock, schedule)
    }

    /** Each sequence its own error type, so the duplicate gate never merges two of them. */
    private fun pending(sequence: Long, at: Long = T0 + sequence, sessionId: String = SESSION_ID) =
        PendingRecord("$sessionId-$sequence", sessionId, TelemetryTestFakes.sdkError("test.Error$sequence"), at)

    private fun record(sequence: Long, createdAt: Long = T0 + sequence) = TelemetryTestFakes.record(
        sequence,
        event = TelemetryTestFakes.sdkError("test.Error$sequence"),
        createdAt = createdAt
    )

    @Test
    fun persist_storesTheBuiltAndFilteredRecord_andReturnsNothingWhenNothingIsDropped() {
        val store = InMemoryStore()

        val followUps = handler(store).run(listOf(TelemetryEffect.Persist(pending(1))))

        val expected = Allowlist.filter(RecordFactory.create(pending(1), NetworkConnectionType.WIFI))
        assertEquals(listOf(expected), store.saved)
        assertTrue(followUps.intents.isEmpty())
        assertFalse(followUps.failed)
    }

    @Test
    fun persist_returnsStoreFull_whenPruningForSpaceDropsRecords() {
        val store = FileDurableStore { folder.root }
        val handler = handler(store)
        (1L..TelemetryLimits.STORE_MAX_RECORDS).forEach { handler.run(listOf(TelemetryEffect.Persist(pending(it)))) }

        val followUps = handler.run(listOf(TelemetryEffect.Persist(pending(500))))

        assertEquals(listOf(RecordsDropped(DroppedReason.STORE_FULL, 1)), followUps.intents)
        assertEquals(TelemetryLimits.STORE_MAX_RECORDS, store.count())
    }

    @Test
    fun persist_returnsTooOld_whenPruningDropsOldRecords() {
        val store = FileDurableStore { folder.root }
        val handler = handler(store)
        handler.run(listOf(TelemetryEffect.Persist(pending(1, at = T0 - TelemetryLimits.RECORD_MAX_AGE_MILLIS - 1))))
        assertEquals(0, store.count())

        handler.run(listOf(TelemetryEffect.Persist(pending(2))))
        clock.nowMillis = T0 + TelemetryLimits.RECORD_MAX_AGE_MILLIS + 3
        val followUps = handler.run(listOf(TelemetryEffect.Persist(pending(3, at = clock.nowMillis))))

        assertEquals(listOf(RecordsDropped(DroppedReason.TOO_OLD, 1)), followUps.intents)
        assertEquals(1, store.count())
    }

    @Test
    fun persist_ofARecordThatLostItsSessionId_savesNothing_andReturnsOneRejected() {
        val store = InMemoryStore()

        val followUps = handler(store).run(listOf(TelemetryEffect.Persist(pending(1, sessionId = "not-a-uuid"))))

        assertTrue(store.saved.isEmpty())
        assertEquals(listOf(RecordsDropped(DroppedReason.REJECTED, 1)), followUps.intents)
        assertFalse("a rejected record is a drop, not a failure", followUps.failed)
    }

    /** MD-47 R_05: a dropped notice that lost its `session.id` is not saved and counts nothing, so it never loops. */
    @Test
    fun persist_ofADroppedNoticeThatLostItsSessionId_savesNothing_andReturnsNothing() {
        val store = InMemoryStore()
        val notice = PendingRecord("not-a-uuid-1", "not-a-uuid", TelemetryEvent.Dropped(DroppedReason.REJECTED, 1), T0)

        val followUps = handler(store).run(listOf(TelemetryEffect.Persist(notice)))

        assertTrue(store.saved.isEmpty())
        assertTrue(followUps.intents.isEmpty())
        assertFalse(followUps.failed)
    }

    @Test
    fun export_returnsNoticesExportedFirst_thenExportStopped_thenPendingCounted_thenTheDropCounts() {
        val store = InMemoryStore()
        val notice = PendingRecord("$SESSION_ID-1", SESSION_ID, TelemetryEvent.Dropped(DroppedReason.RATE_LIMIT, 2), T0)
        store.save(Allowlist.filter(RecordFactory.create(notice, NetworkConnectionType.WIFI)))
        store.save(record(2))
        store.save(record(3))
        store.save(record(4))
        store.save(record(5, createdAt = T0 - TelemetryLimits.RECORD_MAX_AGE_MILLIS - 1))
        store.extraPrune = PruneResult(storeFull = 2)
        val transport = FakeTransport(
            TransportResult.Success(200), // the notice
            TransportResult.HttpError(400, null), // record 2: rejected
            TransportResult.HttpError(401, null) // record 3: sending stops
        )

        val followUps = handler(store, transport).run(listOf(TelemetryEffect.Export(1)))

        assertEquals(
            listOf(
                TelemetryIntent.NoticesExported(setOf(notice.id)),
                TelemetryIntent.ExportStopped,
                TelemetryIntent.PendingCounted(2),
                RecordsDropped(DroppedReason.TOO_OLD, 1),
                RecordsDropped(DroppedReason.STORE_FULL, 2),
                RecordsDropped(DroppedReason.REJECTED, 1)
            ),
            followUps.intents
        )
        assertEquals(3, transport.sent.size)
    }

    /**
     * MD-47 R_01: a notice the export just sent follows first, before the drop an earlier effect of the same run
     * counted, so the reducer clears it before the drop and the drop starts a new notice instead of updating it.
     */
    @Test
    fun sentNotices_followFirst_beforeTheDropsOfAnEarlierEffectInTheSameRun() {
        val store = InMemoryStore()
        val notice = PendingRecord("$SESSION_ID-2", SESSION_ID, TelemetryEvent.Dropped(DroppedReason.STORE_FULL, 1), T0)
        store.save(Allowlist.filter(RecordFactory.create(notice, NetworkConnectionType.WIFI)))
        store.extraPrune = PruneResult(storeFull = 1)

        val followUps = handler(store).run(listOf(TelemetryEffect.Persist(pending(1)), TelemetryEffect.Export(50)))

        assertEquals(
            listOf(
                TelemetryIntent.NoticesExported(setOf(notice.id)),
                RecordsDropped(DroppedReason.STORE_FULL, 1),
                TelemetryIntent.PendingCounted(0),
                RecordsDropped(DroppedReason.STORE_FULL, 1)
            ),
            followUps.intents
        )
        assertFalse(followUps.failed)
    }

    @Test
    fun export_ofAnEmptyStore_returnsOnlyPendingCounted() {
        val transport = FakeTransport()

        val followUps = handler(transport = transport).run(listOf(TelemetryEffect.Export(50)))

        assertEquals(listOf(TelemetryIntent.PendingCounted(0)), followUps.intents)
        assertTrue(transport.sent.isEmpty())
    }

    @Test
    fun export_sendsTheInstallationId_andEveryRecordsSessionId() {
        val store = InMemoryStore()
        (1L..3L).forEach { store.save(record(it)) }
        val transport = FakeTransport()

        handler(store, transport).run(listOf(TelemetryEffect.Export(50)))

        val request = transport.sent.single()
        assertEquals(TelemetryTestFakes.INSTALLATION_ID, request.resourceValue("app.installation.id"))
        assertEquals(List(3) { SESSION_ID }, request.recordValues("session.id"))
        assertEquals(0, store.count())
    }

    @Test
    fun scheduleFlush_callsScheduleFlushWithTheDelay_andReturnsNothing() {
        val followUps = handler().run(listOf(TelemetryEffect.ScheduleFlush(TelemetryLimits.FLUSH_INTERVAL_MILLIS)))

        assertEquals(listOf(TelemetryLimits.FLUSH_INTERVAL_MILLIS), scheduled)
        assertTrue(followUps.intents.isEmpty())
    }

    @Test
    fun drop_touchesNoPort() {
        val store = InMemoryStore()
        val transport = FakeTransport()

        val followUps = handler(store, transport).run(
            TelemetryEffect.DropReason.entries.map { TelemetryEffect.Drop(it) }
        )

        assertTrue(followUps.intents.isEmpty())
        assertFalse("a drop is not a failure", followUps.failed)
        assertTrue(store.threads.isEmpty())
        assertTrue(transport.sent.isEmpty())
        assertTrue(scheduled.isEmpty())
    }

    @Test
    fun aThrowingPort_doesNotStopTheFollowingEffects() {
        val failingStore = object : DurableStore {
            override fun save(record: TelemetryRecord) = error("synthetic store failure")
            override fun oldest(max: Int): List<TelemetryRecord> = error("synthetic store failure")
            override fun remove(ids: Collection<String>) = error("synthetic store failure")
            override fun prune(now: Long): PruneResult = error("synthetic store failure")
            override fun count(): Int = error("synthetic store failure")
        }

        val followUps = handler(failingStore).run(
            listOf(
                TelemetryEffect.Persist(pending(1)),
                TelemetryEffect.Export(50),
                TelemetryEffect.ScheduleFlush(1L)
            )
        )

        assertEquals(listOf(1L), scheduled)
        assertEquals(listOf(TelemetryIntent.PendingCounted(0)), followUps.intents)
        assertTrue("the failed save is reported, never thrown", followUps.failed)
    }

    @Test
    fun aThrowingScheduler_doesNotStopTheFollowingEffects() {
        val store = InMemoryStore()
        val handler = handler(store) { error("synthetic scheduler failure") }

        val outcome = handler.run(listOf(TelemetryEffect.ScheduleFlush(1L), TelemetryEffect.Persist(pending(1))))

        assertEquals(1, store.saved.size)
        assertTrue(outcome.failed)
    }
}

package com.paydock.telemetry

import com.paydock.telemetry.TelemetryEffect.Drop
import com.paydock.telemetry.TelemetryEffect.DropReason
import com.paydock.telemetry.TelemetryEffect.Export
import com.paydock.telemetry.TelemetryEffect.Persist
import com.paydock.telemetry.TelemetryEffect.ScheduleFlush
import com.paydock.telemetry.TelemetryEvent.DroppedReason
import com.paydock.telemetry.TelemetryIntent.FlushSource
import com.paydock.telemetry.TelemetryLimits.BATCH_MAX_RECORDS
import com.paydock.telemetry.TelemetryLimits.FLUSH_INTERVAL_MILLIS
import com.paydock.telemetry.TelemetryLimits.FLUSH_THRESHOLD
import com.paydock.telemetry.TelemetryLimits.HOURLY_CAP
import com.paydock.telemetry.TelemetryLimits.HOUR_MILLIS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the pure [TelemetryReducer] (R_01, R_04, R_09 to R_11, R_17, R_18), one test per rule. `now` is
 * UTC epoch ms and `elapsed` monotonic ms; events are synthetic and carry no message text.
 */
internal class TelemetryReducerTest {

    private val t0 = TelemetryTestFakes.T0
    private val e0 = 5_000L
    private val sessionId = TelemetryTestFakes.SESSION_ID

    private val error = TelemetryTestFakes.sdkError()
    private val crash = TelemetryTestFakes.crash()
    private val widgetOpened = TelemetryEvent.WidgetOpened(widget = "CardDetailsWidget")

    private var sequence = RecordSequence()

    private fun state(level: TelemetryLevel = TelemetryLevel.ALL, enabled: Boolean = true) =
        TelemetryState(level = level, enabled = enabled, sessionId = sessionId)

    private fun reduce(state: TelemetryState, intent: TelemetryIntent, now: Long = t0, elapsed: Long = e0) =
        TelemetryReducer.reduce(state, intent, now, elapsed, sequence)

    private fun report(state: TelemetryState, event: TelemetryEvent, now: Long = t0, elapsed: Long = e0) =
        reduce(state, TelemetryIntent.Reported(event), now, elapsed)

    private fun pending(sequence: Long, event: TelemetryEvent, at: Long = t0) =
        PendingRecord("$sessionId-$sequence", sessionId, event, at)

    private fun persisted(effects: List<TelemetryEffect>): List<PendingRecord> =
        effects.filterIsInstance<Persist>().map { it.record }

    /** Reports [count] errors in a row from [start], returning the last state. */
    private fun reportMany(start: TelemetryState, count: Int, elapsed: Long = e0): TelemetryState =
        (0 until count).fold(start) { current, _ -> report(current, error, elapsed = elapsed).first }

    @Test
    fun recordIds_startAtSequence1_andComeFromTheInjectedSequence() {
        val (first, firstEffects) = report(state(), crash)
        val (_, secondEffects) = report(first, error, t0 + 1)

        assertEquals(listOf("$sessionId-1", "$sessionId-2"), persisted(firstEffects + secondEffects).map { it.id })
        assertTrue(persisted(firstEffects + secondEffects).all { it.sessionId == sessionId })

        val shared = RecordSequence()
        repeat(4) { shared.next() } // another path, a crash handler, took 1 to 4
        val (_, effects) = TelemetryReducer.reduce(state(), TelemetryIntent.Reported(error), t0, e0, shared)
        assertEquals("$sessionId-5", persisted(effects).single().id)
        assertEquals(6L, shared.next())
    }

    @Test
    fun reported_persistsTheRecord_raisesTheCounts_andArmsOneTimer() {
        val (first, effects) = report(state(), error)

        assertEquals(listOf(Persist(pending(1, error)), ScheduleFlush(FLUSH_INTERVAL_MILLIS)), effects)
        assertEquals(1, first.pending)
        assertEquals(1, first.createdInWindow)
        assertEquals(e0, first.windowStart)
        assertTrue(first.flushScheduled)

        val (second, secondEffects) = report(first, error, t0 + 1)

        assertEquals(listOf(Persist(pending(2, error, t0 + 1))), secondEffects)
        assertEquals(2, second.pending)
    }

    @Test
    fun onlyATimerFlush_clearsFlushScheduled() {
        val armed = state().copy(pending = 3, flushScheduled = true)

        listOf(FlushSource.START, FlushSource.RETRY, FlushSource.DEBUG_SEND).forEach { source ->
            val (flushed, effects) = reduce(armed, TelemetryIntent.Flush(source))
            assertEquals(listOf(Export(BATCH_MAX_RECORDS)), effects)
            assertTrue(flushed.flushScheduled)

            // The true count comes back after the flush: the armed timer covers it, so no second timer.
            val (counted, countedEffects) = reduce(flushed, TelemetryIntent.PendingCounted(2))
            assertTrue(countedEffects.isEmpty())
            assertTrue(counted.flushScheduled)
        }

        val (ticked, effects) = reduce(armed, TelemetryIntent.Flush(FlushSource.TIMER))
        assertEquals(listOf(Export(BATCH_MAX_RECORDS)), effects)
        assertFalse(ticked.flushScheduled)
        val (_, countedEffects) = reduce(ticked, TelemetryIntent.PendingCounted(2))
        assertEquals(listOf(ScheduleFlush(FLUSH_INTERVAL_MILLIS)), countedEffects)
    }

    @Test
    fun flushAndAppBackgrounded_exportEvenWhenPendingReadsZero() {
        val (flushed, flushEffects) = reduce(state(), TelemetryIntent.Flush(FlushSource.START))
        assertEquals(listOf(Export(BATCH_MAX_RECORDS)), flushEffects)
        assertEquals(0, flushed.pending)
        assertEquals(t0, flushed.lastExport)

        val (backgrounded, backgroundEffects) = reduce(state(), TelemetryIntent.AppBackgrounded, now = t0 + 7)
        assertEquals(listOf(Export(BATCH_MAX_RECORDS)), backgroundEffects)
        assertEquals(t0 + 7, backgrounded.lastExport)
        assertFalse(backgrounded.flushScheduled)
    }

    @Test
    fun export_resetsPendingToZero_andRecordsTheTime() {
        val (next, effects) = reduce(state().copy(pending = 120), TelemetryIntent.AppBackgrounded, now = t0 + 3)

        assertEquals(listOf(Export(BATCH_MAX_RECORDS)), effects)
        assertEquals(0, next.pending)
        assertEquals(t0 + 3, next.lastExport)
    }

    @Test
    fun reported_atTheThresholdOf20_exportsAtOnce() {
        val nearly = reportMany(state(), FLUSH_THRESHOLD - 1)
        assertEquals(FLUSH_THRESHOLD - 1, nearly.pending)

        val (next, effects) = report(nearly, error, t0 + 100)

        assertEquals(Persist(pending(FLUSH_THRESHOLD.toLong(), error, t0 + 100)), effects.first())
        assertEquals(Export(BATCH_MAX_RECORDS), effects.last())
        assertEquals(1, effects.count { it is Export })
        assertEquals(0, next.pending)
        assertEquals(t0 + 100, next.lastExport)
    }

    @Test
    fun pendingCounted_setsPending_thenSchedules() {
        val (counted, effects) = reduce(state(), TelemetryIntent.PendingCounted(7))
        assertEquals(7, counted.pending)
        assertEquals(listOf(ScheduleFlush(FLUSH_INTERVAL_MILLIS)), effects)
        assertTrue(counted.flushScheduled)

        val (empty, emptyEffects) = reduce(state(), TelemetryIntent.PendingCounted(0))
        assertEquals(0, empty.pending)
        assertTrue(emptyEffects.isEmpty())
        assertFalse(empty.flushScheduled)
    }

    @Test
    fun afterExportStopped_noExportAndNoTimer_evenAt20Pending() {
        val (stopped, stopEffects) = reduce(state().copy(flushScheduled = true), TelemetryIntent.ExportStopped)
        assertTrue(stopEffects.isEmpty())
        assertTrue(stopped.exportStopped)

        // The armed timer fires once more, exports nothing and is not re-armed.
        val (ticked, tickEffects) = reduce(stopped, TelemetryIntent.Flush(FlushSource.TIMER))
        assertTrue(tickEffects.isEmpty())
        assertFalse(ticked.flushScheduled)

        val full = reportMany(ticked, FLUSH_THRESHOLD + 5)
        assertEquals(FLUSH_THRESHOLD + 5, full.pending)
        val (_, effects) = report(full, error)
        assertEquals(1, effects.size)
        assertTrue(effects.single() is Persist)

        val intents = listOf(
            TelemetryIntent.Flush(FlushSource.START),
            TelemetryIntent.Flush(FlushSource.RETRY),
            TelemetryIntent.Flush(FlushSource.DEBUG_SEND),
            TelemetryIntent.AppBackgrounded,
            TelemetryIntent.PendingCounted(30),
            TelemetryIntent.RecordsDropped(DroppedReason.REJECTED, 2)
        )
        intents.forEach { intent ->
            val produced = reduce(full, intent).second
            assertTrue("$intent", produced.none { it is Export || it is ScheduleFlush })
        }
    }

    @Test
    fun the401stRecordInAnHour_isDropped_andTheHoursNoticeCountsEveryDropInOneRecord() {
        val atCap = reportMany(state(), HOURLY_CAP, elapsed = e0)
        assertEquals(HOURLY_CAP, atCap.createdInWindow)
        val idsUsed = HOURLY_CAP.toLong()

        val (first, firstEffects) = report(atCap, error, t0 + 1, e0 + 1)

        val noticeId = "$sessionId-${idsUsed + 1}"
        val firstNotice =
            PendingRecord(noticeId, sessionId, TelemetryEvent.Dropped(DroppedReason.RATE_LIMIT, 1), t0 + 1)
        assertEquals(Drop(DropReason.RATE_LIMIT), firstEffects.first())
        assertEquals(listOf(firstNotice), persisted(firstEffects))
        assertEquals(HOURLY_CAP, first.createdInWindow)
        assertEquals(DroppedNotice(noticeId, 1, t0 + 1), first.notices[DroppedReason.RATE_LIMIT])

        val (second, secondEffects) = report(first, crash, t0 + 2, e0 + 2)
        val (third, thirdEffects) = report(second, widgetOpened, t0 + 3, e0 + 3)

        assertEquals(Drop(DropReason.RATE_LIMIT), secondEffects.first())
        assertEquals(Drop(DropReason.RATE_LIMIT), thirdEffects.first())
        val notices = persisted(secondEffects + thirdEffects)
        assertEquals(listOf(noticeId, noticeId), notices.map { it.id })
        assertEquals(listOf(t0 + 1, t0 + 1), notices.map { it.createdAt })
        assertEquals(TelemetryEvent.Dropped(DroppedReason.RATE_LIMIT, 3), notices.last().event)
        assertEquals(DroppedNotice(noticeId, 3, t0 + 1), third.notices[DroppedReason.RATE_LIMIT])
        assertEquals(HOURLY_CAP, third.createdInWindow)
    }

    @Test
    fun theHourRollsOnElapsedAlone_soAWallClockJumpChangesNothing() {
        val atCap = reportMany(state(), HOURLY_CAP, elapsed = e0)

        // A wall clock a day ahead, an hour not yet over on the monotonic clock: still capped.
        val (jumped, jumpedEffects) = report(atCap, error, now = t0 + 24 * HOUR_MILLIS, elapsed = e0 + HOUR_MILLIS - 1)
        assertEquals(Drop(DropReason.RATE_LIMIT), jumpedEffects.first())
        assertEquals(e0, jumped.windowStart)

        // A wall clock set back, the hour over on the monotonic clock: a new hour, the record kept.
        val (rolled, rolledEffects) = report(jumped, error, now = t0 - HOUR_MILLIS, elapsed = e0 + HOUR_MILLIS)
        assertTrue(rolledEffects.none { it is Drop })
        assertEquals(1, persisted(rolledEffects).size)
        assertEquals(e0 + HOUR_MILLIS, rolled.windowStart)
        assertEquals(1, rolled.createdInWindow)
        assertTrue(rolled.notices.isEmpty())
    }

    @Test
    fun recordsDropped_createsTheNotice_thenUpdatesIt_andNoticesSkipTheCap() {
        val (created, createdEffects) = reduce(state(), TelemetryIntent.RecordsDropped(DroppedReason.STORE_FULL, 2))

        val id = "$sessionId-1"
        assertEquals(
            listOf(
                Persist(PendingRecord(id, sessionId, TelemetryEvent.Dropped(DroppedReason.STORE_FULL, 2), t0)),
                ScheduleFlush(FLUSH_INTERVAL_MILLIS)
            ),
            createdEffects
        )
        assertEquals(1, created.pending)
        assertEquals(0, created.createdInWindow)

        val (updated, updatedEffects) = reduce(
            created,
            TelemetryIntent.RecordsDropped(DroppedReason.STORE_FULL, 3),
            now = t0 + 50
        )
        assertEquals(
            listOf(Persist(PendingRecord(id, sessionId, TelemetryEvent.Dropped(DroppedReason.STORE_FULL, 5), t0))),
            updatedEffects
        )
        assertEquals(DroppedNotice(id, 5, t0), updated.notices[DroppedReason.STORE_FULL])
        assertEquals(2, updated.pending)

        // At the cap, a notice is still created: notices never count toward it.
        val atCap = reportMany(state(), HOURLY_CAP)
        val (noticed, noticedEffects) = reduce(atCap, TelemetryIntent.RecordsDropped(DroppedReason.TOO_OLD, 1))
        assertEquals(1, persisted(noticedEffects).size)
        assertTrue(noticedEffects.none { it is Drop })
        assertEquals(HOURLY_CAP, noticed.createdInWindow)
    }

    @Test
    fun recordsDropped_isIgnoredForNoRecords_whenDisabled_andAtOff() {
        listOf(
            state() to TelemetryIntent.RecordsDropped(DroppedReason.REJECTED, 0),
            state() to TelemetryIntent.RecordsDropped(DroppedReason.REJECTED, -1),
            state(enabled = false) to TelemetryIntent.RecordsDropped(DroppedReason.REJECTED, 2),
            state(level = TelemetryLevel.OFF) to TelemetryIntent.RecordsDropped(DroppedReason.REJECTED, 2)
        ).forEach { (start, intent) ->
            val (next, effects) = reduce(start, intent)
            assertEquals(start, next)
            assertTrue(effects.isEmpty())
        }
        assertEquals(1L, sequence.next())
    }

    @Test
    fun afterNoticesExported_theNextDropStartsANewNotice_withANewIdAndOnlyTheNewCount() {
        val (noticed, _) = reduce(state(), TelemetryIntent.RecordsDropped(DroppedReason.REJECTED, 4))
        val oldId = noticed.notices.getValue(DroppedReason.REJECTED).id
        val (other, _) = reduce(noticed, TelemetryIntent.RecordsDropped(DroppedReason.TOO_OLD, 1))

        val (exported, exportedEffects) = reduce(other, TelemetryIntent.NoticesExported(setOf(oldId)))
        assertTrue(exportedEffects.isEmpty())
        assertNull(exported.notices[DroppedReason.REJECTED])
        assertEquals(other.notices[DroppedReason.TOO_OLD], exported.notices[DroppedReason.TOO_OLD])

        val (renewed, renewedEffects) = reduce(
            exported,
            TelemetryIntent.RecordsDropped(DroppedReason.REJECTED, 2),
            now = t0 + 9
        )
        val notice = persisted(renewedEffects).single()
        assertNotEquals(oldId, notice.id)
        assertEquals("$sessionId-3", notice.id)
        assertEquals(TelemetryEvent.Dropped(DroppedReason.REJECTED, 2), notice.event)
        assertEquals(t0 + 9, notice.createdAt)
        assertEquals(DroppedNotice(notice.id, 2, t0 + 9), renewed.notices[DroppedReason.REJECTED])
    }

    @Test
    fun dropped_cannotBeReported() {
        val start = state()

        val (next, effects) = report(start, TelemetryEvent.Dropped(DroppedReason.RATE_LIMIT, 3))

        assertEquals(start, next)
        assertEquals(listOf(Drop(DropReason.NOT_REPORTABLE)), effects)
        assertEquals(1L, sequence.next())
    }

    @Test
    fun reported_disabled_dropsWithoutStateChange() {
        val start = state(enabled = false)

        val (next, effects) = report(start, crash)

        assertEquals(start, next)
        assertEquals(listOf(Drop(DropReason.DISABLED)), effects)
    }

    @Test
    fun reported_levelOff_dropsEveryEvent() {
        val start = state(level = TelemetryLevel.OFF)

        listOf(crash, error, widgetOpened).forEach { event ->
            val (next, effects) = report(start, event)

            assertEquals(start, next)
            assertEquals(listOf(Drop(DropReason.LEVEL_OFF)), effects)
        }
    }

    @Test
    fun reported_levelErrors_keepsErrorsAndCrashes_andDropsWidgetOpened() {
        val start = state(level = TelemetryLevel.ERRORS)

        listOf(crash, error, TelemetryEvent.UncaughtException(error = error.error)).forEach { event ->
            assertTrue(TelemetryReducer.isError(event))
            assertEquals(1, persisted(report(start, event).second).size)
        }

        val (next, effects) = report(start, widgetOpened)
        assertFalse(TelemetryReducer.isError(widgetOpened))
        assertFalse(TelemetryReducer.isError(TelemetryEvent.Dropped(DroppedReason.RATE_LIMIT, 1)))
        assertEquals(start, next)
        assertEquals(listOf(Drop(DropReason.LEVEL_FILTERED)), effects)
    }

    @Test
    fun levelChanged_setsTheLevel_withNoEffect_andAppliesToTheEventsThatFollow() {
        val (changed, effects) = reduce(state(), TelemetryIntent.LevelChanged(TelemetryLevel.OFF))

        assertEquals(state().copy(level = TelemetryLevel.OFF), changed)
        assertTrue(effects.isEmpty())
        assertEquals(listOf(Drop(DropReason.LEVEL_OFF)), report(changed, error).second)

        val (restored, _) = reduce(changed, TelemetryIntent.LevelChanged(TelemetryLevel.ALL))
        assertEquals(1, persisted(report(restored, error).second).size)
    }

    @Test
    fun reduce_isDeterministic_withTwoFreshSequences_andTheInputIsNotMutated() {
        val notice = DroppedNotice("$sessionId-1", 2, t0 - 5)
        val start = state().copy(
            pending = 19,
            windowStart = e0 - 10,
            createdInWindow = 7,
            notices = mapOf(DroppedReason.REJECTED to notice)
        )
        val snapshot = start.copy(notices = HashMap(start.notices))
        val intents = listOf(
            TelemetryIntent.Reported(error),
            TelemetryIntent.Reported(crash),
            TelemetryIntent.Flush(FlushSource.TIMER),
            TelemetryIntent.Flush(FlushSource.START),
            TelemetryIntent.AppBackgrounded,
            TelemetryIntent.LevelChanged(TelemetryLevel.ERRORS),
            TelemetryIntent.RecordsDropped(DroppedReason.REJECTED, 1),
            TelemetryIntent.RecordsDropped(DroppedReason.STORE_FULL, 1),
            TelemetryIntent.PendingCounted(3),
            TelemetryIntent.NoticesExported(setOf(notice.id)),
            TelemetryIntent.ExportStopped
        )

        intents.forEach { intent ->
            val first = TelemetryReducer.reduce(start, intent, t0, e0, RecordSequence())
            val second = TelemetryReducer.reduce(start, intent, t0, e0, RecordSequence())
            assertEquals(first, second)
            assertEquals(snapshot, start)
        }
    }
}

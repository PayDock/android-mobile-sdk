package com.paydock.telemetry

import com.paydock.telemetry.TelemetryEffect.DropReason
import com.paydock.telemetry.TelemetryEvent.DroppedReason
import com.paydock.telemetry.TelemetryIntent.FlushSource

/**
 * Pure: no I/O, no clock, no randomness. The same state, intent, times and sequence give the same output, and the
 * input state is never mutated (R_01). Its only other input is the session's [RecordSequence], the source of every
 * record ID, `"<sessionId>-<sequence>"`.
 *
 * The disk is the queue: the reducer keeps counts, windows and notice IDs only. Level filtering, the hourly cap and
 * the dropped notices (R_09, R_10), the flush rules and the export stop (R_11, R_17, R_18). `now` is UTC epoch ms,
 * `elapsed` monotonic ms. Every limit comes from [TelemetryLimits].
 */
internal object TelemetryReducer {

    fun reduce(
        state: TelemetryState,
        intent: TelemetryIntent,
        now: Long, // UTC epoch ms
        elapsed: Long, // monotonic ms
        sequence: RecordSequence, // record IDs; the reducer's only other input
    ): Pair<TelemetryState, List<TelemetryEffect>> {
        val effects = mutableListOf<TelemetryEffect>()
        val next = when (intent) {
            is TelemetryIntent.Reported -> reported(state, intent.event, now, elapsed, sequence, effects)
            is TelemetryIntent.Flush -> {
                val ticked = if (intent.source == FlushSource.TIMER) state.copy(flushScheduled = false) else state
                export(ticked, now, effects)
            }
            TelemetryIntent.AppBackgrounded -> export(state, now, effects)
            is TelemetryIntent.LevelChanged -> state.copy(level = intent.level)
            is TelemetryIntent.RecordsDropped -> recordsDropped(state, intent, now, elapsed, sequence, effects)
            is TelemetryIntent.PendingCounted -> schedule(state.copy(pending = intent.count), effects)
            is TelemetryIntent.NoticesExported ->
                state.copy(notices = state.notices.filterValues { it.id !in intent.ids })
            TelemetryIntent.ExportStopped -> state.copy(exportStopped = true)
        }
        return next to effects
    }

    /** True for crashes, errors and network failures; false for WidgetOpened and Dropped. */
    fun isError(event: TelemetryEvent): Boolean = when (event) {
        is TelemetryEvent.Crash,
        is TelemetryEvent.UncaughtException,
        is TelemetryEvent.SdkError,
        is TelemetryEvent.NetworkFailure -> true
        is TelemetryEvent.WidgetOpened,
        is TelemetryEvent.Dropped -> false
    }

    private fun reported(
        state: TelemetryState,
        event: TelemetryEvent,
        now: Long,
        elapsed: Long,
        sequence: RecordSequence,
        effects: MutableList<TelemetryEffect>,
    ): TelemetryState {
        val dropReason = when {
            event is TelemetryEvent.Dropped -> DropReason.NOT_REPORTABLE
            !state.enabled -> DropReason.DISABLED
            state.level == TelemetryLevel.OFF -> DropReason.LEVEL_OFF
            state.level == TelemetryLevel.ERRORS && !isError(event) -> DropReason.LEVEL_FILTERED
            else -> null
        }
        if (dropReason != null) {
            effects += TelemetryEffect.Drop(dropReason)
            return state
        }
        val rolled = roll(state, elapsed)
        if (rolled.createdInWindow >= TelemetryLimits.HOURLY_CAP) {
            effects += TelemetryEffect.Drop(DropReason.RATE_LIMIT)
            return schedule(notice(rolled, DroppedReason.RATE_LIMIT, 1, now, sequence, effects), effects)
        }
        val id = newId(rolled, sequence)
        effects += TelemetryEffect.Persist(PendingRecord(id, rolled.sessionId, event, now))
        val created = rolled.copy(pending = rolled.pending + 1, createdInWindow = rolled.createdInWindow + 1)
        val exported = if (created.pending >= TelemetryLimits.FLUSH_THRESHOLD) export(created, now, effects) else created
        return schedule(exported, effects)
    }

    private fun recordsDropped(
        state: TelemetryState,
        intent: TelemetryIntent.RecordsDropped,
        now: Long,
        elapsed: Long,
        sequence: RecordSequence,
        effects: MutableList<TelemetryEffect>,
    ): TelemetryState {
        if (intent.count <= 0 || !state.enabled || state.level == TelemetryLevel.OFF) return state
        val noticed = notice(roll(state, elapsed), intent.reason, intent.count, now, sequence, effects)
        return schedule(noticed, effects)
    }

    /** Roll: a new hour when none has started or the current one is over, by the monotonic clock alone. */
    private fun roll(state: TelemetryState, elapsed: Long): TelemetryState {
        val start = state.windowStart
        return if (start == null || elapsed - start >= TelemetryLimits.HOUR_MILLIS) {
            state.copy(windowStart = elapsed, createdInWindow = 0, notices = emptyMap())
        } else {
            state
        }
    }

    /**
     * Notice: adds [count] to the reason's unsent notice, keeping its id, or starts one with a new id. Either way the
     * notice is persisted and raises `pending` by 1; the next count corrects any overshoot. Never counts toward the cap.
     */
    private fun notice(
        state: TelemetryState,
        reason: DroppedReason,
        count: Int,
        now: Long,
        sequence: RecordSequence,
        effects: MutableList<TelemetryEffect>,
    ): TelemetryState {
        val existing = state.notices[reason]
        val notice = existing?.copy(count = existing.count + count) ?: DroppedNotice(newId(state, sequence), count, now)
        effects += TelemetryEffect.Persist(
            PendingRecord(notice.id, state.sessionId, TelemetryEvent.Dropped(reason, notice.count), notice.createdAt)
        )
        return state.copy(pending = state.pending + 1, notices = state.notices + (reason to notice))
    }

    /** Export: everything on disk, in batches, unless sending has stopped for the process. */
    private fun export(state: TelemetryState, now: Long, effects: MutableList<TelemetryEffect>): TelemetryState {
        if (state.exportStopped) return state
        effects += TelemetryEffect.Export(TelemetryLimits.BATCH_MAX_RECORDS)
        return state.copy(pending = 0, lastExport = now)
    }

    /** Schedule: one timer while records are pending, none once sending has stopped. */
    private fun schedule(state: TelemetryState, effects: MutableList<TelemetryEffect>): TelemetryState {
        if (state.pending <= 0 || state.flushScheduled || state.exportStopped) return state
        effects += TelemetryEffect.ScheduleFlush(TelemetryLimits.FLUSH_INTERVAL_MILLIS)
        return state.copy(flushScheduled = true)
    }

    private fun newId(state: TelemetryState, sequence: RecordSequence): String = "${state.sessionId}-${sequence.next()}"
}

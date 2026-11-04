package com.paydock.telemetry

import com.paydock.telemetry.TelemetryEffect.DropReason
import com.paydock.telemetry.TelemetryEvent.NetworkError

/**
 * Pure: no I/O, no clock, no randomness. Same inputs, same outputs (R_01).
 *
 * Level filtering (R_05), duplicate collapse (R_06) and flush scheduling (R_07). Every time value is a UTC epoch
 * count in milliseconds (R_11).
 */
internal object TelemetryReducer {
    const val FLUSH_INTERVAL_MILLIS = 10_000L
    const val FLUSH_THRESHOLD = 20
    const val MAX_BATCH = 50
    const val COLLAPSE_WINDOW_MILLIS = 60_000L

    private const val PAYDOCK_FRAME_PREFIX = "com.paydock."

    fun reduce(
        state: TelemetryState,
        intent: TelemetryIntent,
        now: Long,
    ): Pair<TelemetryState, List<TelemetryEffect>> = when (intent) {
        is TelemetryIntent.Reported -> reported(state, intent.event, now)
        TelemetryIntent.Flush -> flush(state.copy(flushScheduled = false), now)
        TelemetryIntent.AppBackgrounded -> backgrounded(state, now)
        is TelemetryIntent.LevelChanged -> state.copy(level = intent.level) to emptyList()
    }

    /**
     * `"<name>|<exceptionType>|<topPaydockFrame>"`; a network failure appends `"|<method>|<urlTemplate>|<failure>"`,
     * where `failure` is `http_<code>`, `transport_<kind>` or `decode`.
     */
    fun fingerprint(event: TelemetryEvent): String = when (event) {
        is TelemetryEvent.Crash -> errorFingerprint("paydock.crash", event.error)
        is TelemetryEvent.UncaughtException -> errorFingerprint("paydock.error", event.error)
        is TelemetryEvent.SdkError -> errorFingerprint("paydock.error", event.error)
        is TelemetryEvent.NetworkFailure -> {
            val (error, failure) = networkFailureParts(event.failure)
            val base = errorFingerprint("paydock.network.failure", error)
            "$base|${event.method.name}|${event.urlTemplate}|$failure"
        }
    }

    /** True for all four kinds today. */
    fun isError(event: TelemetryEvent): Boolean = when (event) {
        is TelemetryEvent.Crash,
        is TelemetryEvent.UncaughtException,
        is TelemetryEvent.SdkError,
        is TelemetryEvent.NetworkFailure -> true
    }

    private fun reported(
        state: TelemetryState,
        event: TelemetryEvent,
        now: Long,
    ): Pair<TelemetryState, List<TelemetryEffect>> {
        val dropReason = when {
            !state.enabled -> DropReason.DISABLED
            state.level == TelemetryLevel.OFF -> DropReason.LEVEL_OFF
            state.level == TelemetryLevel.ERRORS && !isError(event) -> DropReason.LEVEL_FILTERED
            else -> null
        }
        if (dropReason != null) return state to listOf(TelemetryEffect.Drop(dropReason))

        val record = PendingRecord(
            id = "${state.sessionId}-${state.nextSequence}",
            sessionId = state.sessionId,
            event = event,
            firstSeenAt = now
        )
        val sequenced = state.copy(nextSequence = state.nextSequence + 1)
        val effects = mutableListOf<TelemetryEffect>()
        val held = if (event is TelemetryEvent.Crash) {
            effects += TelemetryEffect.Persist(record)
            effects += TelemetryEffect.Enqueue(record)
            sequenced.copy(persisted = sequenced.persisted + 1, queued = sequenced.queued + 1)
        } else {
            hold(sequenced, record, now, effects)
        }
        return afterReport(held, now, effects) to effects
    }

    /** Step 5: counts a repeat within its window, or releases an expired record and holds the new one. */
    private fun hold(
        state: TelemetryState,
        record: PendingRecord,
        now: Long,
        effects: MutableList<TelemetryEffect>,
    ): TelemetryState {
        val key = fingerprint(record.event)
        val existing = state.collapsing[key]
        if (existing != null && now - existing.firstSeenAt < COLLAPSE_WINDOW_MILLIS) {
            val counted = existing.copy(occurrences = existing.occurrences + 1)
            return state.copy(collapsing = state.collapsing + (key to counted))
        }
        val released = if (existing != null) release(state, key, effects) else state
        effects += TelemetryEffect.Persist(record)
        return released.copy(
            collapsing = released.collapsing + (key to record),
            persisted = released.persisted + 1
        )
    }

    /** Step 6: an export at the threshold, then a flush when anything is pending and none is scheduled. */
    private fun afterReport(state: TelemetryState, now: Long, effects: MutableList<TelemetryEffect>): TelemetryState {
        val exported = if (state.queued >= FLUSH_THRESHOLD) {
            effects += TelemetryEffect.Export(MAX_BATCH)
            state.copy(queued = state.queued - minOf(state.queued, MAX_BATCH), lastExport = now)
        } else {
            state
        }
        return schedule(exported, effects)
    }

    private fun schedule(state: TelemetryState, effects: MutableList<TelemetryEffect>): TelemetryState {
        val pending = state.queued > 0 || state.collapsing.isNotEmpty()
        if (!pending || state.flushScheduled) return state
        effects += TelemetryEffect.ScheduleFlush(FLUSH_INTERVAL_MILLIS)
        return state.copy(flushScheduled = true)
    }

    /** Releases the held records whose window has passed, exports everything queued, then reschedules. */
    private fun flush(state: TelemetryState, now: Long): Pair<TelemetryState, List<TelemetryEffect>> {
        val effects = mutableListOf<TelemetryEffect>()
        val expired = state.collapsing.filterValues { now - it.firstSeenAt >= COLLAPSE_WINDOW_MILLIS }.keys
        val released = expired.fold(state) { current, key -> release(current, key, effects) }
        val exported = exportAll(released, now, effects)
        return schedule(exported, effects) to effects
    }

    /** Releases every held record, whatever its age, and exports everything queued. Does not reschedule. */
    private fun backgrounded(state: TelemetryState, now: Long): Pair<TelemetryState, List<TelemetryEffect>> {
        val effects = mutableListOf<TelemetryEffect>()
        val released = state.collapsing.keys.fold(state) { current, key -> release(current, key, effects) }
        return exportAll(released, now, effects) to effects
    }

    private fun exportAll(state: TelemetryState, now: Long, effects: MutableList<TelemetryEffect>): TelemetryState {
        var queued = state.queued
        while (queued > 0) {
            effects += TelemetryEffect.Export(MAX_BATCH)
            queued -= minOf(queued, MAX_BATCH)
        }
        return if (queued == state.queued) state else state.copy(queued = queued, lastExport = now)
    }

    /** Rule R: removes the held record, enqueues it, and upserts it when it carries a count. */
    private fun release(state: TelemetryState, key: String, effects: MutableList<TelemetryEffect>): TelemetryState {
        val record = state.collapsing[key] ?: return state
        effects += TelemetryEffect.Enqueue(record)
        if (record.occurrences > 1) effects += TelemetryEffect.Persist(record)
        return state.copy(collapsing = state.collapsing - key, queued = state.queued + 1)
    }

    private fun errorFingerprint(name: String, error: TelemetryEvent.ErrorInfo?): String {
        val exceptionType = error?.type.orEmpty()
        val topPaydockFrame = error?.frames?.firstOrNull { it.startsWith(PAYDOCK_FRAME_PREFIX) }.orEmpty()
        return "$name|$exceptionType|$topPaydockFrame"
    }

    /** The transport or decode error, if any, and `http_<code>`, `transport_<kind>` or `decode`. */
    private fun networkFailureParts(failure: NetworkError): Pair<TelemetryEvent.ErrorInfo?, String> = when (failure) {
        is NetworkError.HttpStatus -> null to "http_${failure.code}"
        is NetworkError.Transport -> failure.error to "transport_${failure.kind.name.lowercase()}"
        is NetworkError.Decode -> failure.error to "decode"
    }
}

package com.paydock.telemetry

import com.paydock.telemetry.TelemetryEvent.DroppedReason
import com.paydock.telemetry.TelemetryIntent.RecordsDropped

/**
 * The only place with I/O, on the telemetry worker only. Runs each effect in turn and returns the follow-up intents
 * they produced, in order; a failing effect produces none and never stops the next (R_02).
 *
 * A persisted record is built by [RecordFactory] and filtered by [Allowlist] before it is stored; a record that lost
 * its `session.id` or `log.record.uid` is never stored. Every export carries the device context, so each batch holds
 * `app.installation.id` and each record its `session.id` (R_12).
 */
internal class TelemetryEffectHandler(
    private val store: DurableStore,
    private val runner: ExportRunner,
    private val deviceContext: DeviceContextProvider,
    private val clock: Clock,
    private val scheduleFlush: (delayMillis: Long) -> Unit,
) {

    /** The follow-up intents, in order; each effect wrapped, a failure never stops the next. */
    fun run(effects: List<TelemetryEffect>): List<TelemetryIntent> = effects.flatMap { effect ->
        try {
            runEffect(effect)
        } catch (ignored: Throwable) {
            emptyList() // A failing effect never stops the following ones.
        }
    }

    private fun runEffect(effect: TelemetryEffect): List<TelemetryIntent> = when (effect) {
        is TelemetryEffect.Persist -> persist(effect.record)
        is TelemetryEffect.Export -> export(effect.maxRecords)
        is TelemetryEffect.ScheduleFlush -> {
            scheduleFlush(effect.delayMillis)
            emptyList()
        }
        is TelemetryEffect.Drop -> emptyList()
    }

    private fun persist(pending: PendingRecord): List<TelemetryIntent> {
        val record = Allowlist.filter(RecordFactory.create(pending, deviceContext.networkConnectionType()))
        if (record.sessionId == null || record.value(RECORD_UID) !is AttributeValue.Str) {
            return listOf(RecordsDropped(DroppedReason.REJECTED, 1))
        }
        store.save(record)
        val pruned = store.prune(clock.now())
        return dropped(DroppedReason.TOO_OLD, pruned.tooOld) + dropped(DroppedReason.STORE_FULL, pruned.storeFull)
    }

    /** `ExportStopped` first, so no later follow-up can arm a timer; then the count, the notices sent, the drops. */
    private fun export(maxRecords: Int): List<TelemetryIntent> {
        val report = runner.flush(clock.now(), maxRecords)
        return buildList {
            if (report.stopped) add(TelemetryIntent.ExportStopped)
            add(TelemetryIntent.PendingCounted(report.pending))
            if (report.exportedNoticeIds.isNotEmpty()) add(TelemetryIntent.NoticesExported(report.exportedNoticeIds))
            addAll(dropped(DroppedReason.TOO_OLD, report.tooOld))
            addAll(dropped(DroppedReason.STORE_FULL, report.storeFull))
            addAll(dropped(DroppedReason.REJECTED, report.rejected))
        }
    }

    private fun dropped(reason: DroppedReason, count: Int): List<TelemetryIntent> =
        if (count > 0) listOf(RecordsDropped(reason, count)) else emptyList()

    private companion object {
        const val RECORD_UID = "log.record.uid"
    }
}

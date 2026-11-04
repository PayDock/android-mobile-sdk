package com.paydock.telemetry

/**
 * The only place with I/O. Runs each effect in turn; a failing effect never stops the others (R_02).
 *
 * Every export carries the device context, so each batch holds `app.installation.id` and each record its
 * `session.id` (R_12). It runs only on the telemetry worker.
 */
internal class TelemetryEffectHandler(
    private val queue: RecordQueue,
    private val store: DurableStore,
    private val exporter: Exporter,
    private val deviceContext: DeviceContextProvider, // attached to every export (R_12)
    private val scheduleFlush: (delayMillis: Long) -> Unit,
) {
    fun run(effects: List<TelemetryEffect>) {
        effects.forEach { effect ->
            try {
                runEffect(effect)
            } catch (ignored: Throwable) {
                // A failing effect never stops the following ones.
            }
        }
    }

    private fun runEffect(effect: TelemetryEffect) {
        when (effect) {
            is TelemetryEffect.Enqueue -> queue.offer(effect.record)
            is TelemetryEffect.Persist -> store.save(effect.record)
            is TelemetryEffect.ScheduleFlush -> scheduleFlush(effect.delayMillis)
            is TelemetryEffect.Export -> export(effect.maxRecords)
            is TelemetryEffect.Drop -> Unit
        }
    }

    private fun export(maxRecords: Int) {
        val records = queue.drain(maxRecords)
        if (records.isNotEmpty()) {
            exporter.export(ExportBatch(deviceContext.current(), records))
        }
    }
}

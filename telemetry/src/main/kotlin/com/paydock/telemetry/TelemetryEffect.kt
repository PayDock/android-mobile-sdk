package com.paydock.telemetry

/** What the reducer asks the handler to do. The reducer only describes work; [TelemetryEffectHandler] runs it. */
internal sealed interface TelemetryEffect {
    data class Enqueue(val record: PendingRecord) : TelemetryEffect
    data class Persist(val record: PendingRecord) : TelemetryEffect // upsert by record id
    data class ScheduleFlush(val delayMillis: Long) : TelemetryEffect
    data class Export(val maxRecords: Int) : TelemetryEffect // drain up to maxRecords and export them
    data class Drop(val reason: DropReason) : TelemetryEffect

    enum class DropReason { DISABLED, LEVEL_OFF, LEVEL_FILTERED }
}

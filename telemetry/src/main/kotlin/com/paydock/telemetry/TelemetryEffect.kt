package com.paydock.telemetry

/** What the reducer asks the handler to do. The reducer only describes work; [TelemetryEffectHandler] runs it. */
internal sealed interface TelemetryEffect {
    data class Persist(val record: PendingRecord) : TelemetryEffect // build, filter, upsert by record id
    data class ScheduleFlush(val delayMillis: Long) : TelemetryEffect
    data class Export(val maxRecords: Int) : TelemetryEffect // flush everything pending, batches of maxRecords
    data class Drop(val reason: DropReason) : TelemetryEffect

    enum class DropReason { DISABLED, LEVEL_OFF, LEVEL_FILTERED, RATE_LIMIT, NOT_REPORTABLE }
}

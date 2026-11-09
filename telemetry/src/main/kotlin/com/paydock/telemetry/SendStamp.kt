package com.paydock.telemetry

/**
 * The outgoing copy of a stored record (D20, spec § 5.2, § 6). Pure. The record's own time and [sentAt] are device
 * times; both add [offsetMillis]. When the corrected send time is more than [TelemetryLimits.ORDERING_WINDOW_MILLIS]
 * after the corrected record time, the copy gets the send time and `paydock.occurred_at` = the corrected record time
 * as [UtcTimestamp.iso8601], inserted by [TelemetryRecord.with]; otherwise the corrected record time and no new
 * attribute. The body is unchanged. Mirrors `stamp()` of the fixtures' generate.py.
 */
internal object SendStamp {
    const val OCCURRED_AT = "paydock.occurred_at"

    fun stamp(record: TelemetryRecord, sentAt: Long, offsetMillis: Long): TelemetryRecord {
        val occurred = record.timeUnixMillis + offsetMillis
        val sent = sentAt + offsetMillis
        return if (sent - occurred > TelemetryLimits.ORDERING_WINDOW_MILLIS) {
            record.copy(timeUnixMillis = sent).with(OCCURRED_AT, AttributeValue.Str(UtcTimestamp.iso8601(occurred)))
        } else {
            record.copy(timeUnixMillis = occurred)
        }
    }
}

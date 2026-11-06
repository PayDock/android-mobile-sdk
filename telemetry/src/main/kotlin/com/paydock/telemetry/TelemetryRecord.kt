package com.paydock.telemetry

/**
 * One OTLP log record, as built by [RecordFactory] and kept by the store. Attributes are in
 * [Allowlist.RECORD_KEYS] order, each key at most once.
 */
internal data class TelemetryRecord(
    val id: String, // log.record.uid
    val timeUnixMillis: Long, // UTC epoch ms; written as timeUnixNano = ms * 1_000_000
    val severity: Severity,
    val body: String,
    val attributes: List<Attribute>,
) {
    /** The session.id string, if present. */
    val sessionId: String?
        get() = (value(SESSION_ID) as? AttributeValue.Str)?.value

    /** The event.name string, if present. */
    val eventName: String?
        get() = (value(EVENT_NAME) as? AttributeValue.Str)?.value

    /** True for a dropped-records notice (spec § 5.8). */
    val isDroppedNotice: Boolean
        get() = eventName == DROPPED_EVENT_NAME

    fun value(key: String): AttributeValue? = attributes.firstOrNull { it.key == key }?.value

    /** Replaces the key's value, or inserts it in [Allowlist.RECORD_KEYS] order; an unlisted key goes last. */
    fun with(key: String, value: AttributeValue): TelemetryRecord {
        val order = Allowlist.RECORD_KEYS.keys.toList()
        val position = { attribute: Attribute -> order.indexOf(attribute.key).takeIf { it >= 0 } ?: order.size }
        val updated = attributes.filterNot { it.key == key } + Attribute(key, value)
        return copy(attributes = updated.sortedBy(position))
    }

    private companion object {
        const val SESSION_ID = "session.id"
        const val EVENT_NAME = "event.name"
        const val DROPPED_EVENT_NAME = "paydock.telemetry.dropped"
    }
}

package com.paydock.telemetry

/**
 * Merges exact duplicates within one session (R_06, MD-27). Pure. Two records merge only when their [signature]s are
 * equal: the same session, event name and every signature field. A notice, a payment record, or a record without
 * `session.id`, is never merged.
 *
 * The kept record is the group's earliest, by time then the id's sequence as a number, and keeps its own id.
 * `paydock.occurrences` becomes the sum of the members' occurrences (absent counts as 1), set only above 1, and
 * `paydock.http.attempt` the highest, when present.
 */
internal object DuplicateGate {

    private const val SEPARATOR = "\u0000"
    private const val OCCURRENCES = "paydock.occurrences"
    private const val HTTP_ATTEMPT = "paydock.http.attempt"

    /** Each payment record is one attempt's start or end, so two equal ones are still two (R_04). */
    private val NEVER_MERGED = setOf("paydock.payment.started", "paydock.payment.finished")

    private val COMMON_KEYS = listOf("exception.type", "exception.stacktrace", "paydock.widget")

    /** The keys each event adds after [COMMON_KEYS]; widget opened adds none. */
    private val EVENT_KEYS: Map<String, List<String>> = mapOf(
        "paydock.crash" to listOf("paydock.crash.kind", "paydock.crash.source", "paydock.crash.reason"),
        "paydock.error" to listOf("paydock.error.kind", "paydock.error.code"),
        "paydock.network.failure" to listOf(
            "http.request.method",
            "url.template",
            "http.response.status_code",
            "error.type",
            "paydock.api.error.code",
            "paydock.api.error.summary_code",
            "paydock.api.gateway.codes"
        )
    )

    private val EARLIEST: Comparator<TelemetryRecord> =
        compareBy<TelemetryRecord>({ it.timeUnixMillis }, { sequenceOf(it.id) }, { it.id })

    /** The merged records, in input order by each group's first record. */
    fun merge(records: List<TelemetryRecord>): List<MergedRecord> {
        val groups = LinkedHashMap<Any, MutableList<TelemetryRecord>>()
        records.forEach { record ->
            val key: Any = signature(record) ?: Any()
            groups.getOrPut(key) { mutableListOf() } += record
        }
        return groups.values.map(::mergeGroup)
    }

    /** `"<session.id>\u0000<event.name>"`, then `"\u0000<key>=<value>"` per signature key present; null: never merged. */
    fun signature(record: TelemetryRecord): String? {
        val sessionId = record.sessionId
        if (sessionId == null || record.isDroppedNotice || record.eventName in NEVER_MERGED) return null
        val eventName = record.eventName.orEmpty()
        val keys = COMMON_KEYS + EVENT_KEYS[eventName].orEmpty()
        return buildString {
            append(sessionId).append(SEPARATOR).append(eventName)
            keys.forEach { key ->
                record.value(key)?.let { append(SEPARATOR).append(key).append('=').append(text(it)) }
            }
        }
    }

    private fun mergeGroup(group: List<TelemetryRecord>): MergedRecord {
        if (group.size == 1) return MergedRecord(group.single(), listOf(group.single().id))
        val kept = group.minWith(EARLIEST)
        val occurrences = group.sumOf { (it.value(OCCURRENCES) as? AttributeValue.Num)?.value ?: 1L }
        val attempt = group.mapNotNull { (it.value(HTTP_ATTEMPT) as? AttributeValue.Num)?.value }.maxOrNull()
        var merged = kept
        if (occurrences > 1) merged = merged.with(OCCURRENCES, AttributeValue.Num(occurrences))
        if (attempt != null) merged = merged.with(HTTP_ATTEMPT, AttributeValue.Num(attempt))
        return MergedRecord(merged, group.map { it.id })
    }

    private fun text(value: AttributeValue): String = when (value) {
        is AttributeValue.Str -> value.value
        is AttributeValue.Num -> value.value.toString()
        is AttributeValue.Bool -> value.value.toString()
    }

    /** The number after the id's last `-`; an id without one sorts last. */
    private fun sequenceOf(id: String): Long = id.substringAfterLast('-').toLongOrNull() ?: Long.MAX_VALUE
}

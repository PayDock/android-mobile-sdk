package com.paydock.telemetry

/** The on-disk store of records not yet exported: the only queue. */
internal interface DurableStore {
    fun save(record: TelemetryRecord) // upsert by record id; atomic

    /** [save], waiting at most [timeoutMillis] for the store; false when not saved in time (the crash path). */
    fun trySave(record: TelemetryRecord, timeoutMillis: Long): Boolean = run {
        save(record)
        true
    }

    fun oldest(max: Int): List<TelemetryRecord> // by timeUnixMillis, then the id's sequence as a number
    fun remove(ids: Collection<String>)
    fun prune(now: Long): PruneResult // now: UTC epoch ms
    fun count(): Int
}

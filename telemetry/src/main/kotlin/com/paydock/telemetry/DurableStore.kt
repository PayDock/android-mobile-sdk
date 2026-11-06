package com.paydock.telemetry

/** The on-disk store of records not yet exported: the only queue. */
internal interface DurableStore {
    fun save(record: TelemetryRecord) // upsert by record id; atomic
    fun oldest(max: Int): List<TelemetryRecord> // by timeUnixMillis, then the id's sequence as a number
    fun remove(ids: Collection<String>)
    fun prune(now: Long): PruneResult // now: UTC epoch ms
    fun count(): Int
}

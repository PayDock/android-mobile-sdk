package com.paydock.telemetry

/** The on-disk store of records not yet exported. [save] upserts by record id. */
internal interface DurableStore {
    fun save(record: PendingRecord)
    fun loadAll(): List<PendingRecord>
    fun remove(ids: Collection<String>)
    fun prune(now: Long)
}

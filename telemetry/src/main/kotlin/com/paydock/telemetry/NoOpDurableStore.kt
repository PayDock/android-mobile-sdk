package com.paydock.telemetry

/** Writes nothing: no file is created yet (R_02). */
internal object NoOpDurableStore : DurableStore {
    override fun save(record: PendingRecord) = Unit
    override fun loadAll(): List<PendingRecord> = emptyList()
    override fun remove(ids: Collection<String>) = Unit
    override fun prune(now: Long) = Unit
}

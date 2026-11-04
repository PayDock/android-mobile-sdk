package com.paydock.telemetry

/** Keeps nothing: nothing leaves memory yet (R_02). */
internal object NoOpRecordQueue : RecordQueue {
    override fun offer(record: PendingRecord) = Unit
    override fun drain(max: Int): List<PendingRecord> = emptyList()
    override fun size(): Int = 0
}

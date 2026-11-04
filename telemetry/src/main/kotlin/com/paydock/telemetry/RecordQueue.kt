package com.paydock.telemetry

/** The in-memory queue of records waiting for export. */
internal interface RecordQueue {
    fun offer(record: PendingRecord)
    fun drain(max: Int): List<PendingRecord>
    fun size(): Int
}

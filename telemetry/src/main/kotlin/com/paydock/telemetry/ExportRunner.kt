package com.paydock.telemetry

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * One flush of the disk queue at a time, on the worker (R_12, R_13, R_18). It prunes the store, merges duplicates,
 * cuts the records into batches of at most `maxRecords` records and [TelemetryLimits.BATCH_MAX_BYTES], oldest first,
 * and exports each in turn. Files are removed only after a success, a rejection or a single-record 413.
 *
 * It keeps its own attempt count: a retryable failure schedules a later flush through [scheduleRetry] and ends this
 * one, and a flush before that time sends nothing. A 401/403, or [TelemetryLimits.RETRY_MAX_ATTEMPTS] failed
 * attempts, stops sending for the process. It never sleeps and never throws.
 */
internal class ExportRunner(
    private val store: DurableStore,
    private val exporter: Exporter,
    private val deviceContext: DeviceContextProvider,
    private val monotonic: MonotonicClock,
    private val retryPolicy: RetryPolicy,
    private val scheduleRetry: (delayMillis: Long) -> Unit,
) {

    /** A merged record and its encoded size, measured once. */
    private class Sized(val merged: MergedRecord, val bytes: Int)

    /** What this flush has done so far. */
    private class Tally {
        var rejected = 0
        var stopped = false
        val noticeIds = linkedSetOf<String>()
    }

    private var failedAttempts = 0
    private var nextAttemptAt: Long? = null
    private var stopped = false

    /** Worker only; never throws. [now]: UTC epoch ms; [maxRecords]: records per batch, from the Export effect. */
    @Suppress("TooGenericExceptionCaught", "SwallowedException") // a failed flush ends; the next one starts afresh
    fun flush(now: Long, maxRecords: Int): FlushReport = try {
        runFlush(now, maxRecords)
    } catch (ignored: Throwable) {
        FlushReport(pending = runCatching { store.count() }.getOrDefault(0))
    }

    private fun runFlush(now: Long, maxRecords: Int): FlushReport {
        val before = nextAttemptAt
        if (stopped || (before != null && monotonic.elapsed() < before)) return FlushReport(pending = store.count())

        val pruned = store.prune(now)
        val tally = Tally()
        val resource = deviceContext.current()
        if (resource.appInstallationId.isNotBlank()) send(resource, maxRecords.coerceAtLeast(1), tally)
        return FlushReport(
            tooOld = pruned.tooOld,
            storeFull = pruned.storeFull,
            rejected = tally.rejected,
            pending = store.count(),
            stopped = tally.stopped,
            exportedNoticeIds = tally.noticeIds.toSet()
        )
    }

    private fun send(resource: ResourceAttributes, maxRecords: Int, tally: Tally) {
        val merged = DuplicateGate.merge(store.oldest(TelemetryLimits.STORE_MAX_RECORDS))
        val (sendable, unusable) = merged.partition { isSendable(it.record) }
        unusable.forEach { remove(listOf(Sized(it, 0)), tally, rejected = it.sourceIds.size.toLong()) }

        val envelope = OtlpLogEncoder.encode(resource, emptyList()).size
        val queue = ArrayDeque(batches(sendable.map { Sized(it, encodedSize(it.record)) }, maxRecords, envelope))
        while (queue.isNotEmpty()) {
            val batch = queue.removeFirst()
            val sources = batch.sumOf { it.merged.sourceIds.size }.toLong()
            when (val result = exporter.export(ExportBatch(resource, batch.map { it.merged.record }))) {
                is ExportResult.Success -> {
                    failedAttempts = 0
                    nextAttemptAt = null
                    remove(batch, tally, rejected = minOf(result.rejectedRecords.coerceAtLeast(0), sources))
                }
                ExportResult.TooLarge -> if (batch.size > 1) {
                    queue.addFirst(batch.subList(batch.size / 2, batch.size))
                    queue.addFirst(batch.subList(0, batch.size / 2))
                } else {
                    remove(batch, tally, rejected = sources)
                }
                ExportResult.Rejected -> remove(batch, tally, rejected = sources)
                ExportResult.Unauthorized -> return stop(tally)
                is ExportResult.Retry -> return retry(result.retryAfterMillis, tally)
            }
        }
    }

    /** Removes the batch's sources and counts [rejected]; its notices count as exported. */
    private fun remove(batch: List<Sized>, tally: Tally, rejected: Long) {
        store.remove(batch.flatMap { it.merged.sourceIds })
        tally.rejected += rejected.toInt()
        batch.map { it.merged.record }.filter { it.isDroppedNotice }.forEach { tally.noticeIds += it.id }
    }

    private fun retry(retryAfterMillis: Long?, tally: Tally) {
        failedAttempts++
        if (failedAttempts >= TelemetryLimits.RETRY_MAX_ATTEMPTS) return stop(tally)
        val delay = retryPolicy.delayMillis(failedAttempts, retryAfterMillis)
        nextAttemptAt = monotonic.elapsed() + delay
        runCatching { scheduleRetry(delay) }
    }

    private fun stop(tally: Tally) {
        stopped = true
        tally.stopped = true
    }

    /** Oldest first; at most [maxRecords] records and [TelemetryLimits.BATCH_MAX_BYTES] each; a larger record alone. */
    private fun batches(records: List<Sized>, maxRecords: Int, envelope: Int): List<List<Sized>> {
        val batches = mutableListOf<List<Sized>>()
        var current = mutableListOf<Sized>()
        var bytes = envelope
        records.forEach { record ->
            val full = current.size >= maxRecords || bytes + record.bytes > TelemetryLimits.BATCH_MAX_BYTES
            if (current.isNotEmpty() && full) {
                batches += current
                current = mutableListOf()
                bytes = envelope
            }
            current += record
            bytes += record.bytes
        }
        if (current.isNotEmpty()) batches += current
        return batches
    }

    /** The record's JSON in the request, plus its separator. */
    private fun encodedSize(record: TelemetryRecord): Int =
        Json.encodeToString(JsonElement.serializer(), OtlpLogEncoder.recordJson(record))
            .toByteArray(Charsets.UTF_8).size + 1

    private fun isSendable(record: TelemetryRecord): Boolean =
        record.sessionId != null && record.value(RECORD_UID) is AttributeValue.Str

    private companion object {
        const val RECORD_UID = "log.record.uid"
    }
}

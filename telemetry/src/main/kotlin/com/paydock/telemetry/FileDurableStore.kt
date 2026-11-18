package com.paydock.telemetry

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * The disk queue (R_07, R_08): one `<id>.json` file per record in [directory], holding
 * [OtlpLogEncoder.recordJson]. A save writes `<id>.json.tmp`, then renames it, so a record is whole or absent.
 *
 * An in-memory index (id → time, file size, notice or not) is built once from the directory on first use, then kept
 * up to date by every save and remove: [count], the [oldest] order and the cap and age checks of [prune] read it, so
 * a save never re-reads every file. The disk stays the only queue; the index is a view of it, rebuilt on each
 * launch. A file that reads but does not parse, or whose id differs from its name, is deleted and skipped, when the
 * index is built and when [oldest] reads it. A file that cannot be read (an I/O error) is kept and skipped: it is
 * not indexed when the index is built, and keeps its index entry at [oldest], so it is tried again and ages out.
 *
 * The directory is resolved on first use, on the worker, and created only by the first [save]: a read of a missing
 * directory returns empty and creates nothing. Every method holds the store's lock, because a crash handler writes
 * from the crashing thread; the crash path ([trySave]) waits for it at most [TelemetryLimits.CRASH_STORE_WAIT_MILLIS].
 * Limits come from [TelemetryLimits].
 */
internal class FileDurableStore(
    private val readText: (File) -> String = { it.readText(Charsets.UTF_8) }, // a seam for the I/O-error tests
    directory: () -> File, // resolved on first use, on the worker; last, so callers keep the trailing lambda
) : DurableStore {

    private class Entry(val time: Long, val size: Long, val isNotice: Boolean)

    private val directory: File by lazy(directory)

    private val lock = ReentrantLock()

    private var entries: MutableMap<String, Entry>? = null

    override fun save(record: TelemetryRecord) {
        lock.withLock { saveLocked(record) }
    }

    /** Waits at most [timeoutMillis] for the lock; false, and nothing written, when it is not acquired in time. */
    override fun trySave(record: TelemetryRecord, timeoutMillis: Long): Boolean {
        val acquired = try {
            lock.tryLock(timeoutMillis, TimeUnit.MILLISECONDS)
        } catch (interrupted: InterruptedException) {
            Thread.currentThread().interrupt()
            false
        }
        if (!acquired) return false
        try {
            saveLocked(record)
        } finally {
            lock.unlock()
        }
        return true
    }

    private fun saveLocked(record: TelemetryRecord) {
        require(isSafeId(record.id)) { "unusable record id" }
        val index = index()
        val dir = directory
        if (!dir.isDirectory && !dir.mkdirs() && !dir.isDirectory) throw IOException("store directory not created")
        val bytes = Json.encodeToString(JsonElement.serializer(), OtlpLogEncoder.recordJson(record))
            .toByteArray(Charsets.UTF_8)
        val temporary = File(dir, record.id + TEMPORARY_SUFFIX)
        temporary.writeBytes(bytes)
        if (!temporary.renameTo(fileOf(record.id))) {
            temporary.delete()
            throw IOException("store file not renamed")
        }
        index[record.id] = Entry(record.timeUnixMillis, bytes.size.toLong(), record.isDroppedNotice)
    }

    override fun oldest(max: Int): List<TelemetryRecord> = lock.withLock {
        val index = index()
        val result = mutableListOf<TelemetryRecord>()
        for (id in orderedIds(index)) {
            if (result.size >= max) break
            when (val read = read(fileOf(id))) {
                Read.Unreadable -> Unit // kept, and still indexed: tried again on the next read
                is Read.Record -> if (read.record.id == id) {
                    result += read.record
                } else {
                    discard(id, index)
                }
                Read.Invalid -> discard(id, index)
            }
        }
        result
    }

    private fun discard(id: String, index: MutableMap<String, Entry>) {
        fileOf(id).delete()
        index.remove(id)
    }

    override fun remove(ids: Collection<String>) {
        lock.withLock {
            val index = index()
            ids.forEach { id ->
                if (isSafeId(id)) fileOf(id).delete()
                index.remove(id)
            }
        }
    }

    /**
     * Deletes leftover temporary files, then records older than [TelemetryLimits.RECORD_MAX_AGE_MILLIS]. When the
     * store is still over [TelemetryLimits.STORE_MAX_RECORDS] or [TelemetryLimits.STORE_MAX_BYTES], it merges its
     * duplicates on disk, then removes the oldest record, notices last, until it is within both caps or empty.
     */
    override fun prune(now: Long): PruneResult = lock.withLock {
        val index = index()
        directory.listFiles()?.filter { it.name.endsWith(TEMPORARY_SUFFIX) }?.forEach { it.delete() }

        val tooOld = index.filterValues { now - it.time > TelemetryLimits.RECORD_MAX_AGE_MILLIS }
        remove(tooOld.keys)
        if (isOverCaps(index)) mergeDuplicates()

        var storeFull = 0
        while (isOverCaps(index)) {
            val ordered = orderedIds(index)
            val victim = ordered.firstOrNull { index[it]?.isNotice == false } ?: ordered.firstOrNull() ?: break
            if (index[victim]?.isNotice == false) storeFull++
            remove(listOf(victim))
        }
        PruneResult(tooOld = tooOld.values.count { !it.isNotice }, storeFull = storeFull)
    }

    override fun count(): Int = lock.withLock { index().size }

    /** Saves each merged record under its kept id first, then removes its other sources. */
    private fun mergeDuplicates() {
        DuplicateGate.merge(oldest(Int.MAX_VALUE)).filter { it.sourceIds.size > 1 }.forEach { merged ->
            save(merged.record)
            remove(merged.sourceIds - merged.record.id)
        }
    }

    private fun isOverCaps(index: Map<String, Entry>): Boolean =
        index.size > TelemetryLimits.STORE_MAX_RECORDS || index.values.sumOf { it.size } > TelemetryLimits.STORE_MAX_BYTES

    /** By time, then the id's sequence as a number, so `-9` comes before `-10`. */
    private fun orderedIds(index: Map<String, Entry>): List<String> =
        index.entries.sortedWith(
            compareBy<Map.Entry<String, Entry>>({ it.value.time }, { sequenceOf(it.key) }, { it.key })
        ).map { it.key }

    /** Built once, from the directory, on first use; a missing directory gives an empty index and creates nothing. */
    private fun index(): MutableMap<String, Entry> = entries ?: buildIndex().also { entries = it }

    private fun buildIndex(): MutableMap<String, Entry> {
        val index = mutableMapOf<String, Entry>()
        directory.listFiles()?.filter { it.isFile && it.name.endsWith(SUFFIX) }?.forEach { file ->
            val id = file.name.removeSuffix(SUFFIX)
            when (val read = read(file)) {
                Read.Unreadable -> Unit // kept, not indexed: tried again on the next launch
                is Read.Record -> if (read.record.id == id) {
                    index[id] = Entry(read.record.timeUnixMillis, file.length(), read.record.isDroppedNotice)
                } else {
                    file.delete()
                }
                Read.Invalid -> file.delete()
            }
        }
        return index
    }

    /** What reading one file gave: a record, a file that cannot be read now, or one that never will parse. */
    private sealed interface Read {
        class Record(val record: TelemetryRecord) : Read
        data object Unreadable : Read
        data object Invalid : Read
    }

    @Suppress("SwallowedException") // an unreadable file is kept and tried again; one that does not parse is deleted
    private fun read(file: File): Read {
        val text = try {
            readText(file)
        } catch (ignored: IOException) {
            return Read.Unreadable
        }
        return try {
            val record = (Json.parseToJsonElement(text) as? JsonObject)?.let(OtlpLogEncoder::parseRecord)
            if (record == null) Read.Invalid else Read.Record(record)
        } catch (ignored: Exception) {
            Read.Invalid
        }
    }

    private fun fileOf(id: String): File = File(directory, id + SUFFIX)

    private fun isSafeId(id: String): Boolean =
        id.isNotEmpty() && !id.startsWith(".") && id.none { it == '/' || it == '\\' || it == File.separatorChar }

    private fun sequenceOf(id: String): Long = id.substringAfterLast('-').toLongOrNull() ?: Long.MAX_VALUE

    companion object {
        const val DIRECTORY_NAME = "paydock_telemetry"

        private const val SUFFIX = ".json"
        private const val TEMPORARY_SUFFIX = ".json.tmp"
    }
}

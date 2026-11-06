package com.paydock.telemetry

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.io.File
import java.io.IOException

/**
 * The disk queue (R_07, R_08): one `<id>.json` file per record in [directory], holding
 * [OtlpLogEncoder.recordJson]. A save writes `<id>.json.tmp`, then renames it, so a record is whole or absent.
 *
 * An in-memory index (id → time, file size, notice or not) is built once from the directory on first use, then kept
 * up to date by every save and remove: [count], the [oldest] order and the cap and age checks of [prune] read it, so
 * a save never re-reads every file. The disk stays the only queue; the index is a view of it, rebuilt on each
 * launch. A file that does not parse is deleted and skipped, when the index is built and when [oldest] reads it.
 *
 * The directory is resolved on first use, on the worker, and created only by the first [save]: a read of a missing
 * directory returns empty and creates nothing. Every method is synchronized, because a crash handler will write
 * from the crashing thread. Limits come from [TelemetryLimits].
 */
internal class FileDurableStore(
    directory: () -> File, // resolved on first use, on the worker
) : DurableStore {

    private class Entry(val time: Long, val size: Long, val isNotice: Boolean)

    private val directory: File by lazy(directory)

    private var entries: MutableMap<String, Entry>? = null

    @Synchronized
    override fun save(record: TelemetryRecord) {
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

    @Synchronized
    override fun oldest(max: Int): List<TelemetryRecord> {
        val index = index()
        val result = mutableListOf<TelemetryRecord>()
        for (id in orderedIds(index)) {
            if (result.size >= max) break
            val record = read(fileOf(id))
            if (record == null || record.id != id) {
                fileOf(id).delete()
                index.remove(id)
            } else {
                result += record
            }
        }
        return result
    }

    @Synchronized
    override fun remove(ids: Collection<String>) {
        val index = index()
        ids.forEach { id ->
            if (isSafeId(id)) fileOf(id).delete()
            index.remove(id)
        }
    }

    /**
     * Deletes leftover temporary files, then records older than [TelemetryLimits.RECORD_MAX_AGE_MILLIS]. When the
     * store is still over [TelemetryLimits.STORE_MAX_RECORDS] or [TelemetryLimits.STORE_MAX_BYTES], it merges its
     * duplicates on disk, then removes the oldest record, notices last, until it is within both caps or empty.
     */
    @Synchronized
    override fun prune(now: Long): PruneResult {
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
        return PruneResult(tooOld = tooOld.values.count { !it.isNotice }, storeFull = storeFull)
    }

    @Synchronized
    override fun count(): Int = index().size

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
            val record = read(file)
            if (record == null || record.id != id) {
                file.delete()
            } else {
                index[id] = Entry(record.timeUnixMillis, file.length(), record.isDroppedNotice)
            }
        }
        return index
    }

    @Suppress("SwallowedException") // an unreadable file is skipped and deleted; nothing of it is kept
    private fun read(file: File): TelemetryRecord? = try {
        (Json.parseToJsonElement(file.readText(Charsets.UTF_8)) as? JsonObject)?.let(OtlpLogEncoder::parseRecord)
    } catch (ignored: Exception) {
        null
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

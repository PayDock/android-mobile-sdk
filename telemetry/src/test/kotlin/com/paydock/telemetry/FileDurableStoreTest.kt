package com.paydock.telemetry

import com.paydock.telemetry.AttributeValue.Num
import com.paydock.telemetry.AttributeValue.Str
import com.paydock.telemetry.TelemetryLimits.RECORD_MAX_AGE_MILLIS
import com.paydock.telemetry.TelemetryLimits.STORE_MAX_BYTES
import com.paydock.telemetry.TelemetryLimits.STORE_MAX_RECORDS
import com.paydock.telemetry.TelemetryTestFakes.HeldFileStore
import com.paydock.telemetry.TelemetryTestFakes.SESSION_ID
import com.paydock.telemetry.TelemetryTestFakes.T0
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Unit tests for [FileDurableStore], the disk queue (R_07, R_08), on a temporary folder. */
internal class FileDurableStoreTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val directory: File by lazy { File(folder.root, FileDurableStore.DIRECTORY_NAME) }

    private fun store() = FileDurableStore { directory }

    /** Each sequence its own error type, so the duplicate gate never merges two of them. */
    private fun record(sequence: Long, createdAt: Long = T0 + sequence) = TelemetryTestFakes.record(
        sequence,
        event = TelemetryTestFakes.sdkError("test.Error$sequence"),
        createdAt = createdAt
    )

    private fun notice(sequence: Long, createdAt: Long = T0 + sequence) = TelemetryTestFakes.record(
        sequence,
        event = TelemetryEvent.Dropped(TelemetryEvent.DroppedReason.STORE_FULL, 1),
        createdAt = createdAt
    )

    /** A distinct record of about 14 KB on disk. */
    private fun large(sequence: Long) =
        record(sequence).with("exception.stacktrace", Str("\tat test.Frame$sequence.run(Frame.kt:1)\n".repeat(400)))

    private fun jsonFiles(): List<String> =
        directory.listFiles().orEmpty().map { it.name }.filter { it.endsWith(".json") }.sorted()

    @Test
    fun trySave_withTheLockFree_savesAndReturnsTrue() {
        val store = store()

        assertTrue(store.trySave(record(1), TelemetryLimits.CRASH_STORE_WAIT_MILLIS))

        assertEquals(listOf("$SESSION_ID-1.json"), jsonFiles())
        assertEquals(1, store.count())
    }

    /** R_07: a held lock makes the timed save give up after its timeout, writing nothing. */
    @Test
    fun trySave_withTheLockHeld_returnsFalseAfterTheTimeout_andWritesNothing() {
        val held = HeldFileStore(directory)
        try {
            val started = System.nanoTime()

            val saved = held.store.trySave(record(1), SHORT_WAIT_MILLIS)

            val waitedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started)
            assertFalse(saved)
            assertTrue("waited $waitedMillis ms", waitedMillis >= SHORT_WAIT_MILLIS)
            assertTrue("waited $waitedMillis ms", waitedMillis < SHORT_WAIT_MILLIS + SLACK_MILLIS)
        } finally {
            held.release()
        }
        assertEquals(emptyList<String>(), jsonFiles())
        assertEquals(0, held.store.count())
        assertTrue(held.store.trySave(record(2), SHORT_WAIT_MILLIS)) // the lock is free again
        assertEquals(listOf("$SESSION_ID-2.json"), jsonFiles())
    }

    @Test
    fun trySave_onAnInterruptedThread_returnsFalse_andKeepsTheInterrupt() {
        val held = HeldFileStore(directory)
        try {
            Thread.currentThread().interrupt()

            assertFalse(held.store.trySave(record(1), SHORT_WAIT_MILLIS))
            assertTrue(Thread.interrupted()) // clears the flag for the next test
        } finally {
            held.release()
        }
        assertEquals(emptyList<String>(), jsonFiles())
    }

    /** Every caller but the crash path keeps blocking: a save waits for the lock however long it is held. */
    @Test
    fun save_withTheLockHeld_waitsForIt_thenSaves() {
        val held = HeldFileStore(directory)
        val done = CountDownLatch(1)
        val saver = Thread {
            held.store.save(record(1))
            done.countDown()
        }.apply {
            isDaemon = true
            start()
        }

        assertFalse(done.await(SHORT_WAIT_MILLIS + SLACK_MILLIS, TimeUnit.MILLISECONDS))
        held.release()

        assertTrue(done.await(SLACK_MILLIS, TimeUnit.MILLISECONDS))
        saver.join()
        assertEquals(listOf("$SESSION_ID-1.json"), jsonFiles())
    }

    @Test
    fun save_writesOneFilePerRecord_andUpsertsById() {
        val store = store()
        store.save(record(1))
        store.save(record(2))

        store.save(record(1).with("paydock.occurrences", Num(5)))

        assertEquals(2, store.count())
        assertEquals(listOf("$SESSION_ID-1.json", "$SESSION_ID-2.json"), jsonFiles())
        assertEquals(Num(5), store.oldest(1).single().value("paydock.occurrences"))
    }

    @Test
    fun save_writesTheRecordJson_andLeavesNoTemporaryFile() {
        val store = store()

        store.save(record(1))

        val file = File(directory, "$SESSION_ID-1.json")
        assertEquals(
            Json.parseToJsonElement(file.readText()),
            OtlpLogEncoder.recordJson(record(1))
        )
        assertEquals(listOf("$SESSION_ID-1.json"), directory.list()!!.toList())
    }

    @Test
    fun oldest_isByTime_andAtMostMax() {
        val store = store()
        listOf(record(3, T0 + 1), record(1, T0 + 3), record(2, T0 + 2)).forEach(store::save)

        assertEquals(listOf("$SESSION_ID-3", "$SESSION_ID-2", "$SESSION_ID-1"), store.oldest(10).map { it.id })
        assertEquals(listOf("$SESSION_ID-3", "$SESSION_ID-2"), store.oldest(2).map { it.id })
        assertTrue(store.oldest(0).isEmpty())
    }

    @Test
    fun oldest_ordersTheSequenceNumerically() {
        val store = store()
        store.save(record(10, T0))
        store.save(record(9, T0))
        store.save(record(100, T0))

        assertEquals(listOf("$SESSION_ID-9", "$SESSION_ID-10", "$SESSION_ID-100"), store.oldest(3).map { it.id })
    }

    @Test
    fun remove_deletesTheFiles() {
        val store = store()
        (1L..3L).forEach { store.save(record(it)) }

        store.remove(listOf("$SESSION_ID-1", "$SESSION_ID-3", "$SESSION_ID-404"))

        assertEquals(1, store.count())
        assertEquals(listOf("$SESSION_ID-2.json"), jsonFiles())
    }

    @Test
    fun aNewInstanceOnTheSameFolder_readsEveryRecord_asAfterProcessDeath() {
        val records = (1L..5L).map { record(it) }
        store().apply { records.forEach(::save) }

        val restarted = store()

        assertEquals(5, restarted.count())
        assertEquals(records, restarted.oldest(10))
    }

    @Test
    fun prune_keepsAtMost100Records_removingTheOldest() {
        val store = store()
        (1L..STORE_MAX_RECORDS + 5L).forEach { store.save(record(it)) }

        val result = store.prune(T0)

        assertEquals(PruneResult(storeFull = 5), result)
        assertEquals(STORE_MAX_RECORDS, store.count())
        assertEquals("$SESSION_ID-6", store.oldest(1).single().id)
    }

    @Test
    fun prune_keepsAtMost1MB() {
        val store = store()
        (1L..90L).forEach { store.save(large(it)) }
        assertTrue(directory.listFiles()!!.sumOf { it.length() } > STORE_MAX_BYTES)

        val result = store.prune(T0)

        assertTrue(result.storeFull > 0)
        assertTrue(directory.listFiles()!!.sumOf { it.length() } <= STORE_MAX_BYTES)
        assertEquals(90 - result.storeFull, store.count())
        assertEquals("$SESSION_ID-${result.storeFull + 1}", store.oldest(1).single().id)
    }

    @Test
    fun prune_removesRecordsOlderThan156Hours_byRecordTime() {
        val store = store()
        val now = T0 + RECORD_MAX_AGE_MILLIS + 100
        store.save(record(1, createdAt = now - RECORD_MAX_AGE_MILLIS - 1))
        store.save(record(2, createdAt = now - RECORD_MAX_AGE_MILLIS))
        store.save(notice(3, createdAt = now - RECORD_MAX_AGE_MILLIS - 1))

        val result = store.prune(now)

        assertEquals(PruneResult(tooOld = 1), result)
        assertEquals(listOf("$SESSION_ID-2"), store.oldest(10).map { it.id })
    }

    @Test
    fun prune_removesNoticesForSpaceOnlyAfterEveryOtherRecord() {
        val store = store()
        (1L..5L).forEach { store.save(notice(it, createdAt = T0)) }
        (6L..STORE_MAX_RECORDS + 4L).forEach { store.save(record(it)) }

        val result = store.prune(T0)

        assertEquals(PruneResult(storeFull = 4), result)
        assertEquals(STORE_MAX_RECORDS, store.count())
        assertEquals(5, store.oldest(STORE_MAX_RECORDS).count { it.isDroppedNotice })
    }

    @Test
    fun prune_endsWhenOnlyNoticesRemain_removingTheOldestUncounted() {
        val store = store()
        (1L..STORE_MAX_RECORDS + 2L).forEach { store.save(notice(it)) }

        val result = store.prune(T0)

        assertEquals(PruneResult(), result)
        assertEquals(STORE_MAX_RECORDS, store.count())
        assertEquals("$SESSION_ID-3", store.oldest(1).single().id)
    }

    @Test
    fun prune_deletesALeftoverTemporaryFile() {
        val store = store()
        store.save(record(1))
        val leftover = File(directory, "$SESSION_ID-2.json.tmp").apply { writeText("{\"cut\":") }

        store.prune(T0)

        assertFalse(leftover.exists())
        assertEquals(1, store.count())
    }

    @Test
    fun prune_mergesDuplicatesBeforePruning() {
        val store = store()
        val duplicate = TelemetryTestFakes.record(1, event = TelemetryTestFakes.sdkError("test.Same"), createdAt = T0)
        val repeat = TelemetryTestFakes.record(2, event = TelemetryTestFakes.sdkError("test.Same"), createdAt = T0 + 1)
        store.save(duplicate)
        store.save(repeat)
        (3L..STORE_MAX_RECORDS + 1L).forEach { store.save(record(it)) }
        assertEquals(STORE_MAX_RECORDS + 1, store.count())

        val result = store.prune(T0)

        assertEquals(PruneResult(), result)
        assertEquals(STORE_MAX_RECORDS, store.count())
        val kept = store.oldest(1).single()
        assertEquals(duplicate.id, kept.id)
        assertEquals(Num(2), kept.value("paydock.occurrences"))
        assertFalse(File(directory, "${repeat.id}.json").exists())
    }

    @Test
    fun withinBothCaps_countAndPrune_doNotReReadFilesAfterTheIndexIsBuilt() {
        val store = store()
        (1L..3L).forEach { store.save(record(it)) }
        assertEquals(3, store.count())
        val file = File(directory, "$SESSION_ID-2.json")
        file.writeText("not json")

        assertEquals(3, store.count())
        assertEquals(PruneResult(), store.prune(T0))
        assertEquals(3, store.count())
        assertTrue(file.exists())

        // oldest reads the files: the unreadable one is skipped and deleted.
        assertEquals(listOf("$SESSION_ID-1", "$SESSION_ID-3"), store.oldest(10).map { it.id })
        assertFalse(file.exists())
        assertEquals(2, store.count())
    }

    @Test
    fun anUnreadableFile_isSkippedAndDeleted_whenTheIndexIsBuilt() {
        store().save(record(1))
        val garbage = File(directory, "$SESSION_ID-2.json").apply { writeText("{\"timeUnixNano\":") }
        val misnamed = File(directory, "$SESSION_ID-3.json").apply {
            writeText(Json.encodeToString(JsonElement.serializer(), OtlpLogEncoder.recordJson(record(4))))
        }

        val restarted = store()

        assertEquals(1, restarted.count())
        assertEquals(listOf("$SESSION_ID-1"), restarted.oldest(10).map { it.id })
        assertFalse(garbage.exists())
        assertFalse(misnamed.exists())
    }

    @Test
    fun noDirectoryExists_beforeTheFirstSave() {
        val store = store()

        assertEquals(0, store.count())
        assertTrue(store.oldest(10).isEmpty())
        store.remove(listOf("$SESSION_ID-1"))
        assertEquals(PruneResult(), store.prune(T0))
        assertFalse(directory.exists())

        store.save(record(1))

        assertTrue(directory.isDirectory)
        assertEquals(1, store.count())
    }

    @Test
    fun theDirectory_isResolvedOnFirstUse_notAtConstruction() {
        var resolved = 0
        val store = FileDurableStore {
            resolved++
            directory
        }
        assertEquals(0, resolved)

        store.count()
        store.count()

        assertEquals(1, resolved)
    }

    private companion object {
        const val SHORT_WAIT_MILLIS = 100L
        const val SLACK_MILLIS = 2_000L
    }
}

package com.paydock.telemetry

import com.paydock.telemetry.TelemetryTestFakes.FakeClock
import com.paydock.telemetry.TelemetryTestFakes.FakeDeviceContextProvider
import com.paydock.telemetry.TelemetryTestFakes.HeldFileStore
import com.paydock.telemetry.TelemetryTestFakes.InMemoryStore
import com.paydock.telemetry.TelemetryTestFakes.RecordingHandler
import com.paydock.telemetry.TelemetryTestFakes.SESSION_ID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Unit tests for [ChainedCrashHandler], the process's link in the uncaught-exception chain (spec § 7, D9, R_01): it
 * records, then always hands the same thread and throwable on, to the previous handler or else to the fallback.
 */
internal class ChainedCrashHandlerTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val crashingThread = Thread("checkout-worker")
    private val throwable = IllegalStateException()
    private val store = InMemoryStore()

    /** A store whose every call throws. */
    private object ThrowingStore : DurableStore {
        override fun save(record: TelemetryRecord) = error("synthetic store failure")
        override fun oldest(max: Int): List<TelemetryRecord> = error("synthetic store failure")
        override fun remove(ids: Collection<String>) = error("synthetic store failure")
        override fun prune(now: Long): PruneResult = error("synthetic store failure")
        override fun count(): Int = error("synthetic store failure")
    }

    private fun recorder(store: DurableStore = this.store) =
        CrashRecorder(SESSION_ID, RecordSequence(), store, FakeDeviceContextProvider(), FakeClock())

    @Test
    fun uncaughtException_recordsFirst_thenCallsThePreviousHandlerWithTheSameThreadAndThrowable() {
        var storedWhenCalled = -1
        val previous = Thread.UncaughtExceptionHandler { thread, error ->
            storedWhenCalled = store.saved.size
            assertSame(crashingThread, thread)
            assertSame(throwable, error)
        }
        val fallbacks = mutableListOf<Throwable>()

        ChainedCrashHandler(previous, recorder()) { _, error -> fallbacks += error }
            .uncaughtException(crashingThread, throwable)

        assertEquals(1, storedWhenCalled)
        assertTrue(fallbacks.isEmpty())
    }

    @Test
    fun uncaughtException_withAStoreThatThrows_stillCallsThePreviousHandler() {
        val previous = RecordingHandler()

        ChainedCrashHandler(previous, recorder(ThrowingStore)).uncaughtException(crashingThread, throwable)

        assertEquals(listOf<Pair<Thread, Throwable>>(crashingThread to throwable), previous.calls)
    }

    /** R_07: the worker holds the store; the crash record is dropped after the wait and the previous handler runs. */
    @Test
    fun uncaughtException_withTheStoreHeld_dropsTheRecord_andStillCallsThePreviousHandler() {
        val previous = RecordingHandler()
        val fallbacks = mutableListOf<Throwable>()
        val held = HeldFileStore(File(folder.root, FileDurableStore.DIRECTORY_NAME))
        try {
            ChainedCrashHandler(previous, recorder(held.store)) { _, error -> fallbacks += error }
                .uncaughtException(crashingThread, throwable)

            assertEquals(listOf<Pair<Thread, Throwable>>(crashingThread to throwable), previous.calls)
            assertTrue(fallbacks.isEmpty())
        } finally {
            held.release()
        }
        assertEquals(0, held.store.count())
    }

    @Test
    fun uncaughtException_withNoPreviousHandler_runsTheFallback() {
        val fallbacks = mutableListOf<Pair<Thread, Throwable>>()

        ChainedCrashHandler(null, recorder()) { thread, error -> fallbacks += thread to error }
            .uncaughtException(crashingThread, throwable)

        assertEquals(listOf<Pair<Thread, Throwable>>(crashingThread to throwable), fallbacks)
        assertEquals(1, store.saved.size)
    }

    @Test
    fun uncaughtException_withNoPreviousHandlerAndAStoreThatThrows_stillRunsTheFallback() {
        val fallbacks = mutableListOf<Throwable>()

        ChainedCrashHandler(null, recorder(ThrowingStore)) { _, error -> fallbacks += error }
            .uncaughtException(crashingThread, throwable)

        assertEquals(listOf<Throwable>(throwable), fallbacks)
    }

    /** Review fix (R7): a chain that loops back to this handler ends after one record and one forward. */
    @Test
    fun uncaughtException_reEnteredByAChainThatLoopsBack_recordsOnce_andForwardsOnce() {
        lateinit var handler: ChainedCrashHandler
        var forwards = 0
        val loopingPrevious = Thread.UncaughtExceptionHandler { thread, error ->
            forwards++
            handler.uncaughtException(thread, error)
        }
        val fallbacks = mutableListOf<Throwable>()
        handler = ChainedCrashHandler(loopingPrevious, recorder()) { _, error -> fallbacks += error }

        handler.uncaughtException(crashingThread, throwable)

        assertEquals(1, store.saved.size)
        assertEquals(1, forwards)
        assertTrue(fallbacks.isEmpty())
    }

    /** The guard holds only while the handler runs: the next crash on the same thread is recorded again. */
    @Test
    fun uncaughtException_afterALoopEnded_recordsTheNextCrashAgain() {
        lateinit var handler: ChainedCrashHandler
        val loopingPrevious = Thread.UncaughtExceptionHandler { thread, error -> handler.uncaughtException(thread, error) }
        handler = ChainedCrashHandler(loopingPrevious, recorder())

        handler.uncaughtException(crashingThread, throwable)
        handler.uncaughtException(crashingThread, IllegalArgumentException())

        assertEquals(2, store.saved.size)
    }

    /** The guard is per thread: the same handler, entered on another thread while one runs, still records. */
    @Test
    fun uncaughtException_onAnotherThreadWhileOneIsHandled_isStillRecordedAndForwarded() {
        lateinit var handler: ChainedCrashHandler
        var forwards = 0
        val previous = Thread.UncaughtExceptionHandler { _, error ->
            forwards++
            if (forwards == 1) {
                val worker = Thread { handler.uncaughtException(Thread.currentThread(), error) }
                worker.start()
                worker.join()
            }
        }
        handler = ChainedCrashHandler(previous, recorder())

        handler.uncaughtException(crashingThread, throwable)

        assertEquals(2, store.saved.size)
        assertEquals(2, forwards)
    }

    @Test
    fun uncaughtException_recordsWithTheCurrentRecorder() {
        val other = InMemoryStore()
        val handler = ChainedCrashHandler(RecordingHandler(), recorder())

        handler.recorder = recorder(other)
        handler.uncaughtException(crashingThread, throwable)

        assertTrue(store.saved.isEmpty())
        assertEquals(1, other.saved.size)
    }
}

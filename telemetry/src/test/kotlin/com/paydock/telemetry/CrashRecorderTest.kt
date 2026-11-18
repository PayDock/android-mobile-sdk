package com.paydock.telemetry

import com.paydock.telemetry.AttributeValue.Bool
import com.paydock.telemetry.AttributeValue.Str
import com.paydock.telemetry.TelemetryTestFakes.FakeClock
import com.paydock.telemetry.TelemetryTestFakes.FakeDeviceContextProvider
import com.paydock.telemetry.TelemetryTestFakes.HeldFileStore
import com.paydock.telemetry.TelemetryTestFakes.InMemoryStore
import com.paydock.telemetry.TelemetryTestFakes.SESSION_ID
import com.paydock.telemetry.TelemetryTestFakes.T0
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Unit tests for [CrashRecorder], the crash handler's direct store write (D17, R_01, R_02): a fake store, a fake
 * device context and a settable clock, or the real file store where the stored JSON matters. The test throwables get
 * explicit stack traces, because this test's own frames are in `com.paydock.` and would count as SDK code.
 */
internal class CrashRecorderTest {

    @get:Rule
    val folder = TemporaryFolder()

    private class CheckoutFailure(message: String? = null) : IllegalStateException(message)

    private val store = InMemoryStore()
    private val sequence = RecordSequence()
    private val crashingThread = Thread("checkout-worker")

    private fun recorder(
        store: DurableStore = this.store,
        sessionId: String = SESSION_ID,
        deviceContext: DeviceContextProvider = FakeDeviceContextProvider(),
    ) = CrashRecorder(sessionId, sequence, store, deviceContext, FakeClock(T0))

    private fun failure(vararg frames: StackTraceElement, message: String? = null) =
        CheckoutFailure(message).apply { stackTrace = arrayOf(*frames) }

    @Test
    fun record_anSdkCrash_storesAnUncaughtExceptionCrashRecord_inSdk() {
        recorder().record(crashingThread, failure(SDK_FRAME, MERCHANT_FRAME))

        val record = store.saved.single()
        assertEquals("$SESSION_ID-1", record.id)
        assertEquals(T0, record.timeUnixMillis)
        assertEquals(Severity.FATAL, record.severity)
        assertEquals(Str("paydock.crash"), record.value("event.name"))
        assertEquals(Str(SESSION_ID), record.value("session.id"))
        assertEquals(Str("$SESSION_ID-1"), record.value("log.record.uid"))
        assertEquals(Str("wifi"), record.value("network.connection.type"))
        assertEquals(Str(CheckoutFailure::class.java.name), record.value("exception.type"))
        assertEquals(Str("exception"), record.value("paydock.crash.kind"))
        assertEquals(Str("uncaught_handler"), record.value("paydock.crash.source"))
        assertEquals(Str("checkout-worker"), record.value("thread.name"))
        assertEquals(Bool(true), record.value("paydock.exception.in_sdk"))
        assertEquals(null, record.value("paydock.crash.reason"))
    }

    @Test
    fun record_aMerchantCrash_isNotInSdk() {
        recorder().record(crashingThread, failure(MERCHANT_FRAME))

        assertEquals(Bool(false), store.saved.single().value("paydock.exception.in_sdk"))
    }

    /** Principle 11: the message is never read, so a card-like number in it reaches no stored byte. */
    @Test
    fun record_aMessageHoldingACardLikeNumber_appearsNowhereInTheStoredJson() {
        val directory = File(folder.root, FileDurableStore.DIRECTORY_NAME)
        val cardNumber = TelemetryTestFakes.cardLikeNumber()
        val throwable = failure(SDK_FRAME, message = "declined $cardNumber $MESSAGE_SENTINEL")

        recorder(store = FileDurableStore { directory }).record(crashingThread, throwable)

        val stored = directory.listFiles().orEmpty().filter { it.name.endsWith(".json") }
        assertEquals(1, stored.size)
        val json = stored.single().readText()
        assertTrue(json.contains(CheckoutFailure::class.java.name))
        assertFalse(json.contains(cardNumber))
        assertFalse(json.contains(MESSAGE_SENTINEL))
    }

    /** The ID comes from the session's one sequence, so it never collides with an ID the worker took. */
    @Test
    fun record_takesItsIdFromTheSharedSequence() {
        assertEquals(1L, sequence.next()) // the worker's record
        assertEquals(2L, sequence.next())

        recorder().record(crashingThread, failure(MERCHANT_FRAME))

        assertEquals("$SESSION_ID-3", store.saved.single().id)
        assertEquals(4L, sequence.next())
    }

    @Test
    fun record_withAConnectionTypeThatThrows_storesUnknown() {
        val failingContext = object : DeviceContextProvider {
            override fun current(): ResourceAttributes = TelemetryTestFakes.resourceAttributes
            override fun networkConnectionType(): NetworkConnectionType = error("synthetic connectivity failure")
        }

        recorder(deviceContext = failingContext).record(crashingThread, failure(MERCHANT_FRAME))

        assertEquals(Str("unknown"), store.saved.single().value("network.connection.type"))
    }

    /** A record the allowlist strips of its session.id is never stored. */
    @Test
    fun record_rejectedByTheAllowlist_isNotStored() {
        recorder(sessionId = "not-a-uuid").record(crashingThread, failure(MERCHANT_FRAME))

        assertTrue(store.saved.isEmpty())
    }

    @Test
    fun record_neverPrunesOrReadsTheStore() {
        recorder().record(crashingThread, failure(MERCHANT_FRAME))

        assertEquals(1, store.threads.size) // the one save
    }

    /** R_07: the recorder saves through the timed save, waiting at most CRASH_STORE_WAIT_MILLIS. */
    @Test
    fun record_savesThroughTheTimedSave_withTheCrashWait() {
        val waits = mutableListOf<Long>()
        val timed = object : DurableStore by store {
            override fun save(record: TelemetryRecord) = error("the crash path never blocks on save")

            override fun trySave(record: TelemetryRecord, timeoutMillis: Long): Boolean {
                waits += timeoutMillis
                return store.trySave(record, timeoutMillis)
            }
        }

        recorder(store = timed).record(crashingThread, failure(MERCHANT_FRAME))

        assertEquals(listOf(TelemetryLimits.CRASH_STORE_WAIT_MILLIS), waits)
        assertEquals(500L, TelemetryLimits.CRASH_STORE_WAIT_MILLIS)
        assertEquals(1, store.saved.size)
    }

    /** R_07: a store that cannot save in time drops the record silently; the recorder neither throws nor retries. */
    @Test
    fun record_whenTheTimedSaveReturnsFalse_dropsTheRecordSilently() {
        var attempts = 0
        val busy = object : DurableStore by store {
            override fun trySave(record: TelemetryRecord, timeoutMillis: Long): Boolean {
                attempts++
                return false
            }
        }

        recorder(store = busy).record(crashingThread, failure(MERCHANT_FRAME))

        assertEquals(1, attempts)
        assertTrue(store.saved.isEmpty())
    }

    /** R_07: with the worker holding the file store, the crash record is dropped after about 0.5 s. */
    @Test
    fun record_withTheFileStoreHeld_dropsTheRecordAfterTheCrashWait() {
        val directory = File(folder.root, FileDurableStore.DIRECTORY_NAME)
        val held = HeldFileStore(directory)
        try {
            val started = System.nanoTime()

            recorder(store = held.store).record(crashingThread, failure(MERCHANT_FRAME))

            val waitedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started)
            assertTrue("waited $waitedMillis ms", waitedMillis >= TelemetryLimits.CRASH_STORE_WAIT_MILLIS)
            assertTrue("waited $waitedMillis ms", waitedMillis < TelemetryLimits.CRASH_STORE_WAIT_MILLIS + SLACK_MILLIS)
        } finally {
            held.release()
        }
        assertEquals(0, held.store.count())
        assertTrue(directory.listFiles().orEmpty().none { it.name.endsWith(".json") })
    }

    /** Review fix (R6): the build and the save run inside the disk-write permit, never outside it. */
    @Test
    fun record_writesInsideTheDiskWritePermit() {
        val log = mutableListOf<String>()
        val permit = DiskWritePermit { block ->
            log += "allow"
            block()
            log += "saved ${store.saved.size}"
        }

        CrashRecorder(SESSION_ID, sequence, store, FakeDeviceContextProvider(), FakeClock(T0), permit)
            .record(crashingThread, failure(MERCHANT_FRAME))

        assertEquals(listOf("allow", "saved 1"), log)
        assertEquals(1, store.saved.size)
    }

    @Test
    fun record_withAPermitThatRunsNothing_buildsAndSavesNothing() {
        var allowed = 0

        CrashRecorder(SESSION_ID, sequence, store, FakeDeviceContextProvider(), FakeClock(T0)) { allowed++ }
            .record(crashingThread, failure(MERCHANT_FRAME))

        assertEquals(1, allowed)
        assertTrue(store.saved.isEmpty())
        assertEquals(1L, sequence.next()) // no ID was taken outside the permit
    }

    /** The default permit is StrictMode's; on the JVM, where StrictMode cannot be read, the write still happens. */
    @Test
    fun theStrictModePermit_stillRunsTheBlock_withoutTheAndroidRuntime() {
        var ran = false

        DiskWritePermit.STRICT_MODE.allow { ran = true }

        assertTrue(ran)
    }

    private companion object {
        const val MESSAGE_SENTINEL = "secret-message-text"
        const val SLACK_MILLIS = 2_000L
        val SDK_FRAME = StackTraceElement(
            "com.paydock.feature.card.presentation.CardDetailsViewModel",
            "submit",
            "CardDetailsViewModel.kt",
            42
        )
        val MERCHANT_FRAME =
            StackTraceElement("com.example.merchant.CheckoutActivity", "onPay", "CheckoutActivity.kt", 17)
    }
}

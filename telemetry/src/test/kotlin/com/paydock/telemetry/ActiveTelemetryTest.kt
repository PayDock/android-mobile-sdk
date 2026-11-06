package com.paydock.telemetry

import com.paydock.telemetry.TelemetryTestFakes.FakeClock
import com.paydock.telemetry.TelemetryTestFakes.FakeDeviceContextProvider
import com.paydock.telemetry.TelemetryTestFakes.FakeLifecycle
import com.paydock.telemetry.TelemetryTestFakes.FakeMonotonic
import com.paydock.telemetry.TelemetryTestFakes.FakeTransport
import com.paydock.telemetry.TelemetryTestFakes.ImmediateDispatcher
import com.paydock.telemetry.TelemetryTestFakes.InMemoryStore
import com.paydock.telemetry.TelemetryTestFakes.SequentialIds
import com.paydock.telemetry.TelemetryTestFakes.ThrowingDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Unit tests for [ActiveTelemetry], the facade and lifecycle on the worker (R_03, R_04, R_11, R_12, R_14, R_16):
 * recording fakes for the store, the transport and the lifecycle; the immediate dispatcher, or the real
 * [SerialTelemetryDispatcher] where the thread or the order matters. Nothing sleeps: a test waits on a latch.
 */
internal class ActiveTelemetryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val waitSeconds = 5L
    private val credentials = TelemetryTestFakes.credentials
    private val deviceContext = FakeDeviceContextProvider()
    private val store = InMemoryStore()
    private val lifecycle = FakeLifecycle()
    private val error = TelemetryTestFakes.sdkError()

    private fun telemetry(
        dispatcher: TelemetryDispatcher = ImmediateDispatcher(),
        idGenerator: IdGenerator = SequentialIds(),
        clock: Clock = FakeClock(),
        transport: TelemetryTransport = FakeTransport(),
        store: DurableStore = this.store,
    ) = ActiveTelemetry(
        credentials = credentials,
        level = TelemetryLevel.ALL,
        deviceContextProvider = deviceContext,
        ports = ActiveTelemetry.Ports(
            store = store,
            exporter = OtlpExporter(transport, credentials),
            lifecycle = lifecycle,
            retryPolicy = RetryPolicy(Random(5))
        ),
        clock = clock,
        monotonic = FakeMonotonic(),
        idGenerator = idGenerator,
        dispatcher = dispatcher
    )

    /** Waits until every task posted so far, delayed by 0 at most, has run on the worker. */
    private fun SerialTelemetryDispatcher.awaitIdle() {
        val done = CountDownLatch(1)
        assertTrue(postDelayed(0) { done.countDown() })
        assertTrue(done.await(waitSeconds, TimeUnit.SECONDS))
    }

    /** A record from an earlier run, already on disk. */
    private fun storedRecord(sequence: Long) = TelemetryTestFakes.record(
        sequence,
        sessionId = TelemetryTestFakes.uuid(900),
        event = TelemetryTestFakes.sdkError("test.Earlier$sequence"),
        createdAt = TelemetryTestFakes.T0 - 60_000 + sequence
    )

    @Test
    fun isActive_isTrue_andKeepsItsCredentialsAndLevel() {
        val telemetry = telemetry()

        assertTrue(telemetry.isActive)
        assertSame(credentials, telemetry.credentials)
        assertSame(TelemetryLevel.ALL, telemetry.level)
    }

    @Test
    fun initialState_isEnabledAtItsLevelWithItsSessionId() {
        val telemetry = telemetry()

        assertEquals(
            TelemetryState(level = TelemetryLevel.ALL, enabled = true, sessionId = telemetry.sessionId),
            telemetry.currentState()
        )
    }

    /** MD-25 acceptance: a blocked worker never blocks or fails the caller. */
    @Test
    fun report_withTheWorkerBlocked_returnsAtOnce_5000Times() {
        val dispatcher = SerialTelemetryDispatcher()
        val telemetry = telemetry(dispatcher = dispatcher, idGenerator = IdGenerator.RANDOM, clock = Clock.SYSTEM)
        val release = CountDownLatch(1)
        val blocked = CountDownLatch(1)
        val accepted = dispatcher.post {
            blocked.countDown()
            release.await()
        }
        assertTrue(accepted)
        assertTrue(blocked.await(5, TimeUnit.SECONDS))

        try {
            var returned = 0
            val startedAt = System.nanoTime()
            repeat(5_000) {
                telemetry.report(error)
                returned++
            }
            val elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)

            assertEquals(5_000, returned)
            assertTrue("5,000 reports took $elapsedMillis ms", elapsedMillis < 1_000)
        } finally {
            release.countDown()
        }
    }

    @Test
    fun report_start_andSendTestRecord_surviveADispatcherThatThrows() {
        val telemetry = telemetry(dispatcher = ThrowingDispatcher)

        telemetry.report(error)
        telemetry.start()
        telemetry.sendTestRecord()

        assertTrue(telemetry.isActive)
        assertNull(telemetry.resourceAttributes)
        assertEquals(0, lifecycle.registrations)
    }

    @Test
    fun twoInstances_haveDifferentSessionIds_andOneInstanceKeepsItsId() {
        val first = telemetry(idGenerator = IdGenerator.RANDOM)
        val second = telemetry(idGenerator = IdGenerator.RANDOM)

        assertNotEquals(first.sessionId, second.sessionId)

        val sessionId = first.sessionId
        first.report(error)
        first.report(TelemetryTestFakes.crash())
        assertEquals(sessionId, first.sessionId)
        assertEquals(sessionId, first.currentState().sessionId)
    }

    @Test
    fun recordIds_comeFromTheSessionsSequence() {
        val telemetry = telemetry()

        telemetry.report(error)
        telemetry.report(TelemetryTestFakes.crash())

        assertEquals(listOf("${telemetry.sessionId}-1", "${telemetry.sessionId}-2"), store.saved.map { it.id })
        assertEquals(3L, telemetry.sequence.next())
    }

    @Test
    fun start_twice_buildsTheContextOnce() {
        val telemetry = telemetry()
        assertNull(telemetry.resourceAttributes)

        telemetry.start()
        telemetry.start()

        assertEquals(TelemetryTestFakes.resourceAttributes, telemetry.resourceAttributes)
        assertEquals(1, lifecycle.registrations)
    }

    @Test
    fun start_onTheRealWorker_setsTheContextThere() {
        val dispatcher = SerialTelemetryDispatcher()
        val telemetry = telemetry(dispatcher = dispatcher)

        telemetry.start()
        dispatcher.awaitIdle()

        assertEquals(TelemetryTestFakes.resourceAttributes, telemetry.resourceAttributes)
    }

    /** Never on the main thread: every store and transport call runs on the worker, never the caller's thread. */
    @Test
    fun everyStoreAndTransportCall_runsOnThePaydockTelemetryThread() {
        val dispatcher = SerialTelemetryDispatcher()
        val transport = FakeTransport()
        store.save(storedRecord(1))
        store.threads.clear()
        val telemetry = telemetry(dispatcher = dispatcher, transport = transport)

        telemetry.start()
        telemetry.report(error)
        repeat(TelemetryLimits.FLUSH_THRESHOLD) { telemetry.report(TelemetryTestFakes.sdkError("test.Error$it")) }
        telemetry.sendTestRecord()
        dispatcher.awaitIdle()
        dispatcher.awaitIdle()

        assertNotEquals(SerialTelemetryDispatcher.THREAD_NAME, Thread.currentThread().name)
        assertTrue(store.threads.isNotEmpty())
        assertEquals(setOf(SerialTelemetryDispatcher.THREAD_NAME), store.threads.toSet())
        assertTrue(transport.sent.size >= 2)
        assertEquals(setOf(SerialTelemetryDispatcher.THREAD_NAME), transport.sent.map { it.thread }.toSet())
    }

    @Test
    fun theLifecycle_isRegisteredOnceByStartOnly_andTheBackgroundCallbackExports() {
        val dispatcher = ImmediateDispatcher()
        val transport = FakeTransport()
        val telemetry = telemetry(dispatcher = dispatcher, transport = transport)
        telemetry.report(error)
        assertEquals(0, lifecycle.registrations)

        telemetry.start()
        telemetry.start()
        telemetry.report(error)
        assertEquals(1, lifecycle.registrations)
        val sentBefore = transport.sent.size

        lifecycle.onBackground!!.invoke()
        assertEquals(sentBefore, transport.sent.size) // posted, not run on the lifecycle's thread
        dispatcher.runDelayed(0)

        assertEquals(sentBefore + 1, transport.sent.size)
        assertEquals(0, store.count())
        assertEquals(1, lifecycle.registrations)
    }

    @Test
    fun start_flushesTheStoredRecords_earlierRunFirst() {
        val stored = listOf(storedRecord(1), storedRecord(2)).onEach(store::save)
        val transport = FakeTransport()
        val telemetry = telemetry(transport = transport)

        telemetry.start()

        assertEquals(listOf(stored.map { it.id }), transport.sent.map { it.recordIds() })
        assertEquals(0, store.count())
        assertEquals(0, telemetry.currentState().pending)
    }

    @Test
    fun sendTestRecord_storesAndSendsARecordWithTheTestCode() {
        val dispatcher = ImmediateDispatcher()
        val transport = FakeTransport()
        val telemetry = telemetry(dispatcher = dispatcher, transport = transport)
        telemetry.start()

        telemetry.sendTestRecord()
        val saved = store.saved.single()
        assertEquals(AttributeValue.Str(ActiveTelemetry.TEST_RECORD_CODE), saved.value("paydock.error.code"))
        assertEquals(AttributeValue.Str(ActiveTelemetry.TEST_RECORD_TYPE), saved.value("exception.type"))
        assertTrue(transport.sent.isEmpty())
        dispatcher.runDelayed(0)

        val request = transport.sent.single()
        assertEquals(listOf("TelemetryTestRecord"), request.recordValues("paydock.error.code"))
        assertEquals(listOf("com.paydock.telemetry.TelemetryTestRecord"), request.recordValues("exception.type"))
        assertEquals(0, store.count())
    }

    @Test
    fun everySentBatch_carriesTheInstallationId_andEveryRecordItsSessionId() {
        val dispatcher = ImmediateDispatcher()
        val transport = FakeTransport()
        val telemetry = telemetry(dispatcher = dispatcher, transport = transport)
        telemetry.start()

        repeat(TelemetryLimits.FLUSH_THRESHOLD + 3) { telemetry.report(TelemetryTestFakes.sdkError("test.Error$it")) }
        dispatcher.runDelayed()

        assertEquals(2, transport.sent.size)
        assertEquals(TelemetryLimits.FLUSH_THRESHOLD + 3, transport.sent.sumOf { it.logRecords.size })
        transport.sent.forEach { request ->
            assertEquals(TelemetryTestFakes.INSTALLATION_ID, request.resourceValue("app.installation.id"))
            assertTrue(request.recordValues("session.id").all { it == telemetry.sessionId })
        }
    }

    /** With a transport that always answers 401: one send, then ExportStopped, and nothing re-armed after it. */
    @Test
    fun aTransportThatAlwaysAnswers401_sendsOnce_thenStops_andLaterReportsAreStoredButNeverSent() {
        val dispatcher = ImmediateDispatcher()
        val transport = FakeTransport(otherwise = TransportResult.HttpError(401, null))
        val telemetry = telemetry(dispatcher = dispatcher, transport = transport)
        telemetry.start()

        telemetry.report(error)
        assertEquals(1, dispatcher.delayed.size) // the 10 s timer
        dispatcher.runDelayed()

        assertEquals(1, transport.sent.size)
        assertTrue(telemetry.currentState().exportStopped)
        assertTrue(dispatcher.delayed.isEmpty())

        repeat(TelemetryLimits.FLUSH_THRESHOLD + 5) { telemetry.report(TelemetryTestFakes.sdkError("test.Error$it")) }
        telemetry.sendTestRecord()
        lifecycle.onBackground!!.invoke()
        dispatcher.runDelayed()
        dispatcher.runDelayed()

        assertEquals(1, transport.sent.size)
        assertTrue(dispatcher.delayed.isEmpty())
        assertEquals(TelemetryLimits.FLUSH_THRESHOLD + 7, store.count())
    }

    /** Follow-ups run in the same worker task: reports queued behind a slow send are counted, never overwritten. */
    @Test
    fun reportsQueuedBehindASlowSend_areCountedInPending_afterTheFlush() {
        val dispatcher = SerialTelemetryDispatcher()
        val sending = CountDownLatch(1)
        val release = CountDownLatch(1)
        val transport = FakeTransport()
        transport.beforeAnswer = {
            sending.countDown()
            release.await(waitSeconds, TimeUnit.SECONDS)
        }
        store.save(storedRecord(1))
        val telemetry = telemetry(dispatcher = dispatcher, transport = transport)

        telemetry.start()
        assertTrue(sending.await(waitSeconds, TimeUnit.SECONDS))
        repeat(3) { telemetry.report(TelemetryTestFakes.sdkError("test.Error$it")) }
        release.countDown()
        dispatcher.awaitIdle()

        assertEquals(3, telemetry.currentState().pending)
        assertEquals(3, store.count())
        assertTrue(telemetry.currentState().flushScheduled)
    }

    /** The store creates nothing until the first record: an active, started but idle telemetry writes no file. */
    @Test
    fun activeButIdle_createsNoTelemetryDirectory() {
        val dispatcher = SerialTelemetryDispatcher()
        val directory = File(folder.root, FileDurableStore.DIRECTORY_NAME)
        val transport = FakeTransport()
        val telemetry = telemetry(
            dispatcher = dispatcher,
            transport = transport,
            store = FileDurableStore { directory }
        )

        telemetry.start()
        dispatcher.awaitIdle()

        assertEquals(TelemetryTestFakes.resourceAttributes, telemetry.resourceAttributes)
        assertFalse(directory.exists())
        assertTrue(transport.sent.isEmpty())

        telemetry.report(error)
        dispatcher.awaitIdle()
        assertTrue(directory.isDirectory)
    }

    /** Off the main looper, as in a JVM test, the process lifecycle's registration fails quietly and never throws. */
    @Test
    fun processAppLifecycle_registrationWithoutAMainLooper_neverThrows() {
        var backgrounded = 0

        ProcessAppLifecycle.register { backgrounded++ }

        assertEquals(0, backgrounded)
    }
}

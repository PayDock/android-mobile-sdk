package com.paydock.telemetry

import com.paydock.telemetry.TelemetryTestFakes.FakeClock
import com.paydock.telemetry.TelemetryTestFakes.FakeDeviceContextProvider
import com.paydock.telemetry.TelemetryTestFakes.FakeHandlerSlot
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
import org.junit.Assert.assertThrows
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
 * [SerialTelemetryDispatcher] where the thread or the order matters. Nothing sleeps: a test waits on a latch. The
 * crash handler goes into a fake slot and the test crash into a recording main-thread poster, so no test touches the
 * JVM's default handler or the main looper.
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
    private val handlerSlot = FakeHandlerSlot()
    private val mainPosts = mutableListOf<Pair<Long, () -> Unit>>()

    private fun telemetry(
        dispatcher: TelemetryDispatcher = ImmediateDispatcher(),
        idGenerator: IdGenerator = SequentialIds(),
        clock: Clock = FakeClock(),
        transport: TelemetryTransport = FakeTransport(),
        store: DurableStore = this.store,
        clockOffset: ClockOffset = ClockOffset(FakeClock()),
        crashHandlers: CrashHandlerInstaller = CrashHandlerInstaller(handlerSlot),
        postToMain: (Long, () -> Unit) -> Unit = { delay, task -> mainPosts += delay to task },
        exitReports: ExitReportReader = ExitReportReader.NONE,
    ) = ActiveTelemetry(
        credentials = credentials,
        level = TelemetryLevel.ALL,
        deviceContextProvider = deviceContext,
        ports = ActiveTelemetry.Ports(
            store = store,
            exporter = OtlpExporter(transport, credentials),
            lifecycle = lifecycle,
            retryPolicy = RetryPolicy(Random(5)),
            clockOffset = clockOffset,
            exitReports = exitReports
        ),
        clock = clock,
        monotonic = FakeMonotonic(),
        idGenerator = idGenerator,
        dispatcher = dispatcher,
        crashHandlers = crashHandlers,
        postToMain = postToMain
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
    fun start_sendsALateRecordStamped_withThePortsClockOffset() {
        val stored = storedRecord(1).also(store::save)
        val now = stored.timeUnixMillis + TelemetryLimits.ORDERING_WINDOW_MILLIS + 1
        val offset = ClockOffset(FakeClock(TelemetryTestFakes.T0)).apply { measure(TelemetryTestFakes.T0 - 7_000) }
        val transport = FakeTransport()

        telemetry(clock = FakeClock(now), transport = transport, clockOffset = offset).start()

        val sent = transport.sent.single()
        assertEquals(
            ((now - 7_000) * 1_000_000).toString(),
            sent.logRecords.single().getValue("timeUnixNano").toString().trim('"')
        )
        assertEquals(
            listOf(UtcTimestamp.iso8601(stored.timeUnixMillis - 7_000)),
            sent.recordValues(SendStamp.OCCURRED_AT)
        )
        assertEquals(listOf(stored.id), sent.recordIds())
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

    /**
     * MD-47 R_01, end to end: a store filled while offline loses a record on every save, while the threshold export
     * sends the reason's notice; the drop counted in that run starts a new notice, so no notice id is sent twice.
     */
    @Test
    fun aStoreFilledOffline_thenASuccessfulExporter_sendsEachNoticeOnce() {
        val dispatcher = ImmediateDispatcher()
        val store = FileDurableStore { File(folder.root, FileDurableStore.DIRECTORY_NAME) }
        (1L..TelemetryLimits.STORE_MAX_RECORDS).forEach { store.save(storedRecord(it)) }
        val transport = FakeTransport()
        val telemetry = telemetry(dispatcher = dispatcher, transport = transport, store = store)

        var reported = 0
        while (transport.sent.isEmpty() && reported < TelemetryLimits.FLUSH_THRESHOLD) {
            telemetry.report(TelemetryTestFakes.sdkError("test.Error${reported++}"))
        }
        dispatcher.runDelayed()
        dispatcher.runDelayed()

        val noticeIds = transport.sent.flatMap { request ->
            request.recordIds().zip(request.recordValues("event.name"))
                .filter { (_, eventName) -> eventName == "paydock.telemetry.dropped" }
                .map { (id, _) -> id }
        }
        assertTrue("the threshold export ran", transport.sent.isNotEmpty())
        assertEquals("a notice before and one after the export: $noticeIds", 2, noticeIds.size)
        assertEquals("a sent notice was sent again: $noticeIds", noticeIds.toSet().size, noticeIds.size)
        assertEquals(0, store.count())
        assertTrue(telemetry.currentState().notices.isEmpty())
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

    /** R_01, RS-3: the handler is installed by start(), on the calling thread, and never at construction. */
    @Test
    fun theCrashHandler_isInstalledByStartOnly_once() {
        val previous = TelemetryTestFakes.RecordingHandler()
        handlerSlot.current = previous
        val telemetry = telemetry()
        telemetry.report(error)
        assertEquals(0, handlerSlot.sets)
        assertSame(previous, handlerSlot.current)

        telemetry.start()
        telemetry.start()

        assertEquals(1, handlerSlot.sets)
        assertTrue(handlerSlot.current is ChainedCrashHandler)
    }

    /** The install runs before the worker task is posted, so a dispatcher that throws cannot prevent it. */
    @Test
    fun start_installsTheCrashHandler_evenWhenTheDispatcherThrows() {
        telemetry(dispatcher = ThrowingDispatcher).start()

        assertEquals(1, handlerSlot.sets)
    }

    @Test
    fun start_withAnInstallThatThrows_stillRunsTheWorkerTask() {
        val failingSlot = object : DefaultHandlerSlot {
            override fun get(): Thread.UncaughtExceptionHandler? = error("synthetic slot failure")
            override fun set(handler: Thread.UncaughtExceptionHandler) = error("synthetic slot failure")
        }
        val telemetry = telemetry(crashHandlers = CrashHandlerInstaller(failingSlot))

        telemetry.start()

        assertEquals(TelemetryTestFakes.resourceAttributes, telemetry.resourceAttributes)
        assertEquals(1, lifecycle.registrations)
    }

    /** The installed handler records into this session, with an ID from its sequence, and hands the crash on. */
    @Test
    fun theInstalledHandler_recordsACrashIntoThisSession_thenCallsThePreviousHandler() {
        val previous = TelemetryTestFakes.RecordingHandler()
        handlerSlot.current = previous
        val telemetry = telemetry()
        telemetry.start()
        telemetry.report(error)
        val crash = IllegalStateException()
        val thread = Thread("checkout-worker")

        handlerSlot.current!!.uncaughtException(thread, crash)

        val record = store.saved.last()
        assertEquals("${telemetry.sessionId}-2", record.id)
        assertEquals(AttributeValue.Str("uncaught_handler"), record.value("paydock.crash.source"))
        assertEquals(AttributeValue.Str("checkout-worker"), record.value("thread.name"))
        assertEquals(listOf<Pair<Thread, Throwable>>(thread to crash), previous.calls)
    }

    /** R_12: the test crash is posted to the main thread 10 s later and throws a message-less [TelemetryTestCrash]. */
    @Test
    fun throwTestCrash_postsATaskThatThrowsTheTestCrash_tenSecondsLater() {
        val dispatcher = ImmediateDispatcher()
        val telemetry = telemetry(dispatcher = dispatcher)
        telemetry.start()

        telemetry.throwTestCrash()

        val (delay, task) = mainPosts.single()
        assertEquals(ActiveTelemetry.TEST_CRASH_DELAY_MILLIS, delay)
        assertEquals(10_000L, delay)
        val thrown = assertThrows(TelemetryTestCrash::class.java) { task() }
        assertNull(thrown.message)
        assertTrue(dispatcher.delayed.isEmpty())
    }

    @Test
    fun throwTestCrash_whenPostingFails_neverThrows() {
        val telemetry = telemetry(postToMain = { _, _ -> error("synthetic looper failure") })

        telemetry.throwTestCrash()

        assertTrue(telemetry.isActive)
    }

    /** A store that logs each save and count into [log], in the order the worker makes them. */
    private class LoggingStore(private val log: MutableList<String>, private val inner: InMemoryStore) :
        DurableStore by inner {
        override fun save(record: TelemetryRecord) {
            log += "save ${(record.value("paydock.crash.reason") as? AttributeValue.Str)?.value ?: record.id}"
            inner.save(record)
        }

        override fun count(): Int = inner.count().also { log += "count" }
    }

    /** A marker store that logs each write into [log]. */
    private class LoggingMarkers(private val log: MutableList<String>) : KeyValueStore {
        val values = mutableMapOf<String, String>()
        override fun getString(key: String): String? = values[key]
        override fun putString(key: String, value: String) {
            log += "mark $value"
            values[key] = value
        }
    }

    private fun exit(reason: ExitReason, timestamp: Long) = ProcessExit(reason, true, timestamp) { null }

    /**
     * R_04, R_07, note 8: on the worker, the exit reports are read after the stored records are counted and before
     * the start flush; each is stored as a record, and the marker moves only after they are all stored.
     */
    @Test
    fun start_readsTheExitReportsBeforeTheStartFlush_andMovesTheMarkerOnlyAfterTheirDispatch() {
        val log = mutableListOf<String>()
        val dispatcher = SerialTelemetryDispatcher()
        val transport = FakeTransport()
        transport.beforeAnswer = { synchronized(log) { log += "send" } }
        val markers = LoggingMarkers(log)
        val source = ExitInfoSource {
            log += "exits"
            listOf(
                exit(ExitReason.LOW_MEMORY, TelemetryTestFakes.T0 - 2_000),
                exit(ExitReason.ANR, TelemetryTestFakes.T0 - 3_000)
            )
        }
        val telemetry = telemetry(
            dispatcher = dispatcher,
            transport = transport,
            store = LoggingStore(log, store),
            exitReports = ExitReportReader(source, markers)
        )

        telemetry.start()
        dispatcher.awaitIdle()

        assertEquals(
            listOf(
                "count",
                "exits",
                "save ANR",
                "save LOW_MEMORY",
                "mark ${TelemetryTestFakes.T0 - 2_000}",
                "send"
            ),
            log.take(log.indexOf("send") + 1) // the flush counts the store again after its send
        )
        val sent = transport.sent.single()
        assertEquals(listOf("anr", "os_kill"), sent.recordValues("paydock.crash.kind"))
        assertEquals(listOf("exit_info", "exit_info"), sent.recordValues("paydock.crash.source"))
        assertEquals(listOf("${telemetry.sessionId}-1", "${telemetry.sessionId}-2"), sent.recordIds())
        assertEquals((TelemetryTestFakes.T0 - 2_000).toString(), markers.values[ExitReportReader.MARKER_KEY])
        assertEquals(0, store.count())
    }

    @Test
    fun start_withNoExitReports_flushesAsBefore_andWritesNoMarker() {
        val log = mutableListOf<String>()
        val markers = LoggingMarkers(log)
        store.save(storedRecord(1))
        val transport = FakeTransport()

        telemetry(transport = transport, exitReports = ExitReportReader(ExitInfoSource.NONE, markers)).start()

        assertTrue(log.isEmpty())
        assertEquals(1, transport.sent.size)
    }

    /** A marker write that fails costs only a repeat next launch: the records are stored and the start flush runs. */
    @Test
    fun start_withAMarkerWriteThatThrows_stillFlushesTheExitRecords() {
        val failingMarkers = object : KeyValueStore {
            override fun getString(key: String): String? = null
            override fun putString(key: String, value: String) = error("synthetic marker failure")
        }
        val source = ExitInfoSource { listOf(exit(ExitReason.ANR, TelemetryTestFakes.T0 - 1_000)) }
        val transport = FakeTransport()

        telemetry(transport = transport, exitReports = ExitReportReader(source, failingMarkers)).start()

        assertEquals(listOf("anr"), transport.sent.single().recordValues("paydock.crash.kind"))
    }

    private fun TelemetryRecord.crashReason(): String? = (value("paydock.crash.reason") as? AttributeValue.Str)?.value

    /**
     * Review fix (R1): a failed exit-record save leaves the marker, so the next launch reads the exits again (at least
     * once); only then, with every record stored, does the marker move.
     */
    @Test
    fun start_withAnExitRecordSaveThatFails_leavesTheMarker_andTheNextLaunchReadsTheExitsAgain() {
        val markers = TelemetryTestFakes.MapKeyValueStore()
        val exits = listOf(
            exit(ExitReason.ANR, TelemetryTestFakes.T0 - 3_000),
            exit(ExitReason.LOW_MEMORY, TelemetryTestFakes.T0 - 2_000)
        )
        val inner = store
        val diskFullForTheKill = object : DurableStore by inner {
            override fun save(record: TelemetryRecord) {
                if (record.crashReason() == "LOW_MEMORY") error("synthetic disk full")
                inner.save(record)
            }
        }

        telemetry(store = diskFullForTheKill, exitReports = ExitReportReader(ExitInfoSource { exits }, markers)).start()

        assertEquals(listOf("ANR"), store.saved.map { it.crashReason() })
        assertEquals(0, markers.writes)
        assertNull(markers.values[ExitReportReader.MARKER_KEY])

        val nextLaunch = InMemoryStore()
        telemetry(store = nextLaunch, exitReports = ExitReportReader(ExitInfoSource { exits }, markers)).start()

        assertEquals(listOf("ANR", "LOW_MEMORY"), nextLaunch.saved.map { it.crashReason() })
        assertEquals((TelemetryTestFakes.T0 - 2_000).toString(), markers.values[ExitReportReader.MARKER_KEY])
    }

    /** An exit event the reducer drops on purpose (here the hourly cap) is not a failure: the marker still moves. */
    @Test
    fun start_withExitEventsTheReducerDrops_stillMovesTheMarker() {
        val markers = TelemetryTestFakes.MapKeyValueStore()
        val source = ExitInfoSource { listOf(exit(ExitReason.ANR, TelemetryTestFakes.T0 - 1_000)) }
        val telemetry = telemetry(exitReports = ExitReportReader(source, markers))
        repeat(TelemetryLimits.HOURLY_CAP) { telemetry.report(TelemetryTestFakes.sdkError("test.Cap$it")) }

        telemetry.start()

        assertTrue(store.saved.none { it.crashReason() == "ANR" })
        assertTrue("the cap's drop notice", store.saved.any { it.isDroppedNotice })
        assertEquals((TelemetryTestFakes.T0 - 1_000).toString(), markers.values[ExitReportReader.MARKER_KEY])
    }

    /** Off the main looper, as in a JVM test, the process lifecycle's registration fails quietly and never throws. */
    @Test
    fun processAppLifecycle_registrationWithoutAMainLooper_neverThrows() {
        var backgrounded = 0

        ProcessAppLifecycle.register { backgrounded++ }

        assertEquals(0, backgrounded)
    }
}

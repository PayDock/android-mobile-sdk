package com.paydock.telemetry

import com.paydock.telemetry.TelemetryTestFakes.FakeClock
import com.paydock.telemetry.TelemetryTestFakes.FakeDeviceContextProvider
import com.paydock.telemetry.TelemetryTestFakes.ImmediateDispatcher
import com.paydock.telemetry.TelemetryTestFakes.RecordingExporter
import com.paydock.telemetry.TelemetryTestFakes.RecordingQueue
import com.paydock.telemetry.TelemetryTestFakes.RecordingStore
import com.paydock.telemetry.TelemetryTestFakes.SequentialIds
import com.paydock.telemetry.TelemetryTestFakes.ThrowingDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Unit tests for [ActiveTelemetry], the facade and lifecycle on the worker (R_03, R_04, R_12). Every test passes a
 * fake device context: the real one is built in a later slice.
 */
internal class ActiveTelemetryTest {

    private val credentials = TelemetryTestFakes.credentials
    private val deviceContext = FakeDeviceContextProvider()
    private val error = TelemetryTestFakes.sdkError()

    private fun telemetry(
        dispatcher: TelemetryDispatcher = ImmediateDispatcher(),
        idGenerator: IdGenerator = SequentialIds(),
        clock: Clock = FakeClock(),
        ports: ActiveTelemetry.Ports = ActiveTelemetry.Ports(),
    ) = ActiveTelemetry(
        credentials = credentials,
        level = TelemetryLevel.ALL,
        deviceContextProvider = deviceContext,
        clock = clock,
        idGenerator = idGenerator,
        dispatcher = dispatcher,
        ports = ports
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
    fun report_andStart_surviveADispatcherThatThrows() {
        val telemetry = telemetry(dispatcher = ThrowingDispatcher)

        telemetry.report(error)
        telemetry.start()

        assertTrue(telemetry.isActive)
        assertNull(telemetry.resourceAttributes)
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
    fun start_twice_buildsTheContextOnce() {
        val telemetry = telemetry()
        assertNull(telemetry.resourceAttributes)

        telemetry.start()
        telemetry.start()

        assertEquals(1, deviceContext.currentCalls)
        assertEquals(TelemetryTestFakes.resourceAttributes, telemetry.resourceAttributes)
    }

    @Test
    fun start_onTheRealWorker_setsTheContextThere() {
        val dispatcher = SerialTelemetryDispatcher()
        val telemetry = telemetry(dispatcher = dispatcher)

        telemetry.start()
        val done = CountDownLatch(1)
        assertTrue(dispatcher.post { done.countDown() })

        assertTrue(done.await(5, TimeUnit.SECONDS))
        assertEquals(TelemetryTestFakes.resourceAttributes, telemetry.resourceAttributes)
    }

    @Test
    fun dispatch_endToEnd_reachesThePorts_withTheInstallationIdAndTheSessionId() {
        val dispatcher = ImmediateDispatcher()
        val clock = FakeClock()
        val start = clock.nowMillis
        val queue = RecordingQueue()
        val store = RecordingStore()
        val exporter = RecordingExporter()
        val telemetry = telemetry(
            dispatcher = dispatcher,
            clock = clock,
            ports = ActiveTelemetry.Ports(queue = queue, store = store, exporter = exporter)
        )
        telemetry.start()

        repeat(TelemetryReducer.FLUSH_THRESHOLD) { telemetry.report(TelemetryTestFakes.crash()) }
        telemetry.report(error)

        clock.nowMillis = start + TelemetryReducer.FLUSH_INTERVAL_MILLIS
        dispatcher.runDelayed()
        clock.nowMillis = start + TelemetryReducer.COLLAPSE_WINDOW_MILLIS
        dispatcher.runDelayed()

        assertEquals(listOf(TelemetryReducer.FLUSH_THRESHOLD, 1), exporter.batches.map { it.records.size })
        assertEquals(TelemetryReducer.FLUSH_THRESHOLD + 1, store.saved.size)
        assertEquals(TelemetryReducer.FLUSH_THRESHOLD + 1, queue.offered.size)
        exporter.batches.forEach { batch ->
            assertEquals(TelemetryTestFakes.INSTALLATION_ID, batch.resource.appInstallationId)
            assertTrue(batch.records.all { it.sessionId == telemetry.sessionId })
        }
        assertEquals(0, telemetry.currentState().queued)
        assertTrue(telemetry.currentState().collapsing.isEmpty())
        assertEquals(start + TelemetryReducer.COLLAPSE_WINDOW_MILLIS, telemetry.currentState().lastExport)
    }
}

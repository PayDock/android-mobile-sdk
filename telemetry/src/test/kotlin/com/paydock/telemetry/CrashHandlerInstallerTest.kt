package com.paydock.telemetry

import com.paydock.telemetry.TelemetryTestFakes.FakeClock
import com.paydock.telemetry.TelemetryTestFakes.FakeDeviceContextProvider
import com.paydock.telemetry.TelemetryTestFakes.FakeHandlerSlot
import com.paydock.telemetry.TelemetryTestFakes.InMemoryStore
import com.paydock.telemetry.TelemetryTestFakes.RecordingHandler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [CrashHandlerInstaller] (spec § 7, D9, R_01): one link per process, the slot's earlier handler kept
 * behind it, and a later install only swapping the recorder. A fake slot stands in for the JVM's default handler,
 * except in the one test of [DefaultHandlerSlot.SYSTEM], which restores it.
 */
internal class CrashHandlerInstallerTest {

    private val crashingThread = Thread("checkout-worker")
    private val throwable = IllegalStateException()

    private fun recorder(store: DurableStore, sessionId: String) =
        CrashRecorder(sessionId, RecordSequence(), store, FakeDeviceContextProvider(), FakeClock())

    @Test
    fun install_chainsTheSlotsCurrentHandler() {
        val previous = RecordingHandler()
        val slot = FakeHandlerSlot(previous)
        val store = InMemoryStore()

        CrashHandlerInstaller(slot).install(recorder(store, TelemetryTestFakes.uuid(1)))
        slot.current!!.uncaughtException(crashingThread, throwable)

        assertEquals(1, slot.sets)
        assertTrue(slot.current is ChainedCrashHandler)
        assertEquals(1, store.saved.size)
        assertEquals(listOf<Pair<Thread, Throwable>>(crashingThread to throwable), previous.calls)
    }

    @Test
    fun installTwice_makesOneLink_andTheSecondRecorderWins() {
        val previous = RecordingHandler()
        val slot = FakeHandlerSlot(previous)
        val first = InMemoryStore()
        val second = InMemoryStore()
        val installer = CrashHandlerInstaller(slot)

        installer.install(recorder(first, TelemetryTestFakes.uuid(1)))
        val link = slot.current
        installer.install(recorder(second, TelemetryTestFakes.uuid(2)))
        slot.current!!.uncaughtException(crashingThread, throwable)

        assertEquals(1, slot.sets)
        assertSame(link, slot.current)
        assertTrue(first.saved.isEmpty())
        assertEquals(listOf("${TelemetryTestFakes.uuid(2)}-1"), second.saved.map { it.id })
        assertEquals(1, previous.calls.size) // one link: the crash reaches the previous handler once
    }

    @Test
    fun install_withAnEmptySlot_setsALinkWithNoPreviousHandler() {
        val slot = FakeHandlerSlot(null)

        CrashHandlerInstaller(slot).install(recorder(InMemoryStore(), TelemetryTestFakes.uuid(1)))

        assertEquals(1, slot.sets)
        assertTrue(slot.current is ChainedCrashHandler)
    }

    @Test
    fun install_whenTheSlotFailsToSet_triesAgainOnTheNextInstall() {
        var failNext = true
        val slot = object : DefaultHandlerSlot {
            var current: Thread.UncaughtExceptionHandler? = null
            override fun get(): Thread.UncaughtExceptionHandler? = current
            override fun set(handler: Thread.UncaughtExceptionHandler) {
                if (failNext) {
                    failNext = false
                    error("synthetic slot failure")
                }
                current = handler
            }
        }
        val installer = CrashHandlerInstaller(slot)

        runCatching { installer.install(recorder(InMemoryStore(), TelemetryTestFakes.uuid(1))) }
        installer.install(recorder(InMemoryStore(), TelemetryTestFakes.uuid(2)))

        assertTrue(slot.current is ChainedCrashHandler)
    }

    @Test
    fun systemSlot_readsAndWritesTheJvmDefaultHandler() {
        val original = Thread.getDefaultUncaughtExceptionHandler()
        val handler = RecordingHandler()
        try {
            DefaultHandlerSlot.SYSTEM.set(handler)

            assertSame(handler, Thread.getDefaultUncaughtExceptionHandler())
            assertSame(handler, DefaultHandlerSlot.SYSTEM.get())
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(original)
        }
    }
}

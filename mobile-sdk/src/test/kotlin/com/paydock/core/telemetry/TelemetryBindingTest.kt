package com.paydock.core.telemetry

import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.paydock.core.domain.model.Environment
import com.paydock.core.injection.sdkModule
import com.paydock.telemetry.ActiveTelemetry
import com.paydock.telemetry.ChainedCrashHandler
import com.paydock.telemetry.CrashHandlerInstaller
import com.paydock.telemetry.DefaultHandlerSlot
import com.paydock.telemetry.DeviceContextProvider
import com.paydock.telemetry.DurableStore
import com.paydock.telemetry.ExportBatch
import com.paydock.telemetry.ExportResult
import com.paydock.telemetry.Exporter
import com.paydock.telemetry.FileDurableStore
import com.paydock.telemetry.NetworkConnectionType
import com.paydock.telemetry.NoOpTelemetry
import com.paydock.telemetry.PruneResult
import com.paydock.telemetry.ResourceAttributes
import com.paydock.telemetry.SerialTelemetryDispatcher
import com.paydock.telemetry.Telemetry
import com.paydock.telemetry.TelemetryDispatcher
import com.paydock.telemetry.TelemetryRecord
import com.paydock.telemetry.TelemetryRuntime
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import java.io.File

/**
 * Tests for the telemetry activation binding (RS-4, R_01–R_04, D22).
 *
 * Every input is injected: the endpoint, a counting factory that builds [ActiveTelemetry] with an immediate
 * dispatcher, a fake device context and in-memory ports, and a counting fake runtime. The collector table is the only
 * input of [bindTelemetry]; a failed or blank lookup is simulated by mocking [TelemetryCollector]. Each test binds into
 * its own fresh Koin instance. An [ActiveTelemetry] built here installs its crash handler into a fake slot, never into
 * the JVM's default handler.
 */
internal class TelemetryBindingTest {

    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var koin: Koin

    private val endpoint = "https://otlp.invalid/v1/logs"

    /** A runtime that counts its starts and can be told to fail. */
    private class CountingRuntime(private val failure: Throwable? = null) : TelemetryRuntime {
        var starts = 0
            private set

        override fun start() {
            starts++
            failure?.let { throw it }
        }
    }

    /** The default-handler slot of these tests: a plain field, never the JVM's own handler. */
    private class FakeSlot : DefaultHandlerSlot {
        var current: Thread.UncaughtExceptionHandler? = null
        var sets = 0

        override fun get(): Thread.UncaughtExceptionHandler? = current

        override fun set(handler: Thread.UncaughtExceptionHandler) {
            sets++
            current = handler
        }
    }

    private val handlerSlot = FakeSlot()

    /** Keeps records in memory: these tests never touch the disk through an active telemetry. */
    private class MemoryStore : DurableStore {
        private val records = linkedMapOf<String, TelemetryRecord>()

        override fun save(record: TelemetryRecord) {
            records[record.id] = record
        }

        override fun oldest(max: Int): List<TelemetryRecord> = records.values.sortedBy { it.timeUnixMillis }.take(max)

        override fun remove(ids: Collection<String>) {
            ids.forEach { records.remove(it) }
        }

        override fun prune(now: Long): PruneResult = PruneResult()

        override fun count(): Int = records.size
    }

    /** Accepts every batch; nothing leaves the test. */
    private val acceptingExporter = object : Exporter {
        override fun export(batch: ExportBatch): ExportResult = ExportResult.Success()
    }

    /** A fixed device context: these tests never build the real one. */
    private val deviceContext = object : DeviceContextProvider {
        override fun current() = ResourceAttributes(
            serviceVersion = "0.0.0-test",
            deploymentEnvironmentName = "sandbox",
            osVersion = "",
            deviceManufacturer = "",
            deviceModelIdentifier = "",
            appInstallationId = "00000000-0000-4000-8000-000000000001",
            appId = "",
            appVersion = "",
            appDebuggable = true
        )

        override fun networkConnectionType() = NetworkConnectionType.UNKNOWN
    }

    /** Runs every task at once on the caller's thread, so no worker thread is ever started. */
    private val immediate = object : TelemetryDispatcher {
        override fun post(task: () -> Unit): Boolean {
            task()
            return true
        }

        override fun postDelayed(delayMillis: Long, task: () -> Unit): Boolean = true
    }

    private fun activeTelemetry(url: String = endpoint) = ActiveTelemetry(
        endpoint = url,
        deviceContextProvider = deviceContext,
        ports = ActiveTelemetry.Ports(store = MemoryStore(), exporter = acceptingExporter, lifecycle = {}),
        dispatcher = immediate,
        crashHandlers = CrashHandlerInstaller(handlerSlot)
    )

    /** The `createActive` factory, counting its calls and the URLs it was given. */
    private var factoryCalls = 0
    private val factoryUrls = mutableListOf<String>()
    private val countingFactory: (String) -> ActiveTelemetry = { url ->
        factoryCalls++
        factoryUrls += url
        activeTelemetry(url)
    }

    @Before
    fun setUp() {
        resetBoundTelemetry()
        koin = koinApplication { }.koin
    }

    @After
    fun tearDown() {
        resetBoundTelemetry()
    }

    @Test
    fun theSdkModules_withNoBind_resolveTheNoOpFacade() {
        val sdkKoin = koinApplication { modules(sdkModule) }.koin

        assertSame(NoOpTelemetry, sdkKoin.get<Telemetry>())
    }

    @Test
    fun bind_overridesTheDefaultFacade() {
        koin.loadModules(listOf(defaultTelemetryModule))
        val runtime = CountingRuntime()
        val telemetry = activeTelemetry()

        bind(koin, TelemetrySelection(telemetry = telemetry, runtime = runtime))

        assertSame(telemetry, koin.get<Telemetry>())
    }

    private fun select(url: String? = endpoint): TelemetrySelection = selectTelemetry(url, countingFactory)

    /** A no-op selection, and the factory never called: nothing is built and nothing is written to storage. */
    private fun assertNoOp(selection: TelemetrySelection) {
        assertSame(NoOpTelemetry, selection.telemetry)
        assertSame(NoOpTelemetry, selection.runtime)
        assertEquals(0, factoryCalls)
    }

    private fun liveTelemetryThreads(): Set<Thread> = Thread.getAllStackTraces().keys
        .filter { it.isAlive && it.name == SerialTelemetryDispatcher.THREAD_NAME }
        .toSet()

    /** Runs [block] with [TelemetryCollector.logsUrl] answering [answer] for every environment. */
    private fun withCollectorLookup(answer: (Environment) -> String, block: () -> Unit) {
        mockkObject(TelemetryCollector)
        try {
            every { TelemetryCollector.logsUrl(any()) } answers { answer(firstArg()) }
            block()
        } finally {
            unmockkObject(TelemetryCollector)
        }
    }

    @Test
    fun select_blankEndpoint_isNoOp() {
        listOf("", " ", "\t", "\n").forEach { blank -> assertNoOp(select(blank)) }
    }

    /** A failed URL lookup reaches the selection as null. */
    @Test
    fun select_nullEndpoint_isNoOp() {
        assertNoOp(select(null))
    }

    @Test
    fun select_inactive_neverCallsTheFactory() {
        select(null)
        select("")
        select("  ")

        assertEquals(0, factoryCalls)
    }

    @Test
    fun select_anEndpoint_isActiveTelemetryAsBothFacadeAndRuntime_withThatEndpoint() {
        val selection = select()

        val active = selection.telemetry as ActiveTelemetry
        assertSame(active, selection.runtime)
        assertTrue(active.isActive)
        assertEquals(endpoint, active.endpoint)
        assertEquals(listOf(endpoint), factoryUrls)
    }

    @Test
    fun collector_everyEnvironmentHasACollector_devForSandboxAndStaging_prodForProduction() {
        assertEquals(TelemetryCollector.DEV_LOGS_URL, TelemetryCollector.logsUrl(Environment.SANDBOX))
        assertEquals(TelemetryCollector.DEV_LOGS_URL, TelemetryCollector.logsUrl(Environment.STAGING))
        assertEquals(TelemetryCollector.PROD_LOGS_URL, TelemetryCollector.logsUrl(Environment.PRODUCTION))
        assertEquals(
            "https://mobile-telemetry-dev.shared.apac.paydock.com/v1/logs",
            TelemetryCollector.DEV_LOGS_URL
        )
        assertEquals("https://mobile-telemetry.shared.apac.paydock.com/v1/logs", TelemetryCollector.PROD_LOGS_URL)
        Environment.entries.forEach { environment ->
            assertTrue("environment=$environment", TelemetryCollector.logsUrl(environment).isNotBlank())
        }
    }

    @Test
    fun select_everyEnvironment_isActiveWithItsCollectorUrl() {
        Environment.entries.forEach { environment ->
            val selection = select(TelemetryCollector.logsUrl(environment))

            assertTrue("environment=$environment", selection.telemetry is ActiveTelemetry)
        }
        assertEquals(Environment.entries.map { TelemetryCollector.logsUrl(it) }, factoryUrls)
    }

    @Test
    fun production_bindsTheActiveRuntime_withTheProdCollectorUrl() {
        val selection = select(TelemetryCollector.logsUrl(Environment.PRODUCTION))

        val bound = bind(koin, selection)

        val active = bound as ActiveTelemetry
        assertSame(active, koin.get<Telemetry>())
        assertEquals("https://mobile-telemetry.shared.apac.paydock.com/v1/logs", active.endpoint)
        assertEquals(deviceContext.current(), active.resourceAttributes)
        assertEquals(1, handlerSlot.sets)
    }

    @Test
    fun bindTelemetry_aFailedUrlLookup_bindsTheNoOp_andStartsNoTelemetryThread() {
        val before = liveTelemetryThreads()

        withCollectorLookup({ error("synthetic lookup failure") }) {
            Environment.entries.forEach { environment ->
                // The context is never touched: an inactive SDK builds nothing.
                val bound = bindTelemetry(koin, environment, ContextWrapper(null))

                assertSame("environment=$environment", NoOpTelemetry, bound)
                assertSame(NoOpTelemetry, koin.get<Telemetry>())
            }
        }

        // Compared with the threads alive before, so a worker another test class left in this JVM cannot interfere.
        assertEquals(emptySet<Thread>(), liveTelemetryThreads() - before)
    }

    @Test
    fun bindTelemetry_aBlankUrl_bindsTheNoOp() {
        withCollectorLookup({ " " }) {
            Environment.entries.forEach { environment ->
                val bound = bindTelemetry(koin, environment, ContextWrapper(null))

                assertSame("environment=$environment", NoOpTelemetry, bound)
                assertSame(NoOpTelemetry, koin.get<Telemetry>())
            }
        }
    }

    @Test
    fun bind_active_registersTheFacade_andStartsItOnceWithTheDeviceContext() {
        val selection = select()

        val bound = bind(koin, selection)

        assertSame(selection.telemetry, bound)
        assertSame(selection.telemetry, koin.get<Telemetry>())
        assertEquals(deviceContext.current(), (bound as ActiveTelemetry).resourceAttributes)
    }

    @Test
    fun bind_noOp_registersTheNoOpFacade() {
        val bound = bind(koin, select(null))

        assertSame(NoOpTelemetry, bound)
        assertSame(NoOpTelemetry, koin.get<Telemetry>())
    }

    @Test
    fun bind_registersNoRuntimeAndNoSelection() {
        bind(koin, select())

        assertNull(koin.getOrNull<TelemetryRuntime>())
        assertNull(koin.getOrNull<ActiveTelemetry>())
        assertNull(koin.getOrNull<TelemetrySelection>())
    }

    @Test
    fun bind_active_startsTheRuntimeOnce() {
        val runtime = CountingRuntime()
        val facade = activeTelemetry()

        val bound = bind(koin, TelemetrySelection(telemetry = facade, runtime = runtime))

        assertEquals(1, runtime.starts)
        assertSame(facade, bound)
        assertSame(facade, koin.get<Telemetry>())
    }

    @Test
    fun bind_noOp_neverStartsTheRuntime() {
        val runtime = CountingRuntime()

        bind(koin, TelemetrySelection(telemetry = NoOpTelemetry, runtime = runtime))

        assertEquals(0, runtime.starts)
    }

    @Test
    fun bind_startThrows_bindsTheNoOp() {
        val runtime = CountingRuntime(failure = IllegalStateException("synthetic start failure"))
        val facade = activeTelemetry()

        val bound = bind(koin, TelemetrySelection(telemetry = facade, runtime = runtime))

        assertEquals(1, runtime.starts)
        assertSame(NoOpTelemetry, bound)
        assertSame(NoOpTelemetry, koin.get<Telemetry>())
    }

    /**
     * MD-47 R_04: while an active facade is bound, a second active selection returns the first, registers it in the
     * new Koin and never starts the second runtime.
     */
    @Test
    fun bind_activeWhileActiveIsBound_returnsTheFirst_registersItInTheNewKoin_andNeverStartsTheSecond() {
        val firstRuntime = CountingRuntime()
        val first = activeTelemetry()
        bind(koin, TelemetrySelection(telemetry = first, runtime = firstRuntime))
        val secondRuntime = CountingRuntime()
        val newKoin = koinApplication { modules(defaultTelemetryModule) }.koin

        val bound = bind(newKoin, TelemetrySelection(telemetry = activeTelemetry(), runtime = secondRuntime))

        assertSame(first, bound)
        assertSame(first, newKoin.get<Telemetry>())
        assertEquals(0, secondRuntime.starts)
        assertEquals(1, firstRuntime.starts)
    }

    /** MD-47 R_04: an inactive selection replaces the bound active facade with the no-op, and then binds afresh. */
    @Test
    fun bind_inactiveAfterActive_bindsTheNoOp_andANewActiveSelectionThenStarts() {
        bind(koin, TelemetrySelection(telemetry = activeTelemetry(), runtime = CountingRuntime()))

        val noOp = bind(koin, TelemetrySelection(telemetry = NoOpTelemetry, runtime = NoOpTelemetry))

        assertSame(NoOpTelemetry, noOp)
        assertSame(NoOpTelemetry, koin.get<Telemetry>())

        val runtime = CountingRuntime()
        val next = activeTelemetry()
        assertSame(next, bind(koin, TelemetrySelection(telemetry = next, runtime = runtime)))
        assertEquals(1, runtime.starts)
    }

    /** MD-47 R_04: a failed start leaves no dead active facade bound: the next active selection starts. */
    @Test
    fun bind_afterAFailedStart_aNewActiveSelectionStarts() {
        val failing = CountingRuntime(failure = IllegalStateException("synthetic start failure"))
        assertSame(NoOpTelemetry, bind(koin, TelemetrySelection(telemetry = activeTelemetry(), runtime = failing)))

        val runtime = CountingRuntime()
        val next = activeTelemetry()
        val bound = bind(koin, TelemetrySelection(telemetry = next, runtime = runtime))

        assertSame(next, bound)
        assertSame(next, koin.get<Telemetry>())
        assertEquals(1, runtime.starts)
    }

    /** R_01: an active binding installs the crash handler at start. */
    @Test
    fun bind_active_installsTheCrashHandler() {
        val selection = select()
        assertEquals(0, handlerSlot.sets)

        bind(koin, selection)

        assertEquals(1, handlerSlot.sets)
        assertTrue(handlerSlot.current is ChainedCrashHandler)
    }

    /** RS-2: inactive, nothing is built: no factory call, no worker thread, no lifecycle observer, no file. */
    @Test
    fun bind_inactive_createsNoThreadNoObserverAndNoFile() {
        val noBackup = folder.newFolder("no_backup")
        val context = mockk<Context>(relaxed = true)
        every { context.applicationContext } returns context
        every { context.noBackupFilesDir } returns noBackup
        val lifecycle = mockk<Lifecycle>(relaxed = true)
        val owner = mockk<ProcessLifecycleOwner>(relaxed = true)
        mockkObject(ProcessLifecycleOwner)
        try {
            every { ProcessLifecycleOwner.get() } returns owner
            every { owner.lifecycle } returns lifecycle
            val before = liveTelemetryThreads()
            val handlerBefore = Thread.getDefaultUncaughtExceptionHandler()
            var factoryCalls = 0
            val factory: (String) -> ActiveTelemetry = { url ->
                factoryCalls++
                ActiveTelemetry.create(context, url, "0.0.0-test", "sandbox")
            }

            listOf(null, "", " ").forEach { url ->
                assertSame(NoOpTelemetry, bind(koin, selectTelemetry(url, factory)))
            }
            withCollectorLookup({ error("synthetic lookup failure") }) {
                assertSame(NoOpTelemetry, bindTelemetry(koin, Environment.PRODUCTION, context))
            }
            withCollectorLookup({ "" }) {
                assertSame(NoOpTelemetry, bindTelemetry(koin, Environment.SANDBOX, context))
            }

            assertEquals(0, factoryCalls)
            assertEquals(emptySet<Thread>(), liveTelemetryThreads() - before)
            verify(exactly = 0) { lifecycle.addObserver(any()) }
            assertFalse(File(noBackup, FileDurableStore.DIRECTORY_NAME).exists())
            assertEquals(emptyList<String>(), noBackup.list()!!.toList())
            assertSame(handlerBefore, Thread.getDefaultUncaughtExceptionHandler())
        } finally {
            unmockkObject(ProcessLifecycleOwner)
        }
    }
}

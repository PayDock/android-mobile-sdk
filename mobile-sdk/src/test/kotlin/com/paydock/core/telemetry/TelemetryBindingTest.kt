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
import com.paydock.telemetry.TelemetryCredentials
import com.paydock.telemetry.TelemetryCredentialsProvider
import com.paydock.telemetry.TelemetryDispatcher
import com.paydock.telemetry.TelemetryLevel
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
 * Debug tests for the telemetry activation binding (RS-4, R_01–R_04, D22, D23).
 *
 * Every input is injected: fake credentials providers, a counting factory that builds [ActiveTelemetry] with an
 * immediate dispatcher, a fake device context and in-memory ports, and a counting fake runtime — never the generated
 * build values, so the result does not depend on the engineer's `local.properties`. Each test binds into its own
 * fresh Koin instance. The credentials provider and level are tested with injected [LocalTelemetryValues]. An
 * [ActiveTelemetry] built here installs its crash handler into a fake slot and posts its test crash to a list, never
 * to the JVM's default handler or the main looper.
 */
internal class TelemetryBindingTest {

    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var koin: Koin

    private val credentials = TelemetryCredentials(
        endpoint = "https://otlp.invalid/v1/logs",
        authorization = "synthetic-not-a-credential"
    )

    private val configured = TelemetryCredentialsProvider { credentials }
    private val unconfigured = TelemetryCredentialsProvider { null }
    private val failing = TelemetryCredentialsProvider { error("synthetic provider failure") }

    /** A runtime that counts its starts and test records, in order, and can be told to fail. */
    private class CountingRuntime(private val failure: Throwable? = null) : TelemetryRuntime {
        val calls = mutableListOf<String>()

        val starts: Int
            get() = calls.count { it == "start" }

        val testRecords: Int
            get() = calls.count { it == "sendTestRecord" }

        val testCrashes: Int
            get() = calls.count { it == "throwTestCrash" }

        override fun start() {
            calls += "start"
            failure?.let { throw it }
        }

        override fun sendTestRecord() {
            calls += "sendTestRecord"
        }

        override fun throwTestCrash() {
            calls += "throwTestCrash"
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

    /** Every task an [ActiveTelemetry] of these tests posted to the main thread, with its delay. */
    private val mainPosts = mutableListOf<Long>()

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

    private fun activeTelemetry(level: TelemetryLevel = TelemetryLevel.ALL) = ActiveTelemetry(
        credentials = credentials,
        level = level,
        deviceContextProvider = deviceContext,
        ports = ActiveTelemetry.Ports(store = MemoryStore(), exporter = acceptingExporter, lifecycle = {}),
        dispatcher = immediate,
        crashHandlers = CrashHandlerInstaller(handlerSlot),
        postToMain = { delay, _ -> mainPosts += delay }
    )

    /** The `createActive` factory, counting its calls. */
    private var factoryCalls = 0
    private val countingFactory: (TelemetryCredentials, TelemetryLevel) -> ActiveTelemetry = { c, l ->
        factoryCalls++
        assertSame(credentials, c)
        activeTelemetry(l)
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

    private fun select(
        provider: TelemetryCredentialsProvider = configured,
        level: TelemetryLevel = TelemetryLevel.ALL,
    ): TelemetrySelection = selectTelemetry(provider, level, countingFactory)

    /** Release build values: no override, empty, so only the collector table decides. */
    private val releaseValues = LocalTelemetryValues(overrideAllowed = false, endpoint = "", auth = "", level = "")

    /** Debug build values with a local endpoint and auth (synthetic, never a real key). */
    private fun debugValues(
        endpoint: String = "https://local.invalid/v1/logs",
        auth: String = "synthetic-not-a-credential",
        level: String = "",
    ) = LocalTelemetryValues(overrideAllowed = true, endpoint = endpoint, auth = auth, level = level)

    /** A no-op selection, and the factory never called: nothing is built and nothing is written to storage. */
    private fun assertNoOp(selection: TelemetrySelection) {
        assertSame(NoOpTelemetry, selection.telemetry)
        assertSame(NoOpTelemetry, selection.runtime)
        assertEquals(0, factoryCalls)
    }

    private fun liveTelemetryThreads(): Set<Thread> = Thread.getAllStackTraces().keys
        .filter { it.isAlive && it.name == SerialTelemetryDispatcher.THREAD_NAME }
        .toSet()

    @Test
    fun select_blankEndpoint_isNoOp() {
        assertNoOp(select(provider = { TelemetryCredentials(endpoint = " ", authorization = "") }))
    }

    @Test
    fun select_providerReturnsNull_isNoOp() {
        assertNoOp(select(provider = unconfigured))
    }

    @Test
    fun select_providerThrows_isNoOp() {
        assertNoOp(select(provider = failing))
    }

    @Test
    fun select_production_releaseValues_isNoOp() {
        assertNoOp(select(provider = SdkTelemetryCredentialsProvider(Environment.PRODUCTION, releaseValues)))
    }

    @Test
    fun select_sandboxAndStaging_releaseValues_isActiveWithTheCollectorAndNoCredential() {
        var calls = 0
        listOf(Environment.SANDBOX, Environment.STAGING).forEach { environment ->
            val selection = selectTelemetry(
                SdkTelemetryCredentialsProvider(environment, releaseValues),
                telemetryLevel(releaseValues)
            ) { c, l ->
                calls++
                assertEquals(TelemetryCollector.DEV_LOGS_URL, c.endpoint)
                assertEquals("", c.authorization)
                assertSame(TelemetryLevel.ALL, l)
                activeTelemetry(l)
            }

            assertTrue(selection.telemetry is ActiveTelemetry)
        }
        assertEquals(2, calls)
    }

    @Test
    fun select_emptyAuthorization_isActive() {
        var calls = 0
        val noCredential = TelemetryCredentialsProvider { TelemetryCredentials("https://otlp.invalid", "") }
        val selection = selectTelemetry(noCredential, TelemetryLevel.ALL) { _, l ->
            calls++
            activeTelemetry(l)
        }

        assertTrue(selection.telemetry is ActiveTelemetry)
        assertEquals(1, calls)
    }

    @Test
    fun collector_sandboxAndStagingHaveTheDevUrl_productionHasNone() {
        assertEquals(TelemetryCollector.DEV_LOGS_URL, TelemetryCollector.logsUrl(Environment.SANDBOX))
        assertEquals(TelemetryCollector.DEV_LOGS_URL, TelemetryCollector.logsUrl(Environment.STAGING))
        assertEquals("", TelemetryCollector.logsUrl(Environment.PRODUCTION))
        assertEquals(
            "https://mobile-telemetry-dev.shared.apac.paydock.com/v1/logs",
            TelemetryCollector.DEV_LOGS_URL
        )
    }

    @Test
    fun provider_debugOverride_winsInEveryEnvironment() {
        Environment.entries.forEach { environment ->
            val c = SdkTelemetryCredentialsProvider(
                environment,
                debugValues(endpoint = " https://local.invalid/v1/logs ", auth = " synthetic-not-a-credential ")
            ).credentials()

            assertEquals("environment=$environment", "https://local.invalid/v1/logs", c?.endpoint)
            assertEquals("environment=$environment", "synthetic-not-a-credential", c?.authorization)
        }
    }

    @Test
    fun provider_debugOverrideWithoutAuth_sendsNoCredential() {
        val c = SdkTelemetryCredentialsProvider(Environment.PRODUCTION, debugValues(auth = "")).credentials()

        assertEquals("https://local.invalid/v1/logs", c?.endpoint)
        assertEquals("", c?.authorization)
    }

    @Test
    fun provider_debugWithoutEndpoint_fallsBackToTheCollector() {
        val values = debugValues(endpoint = "  ", auth = "synthetic-not-a-credential")

        val sandbox = SdkTelemetryCredentialsProvider(Environment.SANDBOX, values).credentials()

        assertEquals(TelemetryCollector.DEV_LOGS_URL, sandbox?.endpoint)
        assertEquals("", sandbox?.authorization)
        assertNull(SdkTelemetryCredentialsProvider(Environment.PRODUCTION, values).credentials())
    }

    @Test
    fun provider_overrideNotAllowed_ignoresTheLocalValues() {
        val values = LocalTelemetryValues(
            overrideAllowed = false,
            endpoint = "https://local.invalid/v1/logs",
            auth = "synthetic-not-a-credential",
            level = "off"
        )

        val staging = SdkTelemetryCredentialsProvider(Environment.STAGING, values).credentials()

        assertEquals(TelemetryCollector.DEV_LOGS_URL, staging?.endpoint)
        assertEquals("", staging?.authorization)
        assertNull(SdkTelemetryCredentialsProvider(Environment.PRODUCTION, values).credentials())
        assertSame(TelemetryLevel.ALL, telemetryLevel(values))
    }

    @Test
    fun level_releaseIsAll_debugIsTheLocalLevel_allWhenAbsent() {
        assertSame(TelemetryLevel.ALL, telemetryLevel(releaseValues))
        assertSame(TelemetryLevel.ALL, telemetryLevel(debugValues(level = "")))
        assertSame(TelemetryLevel.ERRORS, telemetryLevel(debugValues(level = "errors")))
        assertSame(TelemetryLevel.OFF, telemetryLevel(debugValues(level = "off")))
    }

    @Test
    fun select_levelOff_isNoOp() {
        assertNoOp(select(level = TelemetryLevel.OFF))
    }

    @Test
    fun select_inactive_neverCallsTheFactory() {
        select(provider = unconfigured)
        select(provider = failing)
        select(provider = { TelemetryCredentials(endpoint = "", authorization = "") })
        select(provider = SdkTelemetryCredentialsProvider(Environment.PRODUCTION, releaseValues))
        select(level = TelemetryLevel.OFF)

        assertEquals(0, factoryCalls)
    }

    @Test
    fun select_allConditions_isActiveTelemetryAsBothFacadeAndRuntime() {
        listOf(TelemetryLevel.ERRORS, TelemetryLevel.ALL).forEachIndexed { index, level ->
            val selection = select(level = level)

            val active = selection.telemetry as ActiveTelemetry
            assertSame(active, selection.runtime)
            assertTrue(active.isActive)
            assertSame(credentials, active.credentials)
            assertSame(level, active.level)
            assertEquals(index + 1, factoryCalls)
        }
    }

    @Test
    fun bindTelemetry_inactive_startsNoTelemetryThread() {
        val before = liveTelemetryThreads()

        // Production has no collector URL, and release values carry no override; the context is never touched.
        val bound = bindTelemetry(koin, Environment.PRODUCTION, ContextWrapper(null), releaseValues)

        assertSame(NoOpTelemetry, bound)
        assertSame(NoOpTelemetry, koin.get<Telemetry>())
        // Compared with the threads alive before, so a worker another test class left in this JVM cannot interfere.
        assertEquals(emptySet<Thread>(), liveTelemetryThreads() - before)
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
        val bound = bind(koin, select(provider = unconfigured))

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
     * new Koin and never starts the second runtime, nor sends its test record or test crash.
     */
    @Test
    fun bind_activeWhileActiveIsBound_returnsTheFirst_registersItInTheNewKoin_andNeverStartsTheSecond() {
        val firstRuntime = CountingRuntime()
        val first = activeTelemetry()
        bind(koin, TelemetrySelection(telemetry = first, runtime = firstRuntime))
        val secondRuntime = CountingRuntime()
        val newKoin = koinApplication { modules(defaultTelemetryModule) }.koin

        val bound = bind(
            newKoin,
            TelemetrySelection(telemetry = activeTelemetry(), runtime = secondRuntime),
            sendTestRecord = true,
            testCrash = true
        )

        assertSame(first, bound)
        assertSame(first, newKoin.get<Telemetry>())
        assertEquals(emptyList<String>(), secondRuntime.calls)
        assertEquals(listOf("start"), firstRuntime.calls)
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

    @Test
    fun bind_sendTestRecordTrue_sendsOneTestRecordAfterStart() {
        val runtime = CountingRuntime()

        bind(koin, TelemetrySelection(telemetry = activeTelemetry(), runtime = runtime), sendTestRecord = true)

        assertEquals(listOf("start", "sendTestRecord"), runtime.calls)
    }

    @Test
    fun bind_sendTestRecordFalse_neverSendsATestRecord() {
        val runtime = CountingRuntime()

        bind(koin, TelemetrySelection(telemetry = activeTelemetry(), runtime = runtime), sendTestRecord = false)
        resetBoundTelemetry()
        bind(koin, TelemetrySelection(telemetry = activeTelemetry(), runtime = runtime))

        assertEquals(2, runtime.starts)
        assertEquals(0, runtime.testRecords)
    }

    @Test
    fun bind_sendTestRecord_isNeverAskedOfANoOpOrAFailedStart() {
        val noOp = CountingRuntime()
        val failing = CountingRuntime(failure = IllegalStateException("synthetic start failure"))

        bind(koin, TelemetrySelection(telemetry = NoOpTelemetry, runtime = noOp), sendTestRecord = true)
        val bound =
            bind(koin, TelemetrySelection(telemetry = activeTelemetry(), runtime = failing), sendTestRecord = true)

        assertEquals(0, noOp.testRecords)
        assertEquals(0, failing.testRecords)
        assertSame(NoOpTelemetry, bound)
    }

    @Test
    fun bind_testCrashTrue_asksForTheTestCrashAfterStart() {
        val runtime = CountingRuntime()

        bind(koin, TelemetrySelection(telemetry = activeTelemetry(), runtime = runtime), testCrash = true)

        assertEquals(listOf("start", "throwTestCrash"), runtime.calls)
    }

    @Test
    fun bind_bothDebugFlags_sendTheTestRecordThenAskForTheTestCrash() {
        val runtime = CountingRuntime()

        bind(
            koin,
            TelemetrySelection(telemetry = activeTelemetry(), runtime = runtime),
            sendTestRecord = true,
            testCrash = true
        )

        assertEquals(listOf("start", "sendTestRecord", "throwTestCrash"), runtime.calls)
    }

    @Test
    fun bind_testCrashFalse_neverAsksForATestCrash() {
        val runtime = CountingRuntime()

        bind(koin, TelemetrySelection(telemetry = activeTelemetry(), runtime = runtime), testCrash = false)
        resetBoundTelemetry()
        bind(koin, TelemetrySelection(telemetry = activeTelemetry(), runtime = runtime))

        assertEquals(2, runtime.starts)
        assertEquals(0, runtime.testCrashes)
    }

    @Test
    fun bind_testCrash_isNeverAskedOfANoOpOrAFailedStart() {
        val noOp = CountingRuntime()
        val failing = CountingRuntime(failure = IllegalStateException("synthetic start failure"))

        bind(koin, TelemetrySelection(telemetry = NoOpTelemetry, runtime = noOp), testCrash = true)
        val bound = bind(koin, TelemetrySelection(telemetry = activeTelemetry(), runtime = failing), testCrash = true)

        assertEquals(0, noOp.testCrashes)
        assertEquals(0, failing.testCrashes)
        assertSame(NoOpTelemetry, bound)
    }

    /** R_01, R_12: an active binding installs the crash handler at start and posts the test crash 10 s later. */
    @Test
    fun bind_active_installsTheCrashHandler_andWithTestCrashPostsIt() {
        val selection = select()
        assertEquals(0, handlerSlot.sets)

        bind(koin, selection, testCrash = true)

        assertEquals(1, handlerSlot.sets)
        assertTrue(handlerSlot.current is ChainedCrashHandler)
        assertEquals(listOf(ActiveTelemetry.TEST_CRASH_DELAY_MILLIS), mainPosts)
    }

    @Test
    fun bind_active_withoutTestCrash_postsNothingToTheMainThread() {
        bind(koin, select())

        assertEquals(1, handlerSlot.sets)
        assertTrue(mainPosts.isEmpty())
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
            val factory: (TelemetryCredentials, TelemetryLevel) -> ActiveTelemetry = { c, l ->
                factoryCalls++
                ActiveTelemetry.create(context, c, l, "0.0.0-test", "sandbox")
            }

            val production = SdkTelemetryCredentialsProvider(Environment.PRODUCTION, releaseValues)
            listOf(
                selectTelemetry(unconfigured, TelemetryLevel.ALL, factory),
                selectTelemetry(failing, TelemetryLevel.ALL, factory),
                selectTelemetry(production, TelemetryLevel.ALL, factory),
                selectTelemetry(configured, TelemetryLevel.OFF, factory)
            ).forEach { selection ->
                assertSame(NoOpTelemetry, bind(koin, selection, sendTestRecord = true, testCrash = true))
            }
            assertSame(NoOpTelemetry, bindTelemetry(koin, Environment.PRODUCTION, context, releaseValues))

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

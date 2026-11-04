package com.paydock.core.telemetry

import android.content.ContextWrapper
import com.paydock.core.domain.model.Environment
import com.paydock.telemetry.ActiveTelemetry
import com.paydock.telemetry.DeviceContextProvider
import com.paydock.telemetry.NetworkConnectionType
import com.paydock.telemetry.NoOpTelemetry
import com.paydock.telemetry.ResourceAttributes
import com.paydock.telemetry.SerialTelemetryDispatcher
import com.paydock.telemetry.Telemetry
import com.paydock.telemetry.TelemetryCredentials
import com.paydock.telemetry.TelemetryCredentialsProvider
import com.paydock.telemetry.TelemetryDispatcher
import com.paydock.telemetry.TelemetryLevel
import com.paydock.telemetry.TelemetryRuntime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.Koin
import org.koin.dsl.koinApplication

/**
 * Debug tests for the telemetry activation binding (RS-4, R_05, R_06).
 *
 * Every input is injected: fake credentials providers, a counting factory that builds [ActiveTelemetry] with an
 * immediate dispatcher and a fake device context, and a counting fake runtime — never the generated build values, so
 * the result does not depend on the engineer's `local.properties`. Each test binds into its own fresh Koin instance.
 */
internal class TelemetryBindingTest {

    private lateinit var koin: Koin

    private val credentials = TelemetryCredentials(
        endpoint = "https://otlp.invalid/v1/logs",
        authorization = "synthetic-not-a-credential"
    )

    private val configured = TelemetryCredentialsProvider { credentials }
    private val unconfigured = TelemetryCredentialsProvider { null }
    private val failing = TelemetryCredentialsProvider { error("synthetic provider failure") }

    /** A runtime that counts its starts and can be told to fail. */
    private class CountingRuntime(private val failure: Throwable? = null) : TelemetryRuntime {
        var starts = 0
            private set

        override fun start() {
            starts++
            failure?.let { throw it }
        }
    }

    /** A fixed device context: these tests never build the real one. */
    private val deviceContext = object : DeviceContextProvider {
        override fun current() = ResourceAttributes(
            serviceVersion = "0.0.0-test",
            deploymentEnvironmentName = "sandbox",
            osVersion = "",
            deviceManufacturer = "",
            deviceModelIdentifier = "",
            appInstallationId = "installation-test",
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
        dispatcher = immediate
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
        koin = koinApplication { }.koin
    }

    private fun select(
        buildEnabled: Boolean = true,
        provider: TelemetryCredentialsProvider = configured,
        environment: Environment = Environment.SANDBOX,
        level: TelemetryLevel = TelemetryLevel.ALL,
    ): TelemetrySelection = selectTelemetry(buildEnabled, provider, environment, level, countingFactory)

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
    fun select_buildDisabled_isNoOp() {
        assertNoOp(select(buildEnabled = false))
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
    fun select_production_isNoOp() {
        assertNoOp(select(environment = Environment.PRODUCTION))
    }

    @Test
    fun select_staging_isNoOp() {
        assertNoOp(select(environment = Environment.STAGING))
    }

    @Test
    fun select_levelOff_isNoOp() {
        assertNoOp(select(level = TelemetryLevel.OFF))
    }

    @Test
    fun select_inactive_neverCallsTheFactory() {
        select(buildEnabled = false)
        select(provider = unconfigured)
        select(provider = failing)
        select(environment = Environment.PRODUCTION)
        select(environment = Environment.STAGING)
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

        // Production is never active, whatever the build values say; the context is never touched.
        val bound = bindTelemetry(koin, Environment.PRODUCTION, ContextWrapper(null))

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
        val bound = bind(koin, select(buildEnabled = false))

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
}

package com.paydock.core.telemetry

import com.paydock.core.domain.model.Environment
import com.paydock.telemetry.ActiveTelemetry
import com.paydock.telemetry.NoOpTelemetry
import com.paydock.telemetry.Telemetry
import com.paydock.telemetry.TelemetryCredentials
import com.paydock.telemetry.TelemetryCredentialsProvider
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
 * Every input is injected: fake credentials providers and a counting fake runtime, never the generated build
 * values, so the result does not depend on the engineer's `local.properties`. Each test binds into its own fresh
 * Koin instance.
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

    @Before
    fun setUp() {
        koin = koinApplication { }.koin
    }

    private fun select(
        buildEnabled: Boolean = true,
        provider: TelemetryCredentialsProvider = configured,
        environment: Environment = Environment.SANDBOX,
        level: TelemetryLevel = TelemetryLevel.ALL,
    ): TelemetrySelection = selectTelemetry(buildEnabled, provider, environment, level)

    private fun assertNoOp(selection: TelemetrySelection) {
        assertSame(NoOpTelemetry, selection.telemetry)
        assertSame(NoOpTelemetry, selection.runtime)
    }

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
    fun select_allConditions_isActiveTelemetryAsBothFacadeAndRuntime() {
        listOf(TelemetryLevel.ERRORS, TelemetryLevel.ALL).forEach { level ->
            val selection = select(level = level)

            val active = selection.telemetry as ActiveTelemetry
            assertSame(active, selection.runtime)
            assertTrue(active.isActive)
            assertSame(credentials, active.credentials)
            assertSame(level, active.level)
        }
    }

    @Test
    fun bind_active_registersTheFacade() {
        val selection = select()

        val bound = bind(koin, selection)

        assertSame(selection.telemetry, bound)
        assertSame(selection.telemetry, koin.get<Telemetry>())
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
        val facade = ActiveTelemetry(credentials = credentials, level = TelemetryLevel.ALL)

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
        val facade = ActiveTelemetry(credentials = credentials, level = TelemetryLevel.ALL)

        val bound = bind(koin, TelemetrySelection(telemetry = facade, runtime = runtime))

        assertEquals(1, runtime.starts)
        assertSame(NoOpTelemetry, bound)
        assertSame(NoOpTelemetry, koin.get<Telemetry>())
    }
}

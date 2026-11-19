package com.paydock.core.telemetry

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.paydock.BuildConfig
import com.paydock.core.domain.model.Environment
import com.paydock.telemetry.ActiveTelemetry
import com.paydock.telemetry.NoOpTelemetry
import com.paydock.telemetry.Telemetry
import com.paydock.telemetry.TelemetryActivation
import com.paydock.telemetry.TelemetryRuntime
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module

/*
 * The telemetry activation binding (RS-4): the only file of :mobile-sdk allowed past the telemetry facade
 * (see TelemetrySeamTest). It decides at SDK start whether telemetry runs, registers only the [Telemetry] facade in
 * Koin and keeps the [TelemetryRuntime] lifecycle handle to itself. Its default module binds the no-op facade with
 * the SDK's own modules, so a widget's view model always resolves one.
 */

/** single<Telemetry> { NoOpTelemetry }, part of the SDK's own Koin modules; bind() overrides it (allowOverride). */
internal val defaultTelemetryModule: Module = module { single<Telemetry> { NoOpTelemetry } }

/** What the binding chose. SDK code only ever receives [telemetry]; [runtime] stays in this file. */
internal class TelemetrySelection(val telemetry: Telemetry, val runtime: TelemetryRuntime)

/**
 * The decision with every input injectable: active only for an [endpoint] that is not null or blank (a failed URL
 * lookup arrives as null). [createActive] is called only when the decision passes, so an inactive SDK never builds
 * the worker or touches storage.
 */
internal fun selectTelemetry(endpoint: String?, createActive: (String) -> ActiveTelemetry): TelemetrySelection =
    if (endpoint != null && TelemetryActivation.isActive(endpoint)) {
        val active = createActive(endpoint)
        TelemetrySelection(telemetry = active, runtime = active)
    } else {
        TelemetrySelection(telemetry = NoOpTelemetry, runtime = NoOpTelemetry)
    }

/** Guards [boundTelemetry]. */
private val bindLock = Any()

/** The facade bound in this process, across SDK initialisations; the no-op until an active one is bound. */
@Volatile
private var boundTelemetry: Telemetry = NoOpTelemetry

/**
 * Registers selection.telemetry as the Koin `single<Telemetry>`, then starts the runtime once if it is active; a start
 * that throws binds the no-op instead.
 *
 * One active runtime per process: while an active facade is bound, another active selection is ignored, its runtime
 * never started, and the bound facade is registered in [koin] and returned, so a second SDK initialisation never
 * starts a second runtime sending the same store's files. An inactive selection replaces the bound facade.
 */
internal fun bind(koin: Koin, selection: TelemetrySelection): Telemetry {
    val kept = synchronized(bindLock) {
        if (selection.telemetry.isActive && boundTelemetry.isActive) {
            boundTelemetry
        } else {
            boundTelemetry = selection.telemetry
            null
        }
    }
    if (kept != null) {
        koin.loadModules(listOf(module { single<Telemetry> { kept } }), allowOverride = true)
        return kept // never starts selection.runtime
    }
    koin.loadModules(listOf(module { single<Telemetry> { selection.telemetry } }), allowOverride = true)
    if (!selection.telemetry.isActive) return selection.telemetry
    return runCatching { selection.runtime.start() }.fold(
        onSuccess = { selection.telemetry },
        onFailure = { bindNoOp(koin) }
    )
}

/**
 * The one binding at SDK start (RS-4): looks up the environment's collector URL in [TelemetryCollector] (D22), selects,
 * binds. Never throws. The table is the only input: no build value or local setting is read, in any build type. A
 * lookup that fails or returns a blank URL binds the no-op. When active, the telemetry is built with the app context,
 * the collector URL, the SDK version and the environment name (R_08, R_10).
 */
internal fun bindTelemetry(koin: Koin, environment: Environment, context: Context): Telemetry = runCatching {
    val endpoint = runCatching { TelemetryCollector.logsUrl(environment) }.getOrNull()
    val selection = selectTelemetry(endpoint) { url ->
        ActiveTelemetry.create(context, url, BuildConfig.SDK_VERSION, environment.name.lowercase())
    }
    bind(koin, selection)
}.getOrElse {
    runCatching { bindNoOp(koin) }.getOrDefault(NoOpTelemetry)
}

/** Registers [NoOpTelemetry] as the Koin `single<Telemetry>`, binds it for the process and returns it. */
private fun bindNoOp(koin: Koin): Telemetry {
    synchronized(bindLock) { boundTelemetry = NoOpTelemetry }
    koin.loadModules(listOf(module { single<Telemetry> { NoOpTelemetry } }), allowOverride = true)
    return NoOpTelemetry
}

/** Test-only: forgets the process's bound facade. */
@VisibleForTesting
internal fun resetBoundTelemetry() {
    synchronized(bindLock) { boundTelemetry = NoOpTelemetry }
}

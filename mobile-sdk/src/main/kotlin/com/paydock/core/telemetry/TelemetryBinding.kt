package com.paydock.core.telemetry

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.paydock.BuildConfig
import com.paydock.core.domain.model.Environment
import com.paydock.telemetry.ActiveTelemetry
import com.paydock.telemetry.NoOpTelemetry
import com.paydock.telemetry.Telemetry
import com.paydock.telemetry.TelemetryActivation
import com.paydock.telemetry.TelemetryCredentials
import com.paydock.telemetry.TelemetryCredentialsProvider
import com.paydock.telemetry.TelemetryLevel
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

/** The generated build values; release: override off and empty (D14, R_06). */
internal class LocalTelemetryValues(
    val overrideAllowed: Boolean, // TelemetryBuildValues.ENABLED: true only in the debug build type
    val endpoint: String,
    val auth: String,
    val level: String,
) {
    companion object {
        fun fromBuild() = LocalTelemetryValues(
            TelemetryBuildValues.ENABLED,
            TelemetryBuildValues.OTLP_ENDPOINT,
            TelemetryBuildValues.OTLP_AUTH,
            TelemetryBuildValues.LEVEL,
        )
    }
}

/** Debug override first, then the environment's collector with no credential (D5, D22). */
internal class SdkTelemetryCredentialsProvider(
    private val environment: Environment,
    private val values: LocalTelemetryValues,
) : TelemetryCredentialsProvider {
    override fun credentials(): TelemetryCredentials? {
        val localEndpoint = values.endpoint.trim()
        if (values.overrideAllowed && localEndpoint.isNotEmpty()) {
            return TelemetryCredentials(endpoint = localEndpoint, authorization = values.auth.trim())
        }
        val url = TelemetryCollector.logsUrl(environment)
        return if (url.isEmpty()) null else TelemetryCredentials(endpoint = url, authorization = "")
    }
}

/** ALL in a release build (D23); the local level in a debug build, ALL when absent. */
internal fun telemetryLevel(values: LocalTelemetryValues): TelemetryLevel =
    if (values.overrideAllowed) TelemetryLevel.parse(values.level) else TelemetryLevel.ALL

/** What the binding chose. SDK code only ever receives [telemetry]; [runtime] stays in this file. */
internal class TelemetrySelection(val telemetry: Telemetry, val runtime: TelemetryRuntime)

/**
 * The decision with every input injectable. A provider that throws counts as no credentials. [createActive] is called
 * only when the decision passes, so an inactive SDK never builds the worker or touches storage.
 */
internal fun selectTelemetry(
    credentialsProvider: TelemetryCredentialsProvider,
    level: TelemetryLevel,
    createActive: (TelemetryCredentials, TelemetryLevel) -> ActiveTelemetry,
): TelemetrySelection {
    val credentials = runCatching { credentialsProvider.credentials() }.getOrNull()
    val isActive = TelemetryActivation.isActive(credentials = credentials, level = level)
    return if (isActive && credentials != null) {
        val active = createActive(credentials, level)
        TelemetrySelection(telemetry = active, runtime = active)
    } else {
        TelemetrySelection(telemetry = NoOpTelemetry, runtime = NoOpTelemetry)
    }
}

/** Guards [boundTelemetry]. */
private val bindLock = Any()

/** The facade bound in this process, across SDK initialisations; the no-op until an active one is bound. */
@Volatile
private var boundTelemetry: Telemetry = NoOpTelemetry

/**
 * Registers selection.telemetry as the Koin `single<Telemetry>`, then starts the runtime once if it is active. After a
 * successful start, [sendTestRecord] (debug-only, R_16) asks the runtime to store and send one test record, and
 * [testCrash] (debug-only, R_12) asks it to crash the app with a test crash.
 *
 * One active runtime per process: while an active facade is bound, another active selection is ignored, its runtime
 * never started, and the bound facade is registered in [koin] and returned, so a second SDK initialisation never
 * starts a second runtime sending the same store's files. An inactive selection replaces the bound facade.
 */
internal fun bind(
    koin: Koin,
    selection: TelemetrySelection,
    sendTestRecord: Boolean = false,
    testCrash: Boolean = false,
): Telemetry {
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
        onSuccess = {
            if (sendTestRecord) runCatching { selection.runtime.sendTestRecord() }
            if (testCrash) runCatching { selection.runtime.throwTestCrash() }
            selection.telemetry
        },
        onFailure = { bindNoOp(koin) }
    )
}

/**
 * The one binding at SDK start (RS-4): reads TelemetryBuildValues, selects, binds. Never throws. Credentials come from
 * [SdkTelemetryCredentialsProvider]: the debug override first, in any environment, then the environment's collector
 * (D22); the level is ALL in a release build (D23). When active, the telemetry is built with the app context, the SDK
 * version and the environment name (R_08, R_10). [values] is injectable for tests; the SDK passes the build's.
 */
internal fun bindTelemetry(
    koin: Koin,
    environment: Environment,
    context: Context,
    values: LocalTelemetryValues = LocalTelemetryValues.fromBuild(),
): Telemetry = runCatching {
    val selection = selectTelemetry(
        credentialsProvider = SdkTelemetryCredentialsProvider(environment, values),
        level = telemetryLevel(values),
        createActive = { c, l ->
            ActiveTelemetry.create(context, c, l, BuildConfig.SDK_VERSION, environment.name.lowercase())
        }
    )
    bind(
        koin,
        selection,
        sendTestRecord = TelemetryBuildValues.SEND_TEST_RECORD,
        testCrash = TelemetryBuildValues.TEST_CRASH
    )
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

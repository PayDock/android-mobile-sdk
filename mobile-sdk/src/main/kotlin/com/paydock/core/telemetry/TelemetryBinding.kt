package com.paydock.core.telemetry

import android.content.Context
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
import org.koin.dsl.module

/*
 * The telemetry activation binding (RS-4): the only file of :mobile-sdk allowed past the telemetry facade
 * (see TelemetrySeamTest). It decides at SDK start whether telemetry runs, registers only the [Telemetry] facade in
 * Koin and keeps the [TelemetryRuntime] lifecycle handle to itself.
 */

/** Reads the debug-only build values; release values are empty, so release has none (D14). */
internal object BuildValuesCredentialsProvider : TelemetryCredentialsProvider {
    override fun credentials(): TelemetryCredentials? {
        val endpoint = TelemetryBuildValues.OTLP_ENDPOINT.trim()
        val authorization = TelemetryBuildValues.OTLP_AUTH.trim()
        return if (endpoint.isEmpty() || authorization.isEmpty()) {
            null
        } else {
            TelemetryCredentials(endpoint = endpoint, authorization = authorization)
        }
    }
}

/** What the binding chose. SDK code only ever receives [telemetry]; [runtime] stays in this file. */
internal class TelemetrySelection(val telemetry: Telemetry, val runtime: TelemetryRuntime)

/**
 * The decision with every input injectable. A provider that throws counts as no credentials. [createActive] is called
 * only when the decision passes, so an inactive SDK never builds the worker or touches storage.
 */
internal fun selectTelemetry(
    buildEnabled: Boolean,
    credentialsProvider: TelemetryCredentialsProvider,
    environment: Environment,
    level: TelemetryLevel,
    createActive: (TelemetryCredentials, TelemetryLevel) -> ActiveTelemetry,
): TelemetrySelection {
    val credentials = runCatching { credentialsProvider.credentials() }.getOrNull()
    val isActive = TelemetryActivation.isActive(
        buildEnabled = buildEnabled,
        credentials = credentials,
        isSandbox = environment == Environment.SANDBOX,
        level = level
    )
    return if (isActive && credentials != null) {
        val active = createActive(credentials, level)
        TelemetrySelection(telemetry = active, runtime = active)
    } else {
        TelemetrySelection(telemetry = NoOpTelemetry, runtime = NoOpTelemetry)
    }
}

/**
 * Registers selection.telemetry as the Koin `single<Telemetry>`, then starts the runtime once if it is active. After a
 * successful start, [sendTestRecord] (debug-only, R_16) asks the runtime to store and send one test record, and
 * [testCrash] (debug-only, R_12) asks it to crash the app with a test crash.
 */
internal fun bind(
    koin: Koin,
    selection: TelemetrySelection,
    sendTestRecord: Boolean = false,
    testCrash: Boolean = false,
): Telemetry {
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
 * The one binding at SDK start (RS-4): reads TelemetryBuildValues, selects, binds. Never throws. When active, the
 * telemetry is built with the app context, the SDK version and the environment name (R_08, R_10).
 */
internal fun bindTelemetry(koin: Koin, environment: Environment, context: Context): Telemetry = runCatching {
    val selection = selectTelemetry(
        buildEnabled = TelemetryBuildValues.ENABLED,
        credentialsProvider = BuildValuesCredentialsProvider,
        environment = environment,
        level = TelemetryLevel.parse(TelemetryBuildValues.LEVEL),
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

/** Registers [NoOpTelemetry] as the Koin `single<Telemetry>` and returns it. */
private fun bindNoOp(koin: Koin): Telemetry {
    koin.loadModules(listOf(module { single<Telemetry> { NoOpTelemetry } }), allowOverride = true)
    return NoOpTelemetry
}

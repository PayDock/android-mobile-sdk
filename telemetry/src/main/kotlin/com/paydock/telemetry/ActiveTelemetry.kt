package com.paydock.telemetry

import android.content.Context
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Bound when activation passes: the telemetry facade and lifecycle on one serial worker.
 *
 * [report] wraps the event as an intent, posts it to the worker and returns at once: it never blocks and never
 * throws (R_03). Everything else runs on the worker: the pure [TelemetryReducer] turns each intent into the next
 * state and a list of effects (R_01), and one [TelemetryEffectHandler] runs them against the ports (R_02). Each
 * instance has one session ID, stamped on every pending record (R_04).
 */
internal class ActiveTelemetry(
    val credentials: TelemetryCredentials,
    val level: TelemetryLevel,
    private val deviceContextProvider: DeviceContextProvider,
    private val clock: Clock = Clock.SYSTEM,
    idGenerator: IdGenerator = IdGenerator.RANDOM,
    private val dispatcher: TelemetryDispatcher = SerialTelemetryDispatcher(),
    ports: Ports = Ports(),
) : Telemetry, TelemetryRuntime {

    /** Where records go. No-ops for now, so nothing leaves memory (R_02). */
    class Ports(
        val queue: RecordQueue = NoOpRecordQueue,
        val store: DurableStore = NoOpDurableStore,
        val exporter: Exporter = NoOpExporter,
    )

    val sessionId: String = idGenerator.newId()

    override val isActive: Boolean = true

    /** Null until [start] has run on the worker. */
    @Volatile
    var resourceAttributes: ResourceAttributes? = null
        private set

    /** Written only on the worker; volatile so [currentState] can read it from any thread. */
    @Volatile
    private var state: TelemetryState = TelemetryState(level = level, enabled = true, sessionId = sessionId)

    private val started = AtomicBoolean(false)

    private val handler = TelemetryEffectHandler(
        queue = ports.queue,
        store = ports.store,
        exporter = ports.exporter,
        deviceContext = deviceContextProvider,
        scheduleFlush = { delay -> dispatcher.postDelayed(delay) { dispatch(TelemetryIntent.Flush) } }
    )

    override fun report(event: TelemetryEvent) {
        runCatching { dispatcher.post { dispatch(TelemetryIntent.Reported(event)) } }
    }

    override fun start() {
        if (!started.compareAndSet(false, true)) return
        runCatching {
            dispatcher.post {
                runCatching { resourceAttributes = deviceContextProvider.current() }
            }
        }
    }

    /** For tests. */
    internal fun currentState(): TelemetryState = state

    /** Runs only on the worker. */
    private fun dispatch(intent: TelemetryIntent) {
        val (next, effects) = TelemetryReducer.reduce(state, intent, clock.now())
        state = next
        handler.run(effects)
    }

    companion object {
        /** Production wiring: Android platform, private storage, the real worker, no-op ports. */
        fun create(
            context: Context,
            credentials: TelemetryCredentials,
            level: TelemetryLevel,
            sdkVersion: String,
            environmentName: String,
        ): ActiveTelemetry {
            val appContext = context.applicationContext
            return ActiveTelemetry(
                credentials = credentials,
                level = level,
                deviceContextProvider = DefaultDeviceContextProvider(
                    platform = AndroidPlatformInfo(appContext),
                    installationIds = InstallationIdStore(
                        store = SharedPreferencesKeyValueStore(appContext),
                        idGenerator = IdGenerator.RANDOM
                    ),
                    sdkVersion = sdkVersion,
                    environmentName = environmentName
                )
            )
        }
    }
}

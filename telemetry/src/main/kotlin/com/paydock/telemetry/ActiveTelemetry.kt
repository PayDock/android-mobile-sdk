package com.paydock.telemetry

import android.content.Context
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Bound when activation passes: the telemetry facade and lifecycle on one serial worker.
 *
 * [report] wraps the event as an intent, posts it to the worker and returns at once: it never blocks and never
 * throws (R_03). Everything else runs on the worker: the pure [TelemetryReducer] turns each intent into the next
 * state and a list of effects (R_01), and one [TelemetryEffectHandler] runs them against the ports (R_02). Each
 * instance has one session ID and one [RecordSequence], stamped on every record (R_04).
 *
 * The follow-up intents an effect returns are applied in the same worker task, first in, first out, before any
 * queued intent, so none is dropped or overtaken by a report. Reports use the bounded `post`; every module signal
 * (the timer, the retry, the background callback, the debug send) uses `postDelayed`, so it is never dropped.
 */
internal class ActiveTelemetry(
    val credentials: TelemetryCredentials,
    val level: TelemetryLevel,
    private val deviceContextProvider: DeviceContextProvider,
    private val ports: Ports,
    private val clock: Clock = Clock.SYSTEM,
    private val monotonic: MonotonicClock = MonotonicClock.SYSTEM,
    idGenerator: IdGenerator = IdGenerator.RANDOM,
    private val dispatcher: TelemetryDispatcher = SerialTelemetryDispatcher(),
) : Telemetry, TelemetryRuntime {

    /** Where records go: the disk queue, the exporter and the app lifecycle. */
    class Ports(
        val store: DurableStore,
        val exporter: Exporter,
        val lifecycle: AppLifecycle,
        val retryPolicy: RetryPolicy = RetryPolicy(),
    )

    val sessionId: String = idGenerator.newId()

    /** This session's record IDs; a crash handler takes its IDs here too. */
    val sequence: RecordSequence = RecordSequence()

    override val isActive: Boolean = true

    /** Null until [start] has run on the worker. */
    @Volatile
    var resourceAttributes: ResourceAttributes? = null
        private set

    /** Written only on the worker; volatile so [currentState] can read it from any thread. */
    @Volatile
    private var state: TelemetryState = TelemetryState(level = level, enabled = true, sessionId = sessionId)

    private val started = AtomicBoolean(false)

    private val runner = ExportRunner(
        store = ports.store,
        exporter = ports.exporter,
        deviceContext = deviceContextProvider,
        monotonic = monotonic,
        retryPolicy = ports.retryPolicy,
        scheduleRetry = { delay -> signal(delay, TelemetryIntent.Flush(TelemetryIntent.FlushSource.RETRY)) }
    )

    private val handler = TelemetryEffectHandler(
        store = ports.store,
        runner = runner,
        deviceContext = deviceContextProvider,
        clock = clock,
        scheduleFlush = { delay -> signal(delay, TelemetryIntent.Flush(TelemetryIntent.FlushSource.TIMER)) }
    )

    override fun report(event: TelemetryEvent) {
        runCatching { dispatcher.post { dispatch(TelemetryIntent.Reported(event)) } }
    }

    /** Once: builds the device context, registers the lifecycle, counts the stored records and flushes them. */
    override fun start() {
        if (!started.compareAndSet(false, true)) return
        runCatching {
            dispatcher.post {
                runCatching { resourceAttributes = deviceContextProvider.current() }
                runCatching { ports.lifecycle.register { signal(0, TelemetryIntent.AppBackgrounded) } }
                runCatching { ports.store.count() }.onSuccess { dispatch(TelemetryIntent.PendingCounted(it)) }
                dispatch(TelemetryIntent.Flush(TelemetryIntent.FlushSource.START))
            }
        }
    }

    /** Debug only (R_16): reports a test error like any report, then flushes like a module signal. */
    override fun sendTestRecord() {
        val error = TelemetryEvent.ErrorInfo(type = TEST_RECORD_TYPE, frames = emptyList())
        report(TelemetryEvent.SdkError(error = error, errorCode = TEST_RECORD_CODE))
        signal(0, TelemetryIntent.Flush(TelemetryIntent.FlushSource.DEBUG_SEND))
    }

    /** For tests. */
    internal fun currentState(): TelemetryState = state

    /** A module signal: `postDelayed`, which the capacity check never applies to, so it is never dropped. */
    private fun signal(delayMillis: Long, intent: TelemetryIntent) {
        runCatching { dispatcher.postDelayed(delayMillis) { dispatch(intent) } }
    }

    /** Runs only on the worker: the intent, then every follow-up, first in, first out, in this one task. */
    private fun dispatch(intent: TelemetryIntent) {
        val intents = ArrayDeque<TelemetryIntent>()
        intents.addLast(intent)
        while (intents.isNotEmpty()) {
            val next = intents.removeFirst()
            val (reduced, effects) = TelemetryReducer.reduce(state, next, clock.now(), monotonic.elapsed(), sequence)
            state = reduced
            intents.addAll(handler.run(effects))
        }
    }

    companion object {
        const val TEST_RECORD_CODE = "TelemetryTestRecord"
        const val TEST_RECORD_TYPE = "com.paydock.telemetry.TelemetryTestRecord"

        /**
         * Production wiring: Android platform, private storage, the real worker, the file store in the app's
         * no-backup directory, the OTLP exporter on its own client and the process lifecycle. No I/O on the calling
         * thread: the directory, the client and the lifecycle registration are all resolved later, on the worker.
         */
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
                ),
                ports = Ports(
                    store = FileDurableStore { File(appContext.noBackupFilesDir, FileDurableStore.DIRECTORY_NAME) },
                    exporter = OtlpExporter(
                        OtlpHttpTransport(credentials.endpoint) { TelemetryHttpClient.create(credentials.endpoint) },
                        credentials
                    ),
                    lifecycle = ProcessAppLifecycle
                )
            )
        }
    }
}

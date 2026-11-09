package com.paydock.telemetry

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
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
 *
 * [start] also installs the process's crash handler through [crashHandlers] (R_01), on the calling thread and never
 * at construction, so only an active, started runtime ever touches the default handler.
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
    private val crashHandlers: CrashHandlerInstaller = CrashHandlerInstaller.PROCESS,
    private val postToMain: (delayMillis: Long, task: () -> Unit) -> Unit = ::postToMainLooper,
) : Telemetry, TelemetryRuntime {

    /**
     * Where records go: the disk queue, the exporter and the app lifecycle; the transport's clock offset; and where
     * the previous runs' exits come from.
     */
    class Ports(
        val store: DurableStore,
        val exporter: Exporter,
        val lifecycle: AppLifecycle,
        val retryPolicy: RetryPolicy = RetryPolicy(),
        val clockOffset: ClockOffset = ClockOffset(Clock.SYSTEM), // create() passes the transport's; fresh by default
        val exitReports: ExitReportReader = ExitReportReader.NONE,
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
        clockOffset = ports.clockOffset,
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

    /**
     * Once: installs the crash handler on the calling thread (no I/O: building the recorder only stores references),
     * then, on the worker, builds the device context, registers the lifecycle, counts the stored records, reports the
     * previous runs' exits and moves their marker only once every one of them is stored (R_04, R_07): when a save
     * fails, the marker stays and the exits are read again next launch. Then it flushes them all.
     */
    override fun start() {
        if (!started.compareAndSet(false, true)) return
        runCatching {
            crashHandlers.install(CrashRecorder(sessionId, sequence, ports.store, deviceContextProvider, clock))
        }
        runCatching {
            dispatcher.post {
                runCatching { resourceAttributes = deviceContextProvider.current() }
                runCatching { ports.lifecycle.register { signal(0, TelemetryIntent.AppBackgrounded) } }
                runCatching { ports.store.count() }.onSuccess { dispatch(TelemetryIntent.PendingCounted(it)) }
                runCatching {
                    val now = clock.now()
                    val found = ports.exitReports.read(now)
                    var failed = false
                    found.events.forEach { if (dispatch(TelemetryIntent.Reported(it))) failed = true }
                    // A failed save leaves the marker, so the exits are read again next launch (at least once).
                    if (!failed) found.newestTimestamp?.let { ports.exitReports.markProcessed(it, now) }
                }
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

    /** Debug only (R_12): crashes the app [TEST_CRASH_DELAY_MILLIS] later on the main thread, with no message. */
    override fun throwTestCrash() {
        runCatching { postToMain(TEST_CRASH_DELAY_MILLIS) { throw TelemetryTestCrash() } }
    }

    /** For tests. */
    internal fun currentState(): TelemetryState = state

    /** A module signal: `postDelayed`, which the capacity check never applies to, so it is never dropped. */
    private fun signal(delayMillis: Long, intent: TelemetryIntent) {
        runCatching { dispatcher.postDelayed(delayMillis) { dispatch(intent) } }
    }

    /**
     * Runs only on the worker: the intent, then every follow-up, first in, first out, in this one task. True when any
     * effect of the intent or its follow-ups failed; an effect the reducer drops on purpose is not a failure.
     */
    private fun dispatch(intent: TelemetryIntent): Boolean {
        var failed = false
        val intents = ArrayDeque<TelemetryIntent>()
        intents.addLast(intent)
        while (intents.isNotEmpty()) {
            val next = intents.removeFirst()
            val (reduced, effects) = TelemetryReducer.reduce(state, next, clock.now(), monotonic.elapsed(), sequence)
            state = reduced
            val outcome = handler.run(effects)
            if (outcome.failed) failed = true
            intents.addAll(outcome.intents)
        }
        return failed
    }

    companion object {
        const val TEST_RECORD_CODE = "TelemetryTestRecord"
        const val TEST_RECORD_TYPE = "com.paydock.telemetry.TelemetryTestRecord"
        const val TEST_CRASH_DELAY_MILLIS = 10_000L

        /**
         * Production wiring: Android platform, private storage, the real worker, the file store in the app's
         * no-backup directory, the OTLP exporter on its own client, the process lifecycle and the OS's exit reports
         * (none below API 30). No I/O on the calling thread: the directory, the client, the lifecycle registration,
         * the exit reports and their marker are all resolved later, on the worker.
         */
        fun create(
            context: Context,
            credentials: TelemetryCredentials,
            level: TelemetryLevel,
            sdkVersion: String,
            environmentName: String,
        ): ActiveTelemetry {
            val appContext = context.applicationContext
            val clockOffset = ClockOffset(Clock.SYSTEM)
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
                        OtlpHttpTransport(credentials.endpoint, clockOffset) {
                            TelemetryHttpClient.create(credentials.endpoint)
                        },
                        credentials
                    ),
                    lifecycle = ProcessAppLifecycle,
                    clockOffset = clockOffset,
                    exitReports = ExitReportReader(
                        AndroidExitInfoSource.create(appContext),
                        SharedPreferencesKeyValueStore(appContext),
                        exitMarkerKey()
                    )
                )
            )
        }
    }
}

/**
 * The marker key of this process's exits: one per process on API 30+, where each process reads only its own exits;
 * below, the source is [ExitInfoSource.NONE] and the key is never used.
 */
private fun exitMarkerKey(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    ExitReportReader.markerKeyFor(AndroidExitInfoSource.processName())
} else {
    ExitReportReader.MARKER_KEY
}

/** The one use of the main looper here: runs [task] on the main thread after [delayMillis]. */
private fun postToMainLooper(delayMillis: Long, task: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed({ task() }, delayMillis)
}

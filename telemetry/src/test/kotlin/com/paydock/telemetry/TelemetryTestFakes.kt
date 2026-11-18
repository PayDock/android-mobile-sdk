package com.paydock.telemetry

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream

/** Hand-written fakes shared by the core tests. No mocking library. */
internal object TelemetryTestFakes {

    const val INSTALLATION_ID = "00000000-0000-4000-8000-000000000001"
    const val SESSION_ID = "00000000-0000-4000-8000-000000000101"
    const val T0 = 1_700_000_000_000L

    val resourceAttributes = ResourceAttributes(
        serviceVersion = "0.0.0-test",
        deploymentEnvironmentName = "sandbox",
        osVersion = "15",
        deviceManufacturer = "TestMaker",
        deviceModelIdentifier = "TestModel",
        appInstallationId = INSTALLATION_ID,
        appId = "com.example.merchant",
        appVersion = "1.0.0",
        appDebuggable = true
    )

    val credentials = TelemetryCredentials(
        endpoint = "https://otlp.invalid/v1/logs",
        authorization = "synthetic-not-a-credential"
    )

    fun sdkError(type: String = "java.lang.IllegalStateException", frames: List<String> = emptyList()) =
        TelemetryEvent.SdkError(error = TelemetryEvent.ErrorInfo(type = type, frames = frames))

    fun crash(type: String = "java.lang.IllegalStateException") = TelemetryEvent.Crash(
        error = TelemetryEvent.ErrorInfo(type = type, frames = emptyList()),
        source = TelemetryEvent.CrashSource.UNCAUGHT_HANDLER,
        kind = TelemetryEvent.CrashKind.EXCEPTION
    )

    /** A device context that counts how often it is built. */
    class FakeDeviceContextProvider(
        private val attributes: ResourceAttributes = resourceAttributes,
    ) : DeviceContextProvider {
        @Volatile
        var currentCalls = 0
            private set

        override fun current(): ResourceAttributes {
            currentCalls++
            return attributes
        }

        override fun networkConnectionType(): NetworkConnectionType = NetworkConnectionType.WIFI
    }

    /** Runs every task at once on the caller's thread; delayed tasks are kept to run by hand. */
    class ImmediateDispatcher : TelemetryDispatcher {
        val delayed = mutableListOf<Pair<Long, () -> Unit>>()

        override fun post(task: () -> Unit): Boolean {
            task()
            return true
        }

        override fun postDelayed(delayMillis: Long, task: () -> Unit): Boolean {
            delayed += delayMillis to task
            return true
        }

        /** Runs and forgets the delayed tasks posted so far. */
        fun runDelayed() {
            val tasks = delayed.toList()
            delayed.clear()
            tasks.forEach { (_, task) -> task() }
        }

        /** Runs and forgets only the delayed tasks posted so far with [delayMillis]; keeps the others. */
        fun runDelayed(delayMillis: Long) {
            val tasks = delayed.filter { it.first == delayMillis }
            delayed.removeAll(tasks)
            tasks.forEach { (_, task) -> task() }
        }
    }

    /** A dispatcher that throws from every call. */
    object ThrowingDispatcher : TelemetryDispatcher {
        override fun post(task: () -> Unit): Boolean = error("synthetic dispatcher failure")
        override fun postDelayed(delayMillis: Long, task: () -> Unit): Boolean = error("synthetic dispatcher failure")
    }

    /** The UUID-shaped ID number [n], as the allowlist requires of `session.id` and installation IDs. */
    fun uuid(n: Int): String = "00000000-0000-4000-8000-%012d".format(n)

    /** A record as [RecordFactory] builds it, filtered, for the store and runner tests. */
    fun record(
        sequence: Long,
        sessionId: String = SESSION_ID,
        event: TelemetryEvent = sdkError(),
        createdAt: Long = T0 + sequence,
    ): TelemetryRecord = Allowlist.filter(
        RecordFactory.create(PendingRecord("$sessionId-$sequence", sessionId, event, createdAt), NetworkConnectionType.WIFI)
    )

    /** An in-memory [DurableStore]: oldest by time then sequence, pruned by age only. Records each call's thread. */
    class InMemoryStore : DurableStore {
        val records = linkedMapOf<String, TelemetryRecord>()
        val saved = mutableListOf<TelemetryRecord>()
        val removed = mutableListOf<String>()
        val threads: MutableList<String> = java.util.Collections.synchronizedList(mutableListOf())

        /** Added to every prune's result, to model a store that dropped records for space. */
        var extraPrune = PruneResult()

        @Synchronized
        override fun save(record: TelemetryRecord) {
            threads += Thread.currentThread().name
            saved += record
            records[record.id] = record
        }

        @Synchronized
        override fun oldest(max: Int): List<TelemetryRecord> {
            threads += Thread.currentThread().name
            return records.values.sortedWith(
                compareBy<TelemetryRecord>({ it.timeUnixMillis }, { it.id.substringAfterLast('-').toLongOrNull() })
            ).take(max)
        }

        @Synchronized
        override fun remove(ids: Collection<String>) {
            threads += Thread.currentThread().name
            removed += ids
            ids.forEach { records.remove(it) }
        }

        @Synchronized
        override fun prune(now: Long): PruneResult {
            threads += Thread.currentThread().name
            val tooOld = records.values.filter { now - it.timeUnixMillis > TelemetryLimits.RECORD_MAX_AGE_MILLIS }
            tooOld.forEach { records.remove(it.id) }
            return PruneResult(tooOld = tooOld.count { !it.isDroppedNotice } + extraPrune.tooOld, extraPrune.storeFull)
        }

        @Synchronized
        override fun count(): Int {
            threads += Thread.currentThread().name
            return records.size
        }
    }

    /**
     * A [FileDurableStore] on [directory] whose lock a background thread holds until [release], as the worker would
     * mid-flush: the holder blocks inside the store's first use, while it resolves the directory.
     */
    class HeldFileStore(directory: File) {
        private val entered = CountDownLatch(1)
        private val released = CountDownLatch(1)

        val store = FileDurableStore {
            entered.countDown()
            released.await()
            directory
        }

        private val holder = Thread({ store.count() }, "store-holder").apply {
            isDaemon = true
            start()
        }

        init {
            check(entered.await(HOLD_WAIT_SECONDS, TimeUnit.SECONDS)) { "the holder never took the lock" }
        }

        fun release() {
            released.countDown()
            holder.join(TimeUnit.SECONDS.toMillis(HOLD_WAIT_SECONDS))
        }

        private companion object {
            const val HOLD_WAIT_SECONDS = 5L
        }
    }

    /** One sent request, as the transport saw it, with its body un-gzipped and parsed. */
    class SentRequest(val payload: ByteArray, val headers: Map<String, String>, val thread: String) {
        val json: JsonObject by lazy {
            Json.parseToJsonElement(GZIPInputStream(payload.inputStream()).use { it.readBytes() }.decodeToString())
                .jsonObject
        }
        val resourceLogs: JsonObject
            get() = json["resourceLogs"]!!.jsonArray.single().jsonObject
        val logRecords: List<JsonObject>
            get() = resourceLogs["scopeLogs"]!!.jsonArray.single().jsonObject["logRecords"]!!.jsonArray
                .map { it.jsonObject }

        fun resourceValue(key: String): String? = attributeValue(resourceLogs["resource"]!!.jsonObject, key)

        fun recordValues(key: String): List<String?> = logRecords.map { attributeValue(it, key) }

        fun recordIds(): List<String?> = recordValues("log.record.uid")

        private fun attributeValue(owner: JsonObject, key: String): String? = owner["attributes"]!!.jsonArray
            .map { it.jsonObject }
            .firstOrNull { it["key"]!!.jsonPrimitive.content == key }
            ?.get("value")?.jsonObject?.values?.single()?.jsonPrimitive?.content
    }

    /** A transport that records every send and answers from [script], then with [otherwise]. */
    class FakeTransport(
        vararg script: TransportResult,
        private val otherwise: TransportResult = TransportResult.Success(204),
    ) : TelemetryTransport {
        private val answers = ArrayDeque(script.toList())
        val sent: MutableList<SentRequest> = java.util.Collections.synchronizedList(mutableListOf())

        /** Runs before each answer, on the sending thread: a hook to block or observe a send. */
        @Volatile
        var beforeAnswer: () -> Unit = {}

        override fun send(payload: ByteArray, headers: Map<String, String>): TransportResult {
            sent += SentRequest(payload, headers, Thread.currentThread().name)
            beforeAnswer()
            return synchronized(answers) { answers.removeFirstOrNull() } ?: otherwise
        }
    }

    /** A lifecycle that counts its registrations and keeps the callback. */
    class FakeLifecycle : AppLifecycle {
        @Volatile
        var registrations = 0
            private set

        @Volatile
        var onBackground: (() -> Unit)? = null
            private set

        override fun register(onBackground: () -> Unit) {
            registrations++
            this.onBackground = onBackground
        }
    }

    /** A settable monotonic clock in milliseconds. */
    class FakeMonotonic(var elapsedMillis: Long = 0L) : MonotonicClock {
        override fun elapsed(): Long = elapsedMillis
    }

    /** A settable clock in UTC epoch milliseconds. */
    class FakeClock(var nowMillis: Long = 1_700_000_000_000L) : Clock {
        override fun now(): Long = nowMillis
    }

    /** A platform that counts every read; its connection type can be changed between reads. */
    class FakePlatformInfo(var connection: NetworkConnectionType = NetworkConnectionType.WIFI) : PlatformInfo {
        var osVersionReads = 0
            private set
        var manufacturerReads = 0
            private set
        var modelReads = 0
            private set
        var appIdReads = 0
            private set
        var appVersionReads = 0
            private set
        var appDebuggableReads = 0
            private set
        var connectionReads = 0
            private set

        override val osVersion: String
            get() = "15".also { osVersionReads++ }
        override val manufacturer: String
            get() = "TestMaker".also { manufacturerReads++ }
        override val model: String
            get() = "TestModel".also { modelReads++ }
        override val appId: String
            get() = "com.example.merchant".also { appIdReads++ }
        override val appVersion: String
            get() = "1.0.0".also { appVersionReads++ }
        override val appDebuggable: Boolean
            get() = true.also { appDebuggableReads++ }

        override fun networkConnectionType(): NetworkConnectionType = connection.also { connectionReads++ }
    }

    /** A key-value store over a plain map; share the map to model a restart, use a new one to model an install. */
    class MapKeyValueStore(val values: MutableMap<String, String> = mutableMapOf()) : KeyValueStore {
        var reads = 0
            private set
        var writes = 0
            private set

        override fun getString(key: String): String? {
            reads++
            return values[key]
        }

        override fun putString(key: String, value: String) {
            writes++
            values[key] = value
        }
    }

    /** A default-handler slot over a plain field: never the test JVM's own handler. Counts its writes. */
    class FakeHandlerSlot(var current: Thread.UncaughtExceptionHandler? = null) : DefaultHandlerSlot {
        var sets = 0
            private set

        override fun get(): Thread.UncaughtExceptionHandler? = current

        override fun set(handler: Thread.UncaughtExceptionHandler) {
            sets++
            current = handler
        }
    }

    /** A previous uncaught-exception handler that records every call, in order. */
    class RecordingHandler : Thread.UncaughtExceptionHandler {
        val calls = mutableListOf<Pair<Thread, Throwable>>()

        override fun uncaughtException(thread: Thread, throwable: Throwable) {
            calls += thread to throwable
        }
    }

    /**
     * A card-like number: 16 digits from a non-issuer prefix that pass Luhn, computed at run time so no card-shaped
     * literal is committed.
     */
    fun cardLikeNumber(): String {
        val payload = "990000000000123"
        val sum = payload.reversed().mapIndexed { index, char ->
            val digit = char - '0'
            if (index % 2 == 0) (digit * 2).let { if (it > 9) it - 9 else it } else digit
        }.sum()
        return payload + (10 - sum % 10) % 10
    }

    /** Sequential UUID-shaped IDs: [uuid] of 1, 2, … */
    class SequentialIds : IdGenerator {
        private var next = 0
        override fun newId(): String = uuid(++next)
    }
}

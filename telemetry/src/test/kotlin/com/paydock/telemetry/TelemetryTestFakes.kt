package com.paydock.telemetry

/** Hand-written fakes shared by the core tests. No mocking library. */
internal object TelemetryTestFakes {

    const val INSTALLATION_ID = "installation-test-0001"

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
        source = TelemetryEvent.CrashSource.UNCAUGHT_HANDLER
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
    }

    /** A dispatcher that throws from every call. */
    object ThrowingDispatcher : TelemetryDispatcher {
        override fun post(task: () -> Unit): Boolean = error("synthetic dispatcher failure")
        override fun postDelayed(delayMillis: Long, task: () -> Unit): Boolean = error("synthetic dispatcher failure")
    }

    /** A queue that keeps records in memory, so exports can be observed. */
    class RecordingQueue : RecordQueue {
        val offered = mutableListOf<PendingRecord>()
        private val pending = ArrayDeque<PendingRecord>()

        override fun offer(record: PendingRecord) {
            offered += record
            pending.addLast(record)
        }

        override fun drain(max: Int): List<PendingRecord> {
            val drained = mutableListOf<PendingRecord>()
            while (drained.size < max && pending.isNotEmpty()) drained += pending.removeFirst()
            return drained
        }

        override fun size(): Int = pending.size
    }

    class RecordingStore : DurableStore {
        val saved = mutableListOf<PendingRecord>()

        override fun save(record: PendingRecord) {
            saved += record
        }

        override fun loadAll(): List<PendingRecord> = saved.toList()
        override fun remove(ids: Collection<String>) = Unit
        override fun prune(now: Long) = Unit
    }

    class RecordingExporter : Exporter {
        val batches = mutableListOf<ExportBatch>()

        override fun export(batch: ExportBatch): ExportResult {
            batches += batch
            return ExportResult.SUCCESS
        }
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

    /** Sequential IDs: "id-1", "id-2", … */
    class SequentialIds : IdGenerator {
        private var next = 0
        override fun newId(): String = "id-${++next}"
    }
}

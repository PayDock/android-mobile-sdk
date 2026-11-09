package com.paydock.telemetry

/**
 * Writes one crash record straight to the store, on the calling (crashing) thread (D17, R_02): kind EXCEPTION, source
 * UNCAUGHT_HANDLER, the thread's name, ErrorInfo.from(throwable), createdAt = clock.now(), id "<sessionId>-<next>".
 * Built by RecordFactory, filtered by Allowlist; saved only when it keeps session.id and log.record.uid. The connection
 * type is read once, UNKNOWN on failure. The build and the save run inside [diskWrites], so a debug app's StrictMode
 * disk policy cannot stop the write (review fix). Never posts to the worker, never sends; may throw (the handler
 * catches).
 *
 * Building one only stores references, so the active runtime can build it on any thread without I/O. The ID comes from
 * the session's shared [RecordSequence], so a crash record never collides with one the worker creates.
 */
internal class CrashRecorder(
    private val sessionId: String,
    private val sequence: RecordSequence,
    private val store: DurableStore,
    private val deviceContext: DeviceContextProvider,
    private val clock: Clock,
    private val diskWrites: DiskWritePermit = DiskWritePermit.STRICT_MODE,
) {
    fun record(thread: Thread, throwable: Throwable) {
        diskWrites.allow { write(thread, throwable) }
    }

    private fun write(thread: Thread, throwable: Throwable) {
        val event = TelemetryEvent.Crash(
            error = TelemetryEvent.ErrorInfo.from(throwable),
            source = TelemetryEvent.CrashSource.UNCAUGHT_HANDLER,
            kind = TelemetryEvent.CrashKind.EXCEPTION,
            threadName = thread.name
        )
        val pending = PendingRecord(
            id = "$sessionId-${sequence.next()}",
            sessionId = sessionId,
            event = event,
            createdAt = clock.now()
        )
        val connection = runCatching { deviceContext.networkConnectionType() }
            .getOrDefault(NetworkConnectionType.UNKNOWN)
        val record = Allowlist.filter(RecordFactory.create(pending, connection))
        if (record.sessionId != null && record.value(RECORD_UID) is AttributeValue.Str) store.save(record)
    }

    private companion object {
        const val RECORD_UID = "log.record.uid"
    }
}

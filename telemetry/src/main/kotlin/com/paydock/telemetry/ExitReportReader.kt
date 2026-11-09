package com.paydock.telemetry

import com.paydock.telemetry.TelemetryEvent.CrashKind
import com.paydock.telemetry.TelemetryEvent.CrashSource
import com.paydock.telemetry.TelemetryEvent.ErrorInfo
import java.io.InputStream

/**
 * The next-launch rules (spec § 7, § 7.1, R_04 to R_07). Worker only. [read] never throws: a failing source reads as
 * no exits. It keeps the exits newer than the marker (all of them when there is none), skips those older than
 * [TelemetryLimits.RECORD_MAX_AGE_MILLIS] at [now], and maps the rest, oldest first:
 *   ANR          -> Crash(AnrTraceReader.read(trace), EXIT_INFO, ANR, "ANR", occurredAt = timestamp)
 *   CRASH_NATIVE -> Crash(TombstoneReader.read(trace), EXIT_INFO, NATIVE, "CRASH_NATIVE", occurredAt = timestamp)
 *   the five kill reasons, foreground only -> Crash(null, EXIT_INFO, OS_KILL, reason.name, occurredAt = timestamp)
 *   OTHER, or a kill in the background -> nothing
 * A trace that is null, throws or does not parse gives error = null. newestTimestamp covers every exit newer than the
 * marker, skipped ones included. [markProcessed] stores it; the caller calls it only after the events are on disk.
 *
 * The marker lives under [markerKey], one key per process ([markerKeyFor]), because each process reads only its own
 * exits. A stored marker later than the `now` of [read] (a clock that was ahead) is read as no marker, and
 * [markProcessed] stores at most its `now`, so a clock running ahead can never hide later exits.
 *
 * With no exits the marker is never read, so [NONE]'s store is never touched.
 */
internal class ExitReportReader(
    private val source: ExitInfoSource,
    private val markers: KeyValueStore,
    private val markerKey: String = MARKER_KEY,
) {

    @Suppress("TooGenericExceptionCaught", "SwallowedException") // a failing source or marker reads as no exits
    fun read(now: Long): ExitReports = try {
        val exits = source.exits()
        if (exits.isEmpty()) {
            EMPTY
        } else {
            val marker = markers.getString(markerKey)?.toLongOrNull()?.takeIf { it <= now }
            val fresh = exits.filter { marker == null || it.timestamp > marker }.sortedBy { it.timestamp }
            ExitReports(
                events = fresh
                    .filter { now - it.timestamp <= TelemetryLimits.RECORD_MAX_AGE_MILLIS }
                    .mapNotNull(::crashOf),
                newestTimestamp = fresh.maxOfOrNull { it.timestamp }
            )
        }
    } catch (ignored: Throwable) {
        EMPTY
    }

    /** Stores [timestamp] as the marker, clamped to [now]. */
    fun markProcessed(timestamp: Long, now: Long) {
        markers.putString(markerKey, minOf(timestamp, now).toString())
    }

    private fun crashOf(exit: ProcessExit): TelemetryEvent.Crash? = when (exit.reason) {
        ExitReason.ANR -> crash(exit, CrashKind.ANR, traced(exit, AnrTraceReader::read))
        ExitReason.CRASH_NATIVE -> crash(exit, CrashKind.NATIVE, traced(exit, TombstoneReader::read))
        ExitReason.LOW_MEMORY,
        ExitReason.EXCESSIVE_RESOURCE_USAGE,
        ExitReason.SIGNALED,
        ExitReason.INITIALIZATION_FAILURE,
        ExitReason.DEPENDENCY_DIED,
        -> if (exit.foreground) crash(exit, CrashKind.OS_KILL, error = null) else null
        ExitReason.OTHER -> null
    }

    private fun crash(exit: ProcessExit, kind: CrashKind, error: ErrorInfo?) = TelemetryEvent.Crash(
        error = error,
        source = CrashSource.EXIT_INFO,
        kind = kind,
        reason = exit.reason.name,
        occurredAt = exit.timestamp
    )

    /** The exit's trace through [reader], closed after; null when there is none, or it throws or does not parse. */
    @Suppress("TooGenericExceptionCaught", "SwallowedException") // an unreadable trace is a crash without frames
    private fun traced(exit: ProcessExit, reader: (InputStream) -> ErrorInfo?): ErrorInfo? = try {
        exit.openTrace()?.use(reader)
    } catch (ignored: Throwable) {
        null
    }

    companion object {
        const val MARKER_KEY = "exit_info.last_processed" // in SharedPreferencesKeyValueStore.FILE_NAME

        /** "exit_info.last_processed.<processName>", the key of one process's marker. */
        fun markerKeyFor(processName: String): String = "$MARKER_KEY.$processName"

        private val EMPTY = ExitReports(events = emptyList(), newestTimestamp = null)

        /** No exits, ever: the reader of a runtime without an exit source. Its store is never touched. */
        val NONE: ExitReportReader = ExitReportReader(
            ExitInfoSource.NONE,
            object : KeyValueStore {
                override fun getString(key: String): String? = null
                override fun putString(key: String, value: String) = Unit
            }
        )
    }
}

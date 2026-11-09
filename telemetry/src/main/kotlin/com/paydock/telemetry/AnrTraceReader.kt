package com.paydock.telemetry

import java.io.FilterInputStream
import java.io.InputStream

/**
 * The main thread of an ANR dump: after the first line starting with `"main" `, every trimmed line starting with
 * `at ` up to the first blank line, the `at ` prefix removed and `(Native method)` written `(Native Method)`. At most
 * [TelemetryEvent.ErrorInfo.MAX_FRAMES] frames and [TelemetryLimits.TRACE_MAX_BYTES] bytes read. ErrorInfo(type = null,
 * frames), or null with no frame. Lines that are not frames (`- locked …`, `native: #00 pc …`, thread headers) are
 * never kept. Never throws.
 *
 * Reads lazily and stops at the main thread's end; the caller owns and closes [input].
 */
internal object AnrTraceReader {
    private const val MAIN_THREAD_PREFIX = "\"main\" "
    private const val FRAME_PREFIX = "at "
    private const val NATIVE_AS_DUMPED = "(Native method)"
    private const val NATIVE_AS_FRAMED = "(Native Method)"

    @Suppress("TooGenericExceptionCaught", "SwallowedException") // an unreadable dump is a crash without frames
    fun read(input: InputStream): TelemetryEvent.ErrorInfo? = try {
        val reader = Bounded(input, TelemetryLimits.TRACE_MAX_BYTES).bufferedReader(Charsets.UTF_8)
        val frames = generateSequence { reader.readLine() }
            .dropWhile { !it.startsWith(MAIN_THREAD_PREFIX) }
            .drop(1)
            .map { it.trim() }
            .takeWhile { it.isNotEmpty() }
            .filter { it.startsWith(FRAME_PREFIX) }
            .map { it.removePrefix(FRAME_PREFIX).replace(NATIVE_AS_DUMPED, NATIVE_AS_FRAMED) }
            .take(TelemetryEvent.ErrorInfo.MAX_FRAMES)
            .toList()
        if (frames.isEmpty()) null else TelemetryEvent.ErrorInfo(type = null, frames = frames)
    } catch (ignored: Throwable) {
        null
    }

    /** Ends after [remaining] bytes, as if the stream had ended there. */
    private class Bounded(input: InputStream, private var remaining: Int) : FilterInputStream(input) {
        override fun read(): Int {
            if (remaining <= 0) return -1
            return super.read().also { if (it >= 0) remaining-- }
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            if (remaining <= 0) return -1
            return super.read(buffer, offset, minOf(length, remaining)).also { if (it > 0) remaining -= it }
        }

        override fun skip(count: Long): Long = super.skip(minOf(count, remaining.toLong())).also {
            remaining -= it.toInt()
        }

        override fun available(): Int = minOf(super.available(), remaining)

        override fun markSupported(): Boolean = false
    }
}

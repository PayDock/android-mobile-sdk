package com.paydock.telemetry

import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * The crashing thread of a tombstone (AOSP debuggerd/proto/tombstone.proto), by a minimal protobuf wire reader over at
 * most [TelemetryLimits.TRACE_MAX_BYTES]: Tombstone.tid (6, varint) and threads (16, map entry: key 1 varint, value 2
 * Thread); of threads[tid], current_backtrace (4, repeated BacktraceFrame); of each frame rel_pc (1), function_name (4),
 * function_offset (5) and file_name (6). Every other field is skipped by wire type, abort_message (14), registers and
 * memory included; a group wire type or a truncated field ends the read. A frame is
 * `<function_name>+<function_offset>(<file name>)`, or `0x<rel_pc, lower-case hex>(<file name>)` without a function
 * name, where <file name> is the part after the last `/`. At most MAX_FRAMES frames. ErrorInfo(type = null, frames),
 * or null with no frame. Never throws. A read that ends early keeps the frames of a crashing-thread entry only when
 * that entry was read whole; an entry cut by the 2 MB limit or a truncation gives null.
 *
 * Skipped fields are stepped over by length, never decoded. The caller owns and closes [input].
 */
internal object TombstoneReader {
    private const val TOMBSTONE_TID = 6
    private const val TOMBSTONE_THREADS = 16
    private const val ENTRY_KEY = 1
    private const val ENTRY_VALUE = 2
    private const val THREAD_CURRENT_BACKTRACE = 4
    private const val FRAME_REL_PC = 1
    private const val FRAME_FUNCTION_NAME = 4
    private const val FRAME_FUNCTION_OFFSET = 5
    private const val FRAME_FILE_NAME = 6

    private const val WIRE_VARINT = 0
    private const val WIRE_FIXED64 = 1
    private const val WIRE_LENGTH_DELIMITED = 2
    private const val WIRE_FIXED32 = 5
    private const val FIXED64_BYTES = 8
    private const val FIXED32_BYTES = 4
    private const val TYPE_BITS = 3
    private const val TYPE_MASK = 7
    private const val VARINT_MAX_BYTES = 10
    private const val VARINT_PAYLOAD_BITS = 7
    private const val VARINT_PAYLOAD_MASK = 0x7F
    private const val VARINT_MORE_BIT = 0x80
    private const val BYTE_MASK = 0xFF
    private const val READ_BUFFER_BYTES = 8_192

    @Suppress("TooGenericExceptionCaught", "SwallowedException") // an unreadable tombstone is a crash without frames
    fun read(input: InputStream): TelemetryEvent.ErrorInfo? = try {
        val frames = crashingThread(Wire(readAtMost(input, TelemetryLimits.TRACE_MAX_BYTES)))
            ?.let(::backtrace)
            .orEmpty()
        if (frames.isEmpty()) null else TelemetryEvent.ErrorInfo(type = null, frames = frames)
    } catch (ignored: Throwable) {
        null
    }

    /** The Thread message of threads[tid], or null without a tid or without that thread. */
    private fun crashingThread(tombstone: Wire): Wire? {
        var tid: Long? = null
        val threads = HashMap<Long, Wire>()
        untilEnded {
            forEachField(tombstone) { field, type ->
                when {
                    field == TOMBSTONE_TID && type == WIRE_VARINT -> tid = tombstone.varint()
                    field == TOMBSTONE_THREADS && type == WIRE_LENGTH_DELIMITED ->
                        threadEntry(tombstone.message())?.let { (key, thread) -> threads[key] = thread }
                    else -> tombstone.skip(type)
                }
            }
        }
        return tid?.let { threads[it] }
    }

    /** One `map<uint32, Thread>` entry, or null without a key or a value. Throws [Malformed] when it is cut short. */
    private fun threadEntry(entry: Wire): Pair<Long, Wire>? {
        var key: Long? = null
        var value: Wire? = null
        forEachField(entry) { field, type ->
            when {
                field == ENTRY_KEY && type == WIRE_VARINT -> key = entry.varint()
                field == ENTRY_VALUE && type == WIRE_LENGTH_DELIMITED -> value = entry.message()
                else -> entry.skip(type)
            }
        }
        val entryKey = key ?: return null
        return value?.let { entryKey to it }
    }

    /** The current_backtrace of a Thread message, at most MAX_FRAMES frames; the frames before a malformed one. */
    private fun backtrace(thread: Wire): List<String> {
        val frames = mutableListOf<String>()
        untilEnded {
            forEachField(thread, until = { frames.size >= TelemetryEvent.ErrorInfo.MAX_FRAMES }) { field, type ->
                if (field == THREAD_CURRENT_BACKTRACE && type == WIRE_LENGTH_DELIMITED) {
                    frames += frame(thread.message())
                } else {
                    thread.skip(type)
                }
            }
        }
        return frames
    }

    /** One BacktraceFrame as text. Throws [Malformed] when it is cut short. */
    private fun frame(message: Wire): String {
        var relPc = 0L
        var functionName = ""
        var functionOffset = 0L
        var fileName = ""
        forEachField(message) { field, type ->
            when {
                field == FRAME_REL_PC && type == WIRE_VARINT -> relPc = message.varint()
                field == FRAME_FUNCTION_NAME && type == WIRE_LENGTH_DELIMITED -> functionName = message.string()
                field == FRAME_FUNCTION_OFFSET && type == WIRE_VARINT -> functionOffset = message.varint()
                field == FRAME_FILE_NAME && type == WIRE_LENGTH_DELIMITED -> fileName = message.string()
                else -> message.skip(type)
            }
        }
        val file = fileName.substringAfterLast('/')
        return if (functionName.isNotEmpty()) {
            "$functionName+${java.lang.Long.toUnsignedString(functionOffset)}($file)"
        } else {
            "0x${java.lang.Long.toHexString(relPc)}($file)"
        }
    }

    /**
     * Calls [onField] with each field's number and wire type until [wire] ends or [until] holds; [onField] reads or
     * skips the field's value. Throws [Malformed] at a group wire type or a truncated field.
     */
    private inline fun forEachField(
        wire: Wire,
        until: () -> Boolean = { false },
        onField: (field: Int, type: Int) -> Unit,
    ) {
        while (wire.hasMore() && !until()) {
            val tag = wire.varint()
            val field = (tag ushr TYPE_BITS).toInt()
            if (field <= 0) throw Malformed()
            onField(field, (tag and TYPE_MASK.toLong()).toInt())
        }
    }

    /** Runs [read]; a [Malformed] field ends it, keeping what [read] collected before it. */
    @Suppress("SwallowedException") // the end of the read, not an error
    private inline fun untilEnded(read: () -> Unit) {
        try {
            read()
        } catch (ignored: Malformed) {
            // The read ends here.
        }
    }

    private fun readAtMost(input: InputStream, max: Int): ByteArray {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(READ_BUFFER_BYTES)
        var total = 0
        var read = 0
        while (total < max && read >= 0) {
            read = input.read(buffer, 0, minOf(buffer.size, max - total))
            if (read > 0) {
                out.write(buffer, 0, read)
                total += read
            }
        }
        return out.toByteArray()
    }

    /** A group wire type, a truncated field or an over-long varint: the read ends. */
    private class Malformed : Exception() {
        companion object {
            private const val serialVersionUID: Long = 1L
        }
    }

    /** A cursor over bytes[start, end): one protobuf message. */
    private class Wire(private val bytes: ByteArray, private var position: Int = 0, private val end: Int = bytes.size) {

        fun hasMore(): Boolean = position < end

        fun varint(): Long {
            var result = 0L
            var shift = 0
            repeat(VARINT_MAX_BYTES) {
                if (position >= end) throw Malformed()
                val byte = bytes[position++].toInt() and BYTE_MASK
                result = result or ((byte and VARINT_PAYLOAD_MASK).toLong() shl shift)
                if (byte and VARINT_MORE_BIT == 0) return result
                shift += VARINT_PAYLOAD_BITS
            }
            throw Malformed()
        }

        /** A length-delimited field's value, as its own cursor. */
        fun message(): Wire {
            val length = length()
            val start = position
            position += length
            return Wire(bytes, start, start + length)
        }

        fun string(): String {
            val length = length()
            val start = position
            position += length
            return String(bytes, start, length, Charsets.UTF_8)
        }

        /** Steps over a field's value by its wire type; never decodes it. */
        fun skip(type: Int) {
            when (type) {
                WIRE_VARINT -> varint()
                WIRE_FIXED64 -> advance(FIXED64_BYTES)
                WIRE_LENGTH_DELIMITED -> advance(length())
                WIRE_FIXED32 -> advance(FIXED32_BYTES)
                else -> throw Malformed() // 3 and 4 are the deprecated groups; 6 and 7 do not exist
            }
        }

        private fun length(): Int {
            val length = varint()
            if (length < 0 || length > end - position) throw Malformed()
            return length.toInt()
        }

        private fun advance(count: Int) {
            if (count > end - position) throw Malformed()
            position += count
        }
    }
}

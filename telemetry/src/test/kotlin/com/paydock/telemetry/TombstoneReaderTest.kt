package com.paydock.telemetry

import com.paydock.telemetry.TelemetryTestFakes.cardLikeNumber
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Unit tests for [TombstoneReader], the crashing thread of a tombstone (spec § 7.1, R_06). The bytes are built by a
 * tiny test-only protobuf writer in the layout of AOSP's `tombstone.proto`, so no platform file is needed.
 */
internal class TombstoneReaderTest {

    /** A test-only protobuf writer: just the wire types the cases need. */
    private class Proto {
        private val out = ByteArrayOutputStream()

        fun varint(field: Int, value: Long) = apply {
            tag(field, WIRE_VARINT)
            raw(value)
        }

        fun bytes(field: Int, value: ByteArray) = apply {
            tag(field, WIRE_LENGTH_DELIMITED)
            raw(value.size.toLong())
            out.write(value)
        }

        fun string(field: Int, value: String) = bytes(field, value.toByteArray(Charsets.UTF_8))

        fun message(field: Int, value: Proto) = bytes(field, value.toByteArray())

        fun fixed64(field: Int, value: Long) = apply {
            tag(field, WIRE_FIXED64)
            repeat(8) { out.write((value ushr (8 * it)).toInt() and 0xFF) }
        }

        fun fixed32(field: Int, value: Int) = apply {
            tag(field, WIRE_FIXED32)
            repeat(4) { out.write((value ushr (8 * it)) and 0xFF) }
        }

        /** A start-group tag, the deprecated wire type 3, followed by an end-group tag. */
        fun group(field: Int) = apply {
            tag(field, WIRE_START_GROUP)
            tag(field, WIRE_END_GROUP)
        }

        fun toByteArray(): ByteArray = out.toByteArray()

        private fun tag(field: Int, type: Int) = raw((field.toLong() shl 3) or type.toLong())

        private fun raw(value: Long) {
            var rest = value
            while (rest and 0x7FL.inv() != 0L) {
                out.write(((rest and 0x7F) or 0x80).toInt())
                rest = rest ushr 7
            }
            out.write(rest.toInt())
        }
    }

    private fun frame(
        relPc: Long = 0x1a2bL,
        functionName: String? = null,
        functionOffset: Long = 0,
        fileName: String = "/data/app/~~random==/com.example.merchant-abc==/lib/arm64/libmerchant.so",
    ) = Proto().apply {
        varint(1, relPc) // rel_pc
        varint(2, relPc + 0x7000_0000L) // pc: skipped
        varint(3, 0x7fff_0000L) // sp: skipped
        functionName?.let { string(4, it) }
        varint(5, functionOffset)
        string(6, fileName)
        varint(7, 0) // file_map_offset: skipped
        string(8, "0123456789abcdef") // build_id: skipped
    }

    private fun thread(id: Long, name: String, vararg frames: Proto) = Proto().apply {
        varint(1, id) // id
        string(2, name) // name
        message(3, Proto().string(1, "x0").varint(2, 0x1234)) // registers: skipped
        frames.forEach { message(4, it) } // current_backtrace
        message(5, Proto().string(1, "[anon]").varint(2, 0x1000)) // memory_dump: skipped
    }

    private fun entry(key: Long, thread: Proto) = Proto().varint(1, key).message(2, thread)

    private fun tombstone(tid: Long, vararg threads: Pair<Long, Proto>, abortMessage: String? = null) = Proto().apply {
        varint(1, 3) // arch: skipped
        string(2, "synthetic/fingerprint") // build_fingerprint: skipped
        varint(5, 4321) // pid: skipped
        varint(6, tid) // tid
        abortMessage?.let { string(14, it) } // abort_message: skipped unread
        threads.forEach { (key, thread) -> message(16, entry(key, thread)) }
    }

    private fun read(bytes: ByteArray) = TombstoneReader.read(ByteArrayInputStream(bytes))

    private fun read(proto: Proto) = read(proto.toByteArray())

    @Test
    fun read_keepsTheThreadOfTheTid_amongTwoThreads() {
        val other = thread(4321, "main", frame(functionName = "main_loop", functionOffset = 8))
        val crashing = thread(4400, "worker", frame(functionName = "crash_here", functionOffset = 24))

        val error = read(tombstone(4400, 4321L to other, 4400L to crashing))

        assertEquals(TelemetryEvent.ErrorInfo(type = null, frames = listOf("crash_here+24(libmerchant.so)")), error)
    }

    @Test
    fun read_aTidBeforeOrAfterTheThreads_findsTheSameThread() {
        val crashing = thread(7, "worker", frame(functionName = "f", functionOffset = 1))
        val tidLast = Proto().message(16, entry(7, crashing)).varint(6, 7)

        assertEquals(listOf("f+1(libmerchant.so)"), read(tidLast)?.frames)
    }

    @Test
    fun read_framesWithAndWithoutAFunctionName() {
        val crashing = thread(
            1,
            "main",
            frame(relPc = 0x4f2c0L, functionName = "abort", functionOffset = 164, fileName = "/apex/bionic/libc.so"),
            frame(relPc = 0xABCDEFL, fileName = "/data/app/~~x==/com.example-y==/lib/arm64/libmerchant.so"),
            frame(relPc = 0x10L, functionName = "", fileName = "libbare.so")
        )

        val error = read(tombstone(1, 1L to crashing))

        assertEquals(listOf("abort+164(libc.so)", "0xabcdef(libmerchant.so)", "0x10(libbare.so)"), error?.frames)
        assertNull(error?.type)
    }

    @Test
    fun read_reducesAFilePathToItsName() {
        val path = "/data/app/~~Yz0rAnd0m==/com.example.merchant-Q1w2e3==/lib/arm64/libpaydock.so"
        val crashing = thread(1, "main", frame(functionName = "f", functionOffset = 2, fileName = path))

        val frame = read(tombstone(1, 1L to crashing))!!.frames.single()

        assertEquals("f+2(libpaydock.so)", frame)
        assertFalse(frame.contains("com.example.merchant"))
        assertFalse(frame.contains("/"))
    }

    /** Principle 11: the abort message is skipped unread, so a card-like number in it reaches no frame. */
    @Test
    fun read_anAbortMessageWithACardLikeNumber_appearsNowhereInTheResult() {
        val cardNumber = cardLikeNumber()
        val crashing = thread(1, "main", frame(functionName = "f", functionOffset = 2))

        val error = read(tombstone(1, 1L to crashing, abortMessage = "payment failed for $cardNumber"))

        assertEquals(listOf("f+2(libmerchant.so)"), error?.frames)
        assertFalse(error.toString().contains(cardNumber))
    }

    @Test
    fun read_skipsAnUnknownFieldOfEveryWireType_atEveryLevel() {
        val unknownFields: Proto.() -> Unit = {
            varint(90, 300)
            fixed64(91, -1L)
            string(92, "unknown")
            fixed32(93, 0x7F7F7F7F)
        }
        val crashing = Proto().apply {
            unknownFields()
            message(4, frame(functionName = "f", functionOffset = 3).apply(unknownFields))
            unknownFields()
        }
        val tombstone = Proto().apply {
            unknownFields()
            varint(6, 1)
            message(16, Proto().apply(unknownFields).varint(1, 1).message(2, crashing))
            unknownFields()
        }

        assertEquals(listOf("f+3(libmerchant.so)"), read(tombstone)?.frames)
    }

    @Test
    fun read_aTruncatedBuffer_keepsWhatWasCompleteBeforeTheCut() {
        val crashing = thread(1, "main", frame(functionName = "f", functionOffset = 3))
        val whole = tombstone(1, 1L to crashing).toByteArray()

        assertEquals(listOf("f+3(libmerchant.so)"), read(whole)?.frames)
        assertNull(read(whole.copyOf(whole.size - 5)))
        assertNull(read(whole.copyOf(3)))
        assertNull(read(ByteArray(0)))
    }

    @Test
    fun read_aTruncatedFrame_endsTheBacktraceAfterTheFramesBeforeIt() {
        val first = frame(functionName = "first", functionOffset = 1).toByteArray()
        val cut = Proto().varint(1, 5).toByteArray() + byteArrayOf(0x22, 0x10) // function_name of 16 bytes, none follow
        val crashing = Proto().bytes(4, first).bytes(4, cut).bytes(4, first)

        assertEquals(listOf("first+1(libmerchant.so)"), read(tombstone(1, 1L to crashing))?.frames)
    }

    @Test
    fun read_aGroupWireType_endsTheRead() {
        val crashing = thread(1, "main", frame(functionName = "f", functionOffset = 3))
        val groupFirst = Proto().varint(6, 1).group(50).message(16, entry(1, crashing))
        val groupAfter = Proto().varint(6, 1).message(16, entry(1, crashing)).group(50)

        assertNull(read(groupFirst))
        assertEquals(listOf("f+3(libmerchant.so)"), read(groupAfter)?.frames)
    }

    @Test
    fun read_keepsAtMostMaxFrames() {
        val frames = (1..TelemetryEvent.ErrorInfo.MAX_FRAMES + 6).map {
            frame(functionName = "f$it", functionOffset = it.toLong())
        }
        val crashing = thread(1, "main", *frames.toTypedArray())

        val error = read(tombstone(1, 1L to crashing))!!

        assertEquals(TelemetryEvent.ErrorInfo.MAX_FRAMES, error.frames.size)
        assertEquals("f1+1(libmerchant.so)", error.frames.first())
        assertEquals("f64+64(libmerchant.so)", error.frames.last())
    }

    @Test
    fun read_withoutATidOrItsThread_isNull() {
        val crashing = thread(1, "main", frame(functionName = "f", functionOffset = 3))

        assertNull(read(Proto().message(16, entry(1, crashing))))
        assertNull(read(tombstone(2, 1L to crashing)))
        assertNull(read(tombstone(1, 1L to thread(1, "main"))))
    }

    @Test
    fun read_bytesThatAreNotATombstone_isNull() {
        assertNull(read("not a tombstone at all".toByteArray()))
        assertNull(read(ByteArray(64) { 0xFF.toByte() }))
    }

    /** A managed frame in a native crash makes the record SDK code through the factory's existing rule. */
    @Test
    fun aManagedPaydockFrame_makesTheCrashRecordInSdk() {
        val crashing = thread(1, "main", frame(functionName = "com.paydock.feature.X.y", functionOffset = 12))
        val event = TelemetryEvent.Crash(
            error = read(tombstone(1, 1L to crashing)),
            source = TelemetryEvent.CrashSource.EXIT_INFO,
            kind = TelemetryEvent.CrashKind.NATIVE,
            reason = "CRASH_NATIVE",
            occurredAt = TelemetryTestFakes.T0
        )

        val record = TelemetryTestFakes.record(1, event = event)

        assertEquals(AttributeValue.Bool(true), record.value("paydock.exception.in_sdk"))
        assertTrue(event.error!!.frames.single().startsWith("com.paydock.feature.X.y+12("))
    }

    private companion object {
        const val WIRE_VARINT = 0
        const val WIRE_FIXED64 = 1
        const val WIRE_LENGTH_DELIMITED = 2
        const val WIRE_START_GROUP = 3
        const val WIRE_END_GROUP = 4
        const val WIRE_FIXED32 = 5
    }
}

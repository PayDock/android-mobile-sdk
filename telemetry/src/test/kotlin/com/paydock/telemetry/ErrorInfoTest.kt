package com.paydock.telemetry

import com.paydock.telemetry.TelemetryEvent.ErrorInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [ErrorInfo.from] (R_10).
 *
 * An exception enters an event only as an [ErrorInfo]: its type, its frames and its cause chain, never a message,
 * toString() output or suppressed exceptions, so data in memory cannot ride an exception into a record.
 */
internal class ErrorInfoTest {

    private class OuterFailure(message: String) : IllegalStateException(message)
    private class CauseFailure(message: String) : IllegalArgumentException(message)
    private class SuppressedFailure(message: String) : UnsupportedOperationException(message)
    private class ChainFailure : RuntimeException("chain link")

    /** A throwable whose stack trace cannot be read. */
    private class UnreadableStackFailure : RuntimeException("unreadable") {
        override fun getStackTrace(): Array<StackTraceElement> = error("no stack trace")
    }

    @Test
    fun from_keepsNoMessageOfTheThrowableItsCauseOrItsSuppressed() {
        val throwable = OuterFailure(OUTER_SENTINEL).apply {
            initCause(CauseFailure(CAUSE_SENTINEL))
            addSuppressed(SuppressedFailure(SUPPRESSED_SENTINEL))
        }

        val info = ErrorInfo.from(throwable)

        val text = info.toString()
        listOf(OUTER_SENTINEL, CAUSE_SENTINEL, SUPPRESSED_SENTINEL).forEach { sentinel ->
            assertFalse("ErrorInfo must not contain $sentinel: $text", text.contains(sentinel))
        }
        assertFalse(text.contains(SuppressedFailure::class.java.name))
        assertEquals(OuterFailure::class.java.name, info.type)
        assertEquals(listOf(CauseFailure::class.java.name), info.causeTypes)
        assertEquals(CauseFailure::class.java.name, info.cause?.type)
        assertNull(info.cause?.cause)
    }

    @Test
    fun from_buildsFramesFromTheStackTraceFields() {
        val throwable = OuterFailure(OUTER_SENTINEL)

        val info = ErrorInfo.from(throwable)

        val top = throwable.stackTrace.first()
        assertEquals(minOf(throwable.stackTrace.size, ErrorInfo.MAX_FRAMES), info.frames.size)
        assertEquals("${top.className}.${top.methodName}(${top.fileName}:${top.lineNumber})", info.frames.first())
        assertEquals(ErrorInfoTest::class.java.name, top.className)
        assertEquals("ErrorInfoTest.kt", top.fileName)
    }

    @Test
    fun from_capsFramesAtMaxFrames() {
        val throwable = OuterFailure(OUTER_SENTINEL).apply {
            stackTrace = Array(ErrorInfo.MAX_FRAMES + 10) { index ->
                StackTraceElement("com.example.Caller", "call$index", "Caller.kt", index + 1)
            }
        }

        val info = ErrorInfo.from(throwable)

        assertEquals(ErrorInfo.MAX_FRAMES, info.frames.size)
        assertEquals("com.example.Caller.call0(Caller.kt:1)", info.frames.first())
        assertEquals(
            "com.example.Caller.call${ErrorInfo.MAX_FRAMES - 1}(Caller.kt:${ErrorInfo.MAX_FRAMES})",
            info.frames.last()
        )
    }

    @Test
    fun from_capsTheCauseChainAtMaxCauses() {
        var throwable: Throwable = ChainFailure()
        repeat(ErrorInfo.MAX_CAUSES + 5) { throwable = ChainFailure().initCause(throwable) }

        val info = ErrorInfo.from(throwable)

        assertEquals(ErrorInfo.MAX_CAUSES, info.causeTypes.size)
        assertTrue(info.causeTypes.all { it == ChainFailure::class.java.name })
    }

    @Test
    fun from_terminatesOnACyclicCauseChain() {
        val first = OuterFailure(OUTER_SENTINEL)
        val second = CauseFailure(CAUSE_SENTINEL)
        first.initCause(second)
        second.initCause(first)

        val info = ErrorInfo.from(first)

        assertEquals(OuterFailure::class.java.name, info.type)
        assertEquals(listOf(CauseFailure::class.java.name), info.causeTypes)
    }

    @Test
    fun from_readsUnknownSourceForANullFileNameAndNativeMethodForANativeFrame() {
        val throwable = OuterFailure(OUTER_SENTINEL).apply {
            stackTrace = arrayOf(
                StackTraceElement("com.example.Caller", "noFile", null, 12),
                StackTraceElement("com.example.Caller", "native", "Caller.kt", NATIVE_LINE_NUMBER)
            )
        }

        val info = ErrorInfo.from(throwable)

        assertEquals(
            listOf("com.example.Caller.noFile(Unknown Source:12)", "com.example.Caller.native(Native Method)"),
            info.frames
        )
    }

    @Test
    fun from_fallsBackToTheTypeAloneWhenTheStackTraceCannotBeRead() {
        val throwable = UnreadableStackFailure()

        val info = ErrorInfo.from(throwable)

        assertEquals(ErrorInfo(type = UnreadableStackFailure::class.java.name, frames = emptyList()), info)
    }

    private companion object {
        const val OUTER_SENTINEL = "sentinel-outer-7f3a"
        const val CAUSE_SENTINEL = "sentinel-cause-91c2"
        const val SUPPRESSED_SENTINEL = "sentinel-suppressed-4be8"

        /** [StackTraceElement.isNativeMethod] is true for line number -2. */
        const val NATIVE_LINE_NUMBER = -2
    }
}

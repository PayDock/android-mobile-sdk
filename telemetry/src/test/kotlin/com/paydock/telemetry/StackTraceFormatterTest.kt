package com.paydock.telemetry

import com.paydock.telemetry.TelemetryEvent.ErrorInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [StackTraceFormatter] (spec § 5.5): line format, frame and cause limits, and the 16 KB cut. */
internal class StackTraceFormatterTest {

    private fun frames(count: Int, prefix: String = "com.example.Caller.call") =
        (0 until count).map { "$prefix$it(Caller.kt:${it + 1})" }

    @Test
    fun format_writesTheTypeLineFramesAndCausedByLines() {
        val error = ErrorInfo(
            type = "java.lang.IllegalStateException",
            frames = listOf("a.B.c(B.kt:1)", "d.E.f(E.kt:2)"),
            cause = ErrorInfo(type = "java.lang.NullPointerException", frames = listOf("g.H.i(H.kt:3)"))
        )

        assertEquals(
            "java.lang.IllegalStateException\n\tat a.B.c(B.kt:1)\n\tat d.E.f(E.kt:2)\n" +
                "Caused by: java.lang.NullPointerException\n\tat g.H.i(H.kt:3)",
            StackTraceFormatter.format(error)
        )
    }

    @Test
    fun format_writesNoTypeLineWithoutAType() {
        val error = ErrorInfo(type = null, frames = listOf("android.os.MessageQueue.nativePollOnce(Native Method)"))

        assertEquals("\tat android.os.MessageQueue.nativePollOnce(Native Method)", StackTraceFormatter.format(error))
    }

    @Test
    fun format_keepsAtMost64FramesPerException() {
        val error = ErrorInfo(
            type = "Outer",
            frames = frames(StackTraceFormatter.MAX_FRAMES + 5),
            cause = ErrorInfo(type = "Inner", frames = frames(StackTraceFormatter.MAX_FRAMES + 5, "com.example.In.x"))
        )

        val lines = StackTraceFormatter.format(error).split("\n")

        assertEquals(2 + 2 * StackTraceFormatter.MAX_FRAMES, lines.size)
        assertEquals("\tat com.example.Caller.call63(Caller.kt:64)", lines[StackTraceFormatter.MAX_FRAMES])
        assertEquals("Caused by: Inner", lines[StackTraceFormatter.MAX_FRAMES + 1])
    }

    @Test
    fun format_keepsAtMost8Causes() {
        val error = (StackTraceFormatter.MAX_CAUSES + 3 downTo 0).fold(null as ErrorInfo?) { inner, depth ->
            ErrorInfo(type = "Cause$depth", frames = emptyList(), cause = inner)
        }!!

        val lines = StackTraceFormatter.format(error).split("\n")

        assertEquals(1 + StackTraceFormatter.MAX_CAUSES, lines.size)
        assertEquals("Cause0", lines.first())
        assertEquals("Caused by: Cause${StackTraceFormatter.MAX_CAUSES}", lines.last())
    }

    @Test
    fun format_cutsAtTheLastWholeLineWithin16KiB() {
        val longFrame = "com.example." + "x".repeat(990) // 1 002 bytes, 1 007 with "\tat " and "\n"
        val error = ErrorInfo(
            type = "Outer",
            frames = List(StackTraceFormatter.MAX_FRAMES) { longFrame },
            cause = ErrorInfo(type = "Inner", frames = List(10) { longFrame })
        )

        val text = StackTraceFormatter.format(error)

        val bytes = text.toByteArray(Charsets.UTF_8).size
        assertTrue("$bytes bytes", bytes <= StackTraceFormatter.MAX_BYTES)
        assertTrue("cut too early: $bytes bytes", bytes > StackTraceFormatter.MAX_BYTES - 1_007)
        val lines = text.split("\n")
        assertEquals("Outer", lines.first())
        assertTrue(lines.drop(1).all { it == "\tat $longFrame" })
    }

    @Test
    fun format_countsUtf8BytesNotCharacters() {
        val wide = "com.example.Ü" + "é".repeat(4_000) // 2 bytes per accented character
        val error = ErrorInfo(type = "Outer", frames = List(3) { wide })

        val text = StackTraceFormatter.format(error)

        assertTrue(text.toByteArray(Charsets.UTF_8).size <= StackTraceFormatter.MAX_BYTES)
        assertEquals(3, text.split("\n").size)
    }

    @Test
    fun format_isExactlyTheLimitWhenTheLinesFit() {
        // "Outer" (5 bytes), then lines of "\n\tat " (5 bytes) + frame, sized so the total is exactly MAX_BYTES.
        val frame = "f".repeat((StackTraceFormatter.MAX_BYTES - 5) / 2 - 5)
        val rest = StackTraceFormatter.MAX_BYTES - 5 - 2 * (5 + frame.length)
        val error = ErrorInfo(type = "Outer", frames = listOf(frame, frame + "g".repeat(rest), "next"))

        val text = StackTraceFormatter.format(error)

        assertEquals(StackTraceFormatter.MAX_BYTES, text.toByteArray(Charsets.UTF_8).size)
        assertEquals(3, text.split("\n").size)
    }
}

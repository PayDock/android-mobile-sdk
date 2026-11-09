package com.paydock.telemetry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Unit tests for [AnrTraceReader], the main thread of an ANR dump (spec § 7.1, R_05): synthetic dumps in the layout
 * of the OS's `traces.txt`, with two threads, `- locked` lines, native lines and `(Native method)` frames.
 */
internal class AnrTraceReaderTest {

    private val dump = """
        ----- pid 4321 at 2026-11-03 03:58:41.000000000+0000 -----
        Cmd line: com.example.merchant

        "Signal Catcher" daemon prio=10 tid=2 Runnable
          | group="system" sCount=0 ucsCount=0 flags=0 obj=0x12c80000 self=0x7b8e0c3c00
          native: #00 pc 000000000004e1dc  /apex/com.android.runtime/lib64/bionic/libc.so (syscall+28)
          at java.lang.Object.wait(Native method)
          at com.example.merchant.Background.run(Background.kt:12)

        "main" prio=5 tid=1 Native
          | group="main" sCount=1 ucsCount=0 flags=1 obj=0x72c0a3b8 self=0x7b8e0c1000
          | sysTid=4321 nice=-10 cgrp=top-app sched=0/0 handle=0x7c9f6d34f8
          native: #00 pc 00000000000a2f8c  /apex/com.android.runtime/lib64/bionic/libc.so (__epoll_pwait+12)
          native: #01 pc 0000000000017d74  /system/lib64/libutils.so (android::Looper::pollInner(int)+180)
          at android.os.MessageQueue.nativePollOnce(Native method)
          - locked <0x0f3b2a1c> (a java.lang.Object)
          at com.paydock.feature.card.presentation.ui.CardDetailsWidgetKt.CardDetailsWidget(CardDetailsWidget.kt:77)
          - waiting to lock <0x0a1b2c3d> (a java.lang.Object) held by thread 2
          at android.os.Looper.loop(Looper.java:288)

        "Binder:4321_1" prio=5 tid=3 Native
          at android.os.Binder.execTransact(Binder.java:1234)
    """.trimIndent()

    private fun read(text: String) = AnrTraceReader.read(ByteArrayInputStream(text.toByteArray(Charsets.UTF_8)))

    @Test
    fun read_keepsOnlyTheMainThreadsFrames_inOrder_withNativeMethodNormalised() {
        val error = read(dump)

        assertEquals(
            TelemetryEvent.ErrorInfo(
                type = null,
                frames = listOf(
                    "android.os.MessageQueue.nativePollOnce(Native Method)",
                    "com.paydock.feature.card.presentation.ui.CardDetailsWidgetKt" +
                        ".CardDetailsWidget(CardDetailsWidget.kt:77)",
                    "android.os.Looper.loop(Looper.java:288)"
                )
            ),
            error
        )
    }

    @Test
    fun read_aDumpWithoutAMainThread_isNull() {
        assertNull(read(dump.replace("\"main\" prio", "\"renamed\" prio")))
    }

    @Test
    fun read_aMainThreadWithoutFrames_isNull() {
        assertNull(read("\"main\" prio=5 tid=1 Native\n  native: #00 pc 00000000000a2f8c  /system/lib64/libc.so\n\n"))
    }

    @Test
    fun read_anEmptyOrUnrelatedInput_isNull() {
        assertNull(read(""))
        assertNull(read("not an ANR dump\nat a.B.c(B.kt:1)\n"))
    }

    @Test
    fun read_aMainThreadAtTheEndOfTheDump_readsToTheEnd() {
        val error = read("\"main\" prio=5 tid=1 Native\n  at a.B.c(B.kt:1)\n  at a.B.d(B.kt:2)")

        assertEquals(listOf("a.B.c(B.kt:1)", "a.B.d(B.kt:2)"), error?.frames)
    }

    @Test
    fun read_keepsAtMostMaxFrames() {
        val frames = (1..TelemetryEvent.ErrorInfo.MAX_FRAMES + 10).joinToString("\n") { "  at a.B.m$it(B.kt:$it)" }

        val error = read("\"main\" prio=5 tid=1 Native\n$frames\n")

        assertEquals(TelemetryEvent.ErrorInfo.MAX_FRAMES, error?.frames?.size)
        assertEquals("a.B.m1(B.kt:1)", error?.frames?.first())
    }

    @Test
    fun read_stopsAfterTheTraceByteLimit() {
        val header = "\"main\" prio=5 tid=1 Native\n"
        val padding = "  | " + "x".repeat(TelemetryLimits.TRACE_MAX_BYTES) + "\n"

        assertNull(read(header + padding + "  at a.B.c(B.kt:1)\n"))
    }

    @Test
    fun read_aStreamThatThrows_isNull() {
        val failing = object : InputStream() {
            override fun read(): Int = throw IOException("synthetic read failure")
        }

        assertNull(AnrTraceReader.read(failing))
    }
}

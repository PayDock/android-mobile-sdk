package com.paydock.telemetry

import android.content.ContextWrapper
import com.paydock.telemetry.TelemetryEvent.CrashKind
import com.paydock.telemetry.TelemetryEvent.CrashSource
import com.paydock.telemetry.TelemetryTestFakes.MapKeyValueStore
import com.paydock.telemetry.TelemetryTestFakes.T0
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Unit tests for [ExitReportReader], the next-launch rules (spec § 7, § 7.1, R_04 to R_07): a fake exit source and an
 * in-memory key-value store. [AndroidExitInfoSource] itself is not unit-tested (no Robolectric); only its version
 * branch is, through its internal `sdkInt` overload.
 */
internal class ExitReportReaderTest {

    private val markers = MapKeyValueStore()
    private val now = T0

    private val anrDump = "\"main\" prio=5 tid=1 Native\n" +
        "  at android.os.MessageQueue.nativePollOnce(Native method)\n" +
        "  at $SDK_FRAME\n" +
        "\n"

    /** A tombstone whose crashing thread (tid 1) has one frame `f+2(libmerchant.so)`, in protobuf wire bytes. */
    private val tombstone: ByteArray = run {
        val fileName = "/data/app/lib/libmerchant.so".toByteArray()
        val frame = byteArrayOf(0x08, 0x10, 0x22, 0x01, 'f'.code.toByte(), 0x28, 0x02, 0x32, fileName.size.toByte()) +
            fileName
        val thread = byteArrayOf(0x08, 0x01, 0x22, frame.size.toByte()) + frame
        val entry = byteArrayOf(0x08, 0x01, 0x12, thread.size.toByte()) + thread
        byteArrayOf(0x30, 0x01, 0x82.toByte(), 0x01, entry.size.toByte()) + entry
    }

    private fun exit(
        reason: ExitReason,
        timestamp: Long,
        foreground: Boolean = true,
        trace: () -> InputStream? = { null },
    ) = ProcessExit(reason, foreground, timestamp, trace)

    private fun reader(vararg exits: ProcessExit) = ExitReportReader(ExitInfoSource { exits.toList() }, markers)

    private fun text(value: String): () -> InputStream = { ByteArrayInputStream(value.toByteArray()) }

    @Test
    fun anAnr_isAnExitInfoAnrCrash_withTheMainThreadsFrames_atItsExitTime() {
        val found = reader(exit(ExitReason.ANR, now - 1_000, trace = text(anrDump))).read(now)

        val crash = found.events.single()
        assertEquals(CrashSource.EXIT_INFO, crash.source)
        assertEquals(CrashKind.ANR, crash.kind)
        assertEquals("ANR", crash.reason)
        assertEquals(now - 1_000, crash.occurredAt)
        assertNull(crash.threadName)
        assertNull(crash.error!!.type)
        assertEquals(
            listOf(
                "android.os.MessageQueue.nativePollOnce(Native Method)",
                SDK_FRAME
            ),
            crash.error!!.frames
        )
        assertEquals(now - 1_000, found.newestTimestamp)
    }

    @Test
    fun aNativeCrash_isAnExitInfoNativeCrash_withTheTombstonesFrames() {
        val found = reader(exit(ExitReason.CRASH_NATIVE, now - 1_000) { ByteArrayInputStream(tombstone) }).read(now)

        val crash = found.events.single()
        assertEquals(CrashSource.EXIT_INFO, crash.source)
        assertEquals(CrashKind.NATIVE, crash.kind)
        assertEquals("CRASH_NATIVE", crash.reason)
        assertEquals(now - 1_000, crash.occurredAt)
        assertEquals(TelemetryEvent.ErrorInfo(type = null, frames = listOf("f+2(libmerchant.so)")), crash.error)
    }

    @Test
    fun eachKillReason_inTheForeground_isAnOsKill_withoutAnError() {
        val kills = listOf(
            ExitReason.LOW_MEMORY,
            ExitReason.EXCESSIVE_RESOURCE_USAGE,
            ExitReason.SIGNALED,
            ExitReason.INITIALIZATION_FAILURE,
            ExitReason.DEPENDENCY_DIED
        )
        val exits = kills.mapIndexed { index, reason -> exit(reason, now - 10_000 + index) }

        val found = reader(*exits.toTypedArray()).read(now)

        assertEquals(kills.map { it.name }, found.events.map { it.reason })
        found.events.forEachIndexed { index, crash ->
            assertEquals(CrashSource.EXIT_INFO, crash.source)
            assertEquals(CrashKind.OS_KILL, crash.kind)
            assertNull(crash.error)
            assertEquals(now - 10_000 + index, crash.occurredAt)
        }
    }

    @Test
    fun aKillInTheBackground_isNothing_butMovesTheMarker() {
        val exits = listOf(ExitReason.LOW_MEMORY, ExitReason.SIGNALED, ExitReason.DEPENDENCY_DIED)
            .mapIndexed { index, reason -> exit(reason, now - 3_000 + index, foreground = false) }

        val found = reader(*exits.toTypedArray()).read(now)

        assertTrue(found.events.isEmpty())
        assertEquals(now - 3_000 + 2, found.newestTimestamp)
    }

    @Test
    fun anAnrOrANativeCrashInTheBackground_isStillReported() {
        val found = reader(
            exit(ExitReason.ANR, now - 2_000, foreground = false),
            exit(ExitReason.CRASH_NATIVE, now - 1_000, foreground = false)
        ).read(now)

        assertEquals(listOf(CrashKind.ANR, CrashKind.NATIVE), found.events.map { it.kind })
    }

    @Test
    fun other_isIgnored_butMovesTheMarker() {
        val found = reader(exit(ExitReason.OTHER, now - 500), exit(ExitReason.OTHER, now - 400, foreground = false))
            .read(now)

        assertTrue(found.events.isEmpty())
        assertEquals(now - 400, found.newestTimestamp)
    }

    @Test
    fun theMarker_skipsOlderExits_andTheNewestMovesToTheNewestExit_ignoredOnesIncluded() {
        markers.values[ExitReportReader.MARKER_KEY] = (now - 5_000).toString()
        val reader = reader(
            exit(ExitReason.ANR, now - 6_000),
            exit(ExitReason.LOW_MEMORY, now - 5_000),
            exit(ExitReason.SIGNALED, now - 4_000),
            exit(ExitReason.OTHER, now - 1_000)
        )

        val found = reader.read(now)

        assertEquals(listOf(now - 4_000), found.events.map { it.occurredAt })
        assertEquals(now - 1_000, found.newestTimestamp)
        assertEquals(0, markers.writes)
    }

    @Test
    fun markProcessed_storesTheTimestampAsADecimalString_andTheNextReadSkipsUpToIt() {
        val exits = mutableListOf(exit(ExitReason.ANR, now - 2_000))
        val reader = ExitReportReader(ExitInfoSource { exits.toList() }, markers)

        reader.markProcessed(reader.read(now).newestTimestamp!!, now)
        assertEquals((now - 2_000).toString(), markers.values[ExitReportReader.MARKER_KEY])

        exits += exit(ExitReason.CRASH_NATIVE, now - 1_000)
        val next = reader.read(now)
        assertEquals(listOf(CrashKind.NATIVE), next.events.map { it.kind })
        assertEquals(now - 1_000, next.newestTimestamp)
    }

    @Test
    fun noMarker_readsAllExits() {
        val found = reader(exit(ExitReason.ANR, now - 9_000), exit(ExitReason.LOW_MEMORY, now - 1)).read(now)

        assertEquals(2, found.events.size)
        assertEquals(now - 1, found.newestTimestamp)
    }

    @Test
    fun anUnreadableMarker_readsAllExits() {
        markers.values[ExitReportReader.MARKER_KEY] = "not a number"

        assertEquals(1, reader(exit(ExitReason.ANR, now - 1)).read(now).events.size)
    }

    @Test
    fun anExitOlderThan156Hours_isSkipped_butMovesTheMarker_andExactly156HoursIsKept() {
        val maxAge = TelemetryLimits.RECORD_MAX_AGE_MILLIS
        assertEquals(156L * 3_600_000L, maxAge)

        val found = reader(exit(ExitReason.ANR, now - maxAge - 1), exit(ExitReason.LOW_MEMORY, now - maxAge)).read(now)

        assertEquals(listOf(now - maxAge), found.events.map { it.occurredAt })
        assertEquals(now - maxAge, found.newestTimestamp)

        val tooOld = reader(exit(ExitReason.ANR, now - maxAge - 1)).read(now)
        assertTrue(tooOld.events.isEmpty())
        assertEquals(now - maxAge - 1, tooOld.newestTimestamp)
    }

    @Test
    fun aSourceThatThrows_readsAsNoExits() {
        val found = ExitReportReader(ExitInfoSource { error("synthetic source failure") }, markers).read(now)

        assertEquals(ExitReports(events = emptyList(), newestTimestamp = null), found)
    }

    @Test
    fun aMarkerStoreThatThrows_readsAsNoExits() {
        val failing = object : KeyValueStore {
            override fun getString(key: String): String? = error("synthetic store failure")
            override fun putString(key: String, value: String) = error("synthetic store failure")
        }

        val found = ExitReportReader(ExitInfoSource { listOf(exit(ExitReason.ANR, now - 1)) }, failing).read(now)

        assertEquals(ExitReports(events = emptyList(), newestTimestamp = null), found)
    }

    @Test
    fun aTraceThatIsNull_throws_orDoesNotParse_givesACrashWithoutAnError() {
        var closed = false
        val unparsable = object : InputStream() {
            private val bytes = ByteArrayInputStream("garbage".toByteArray())
            override fun read(): Int = bytes.read()
            override fun close() {
                closed = true
            }
        }
        val found = reader(
            exit(ExitReason.ANR, now - 4_000) { null },
            exit(ExitReason.ANR, now - 3_000) { throw IOException("synthetic trace failure") },
            exit(ExitReason.CRASH_NATIVE, now - 2_000) { unparsable },
            exit(ExitReason.CRASH_NATIVE, now - 1_000) {
                object : InputStream() {
                    override fun read(): Int = throw IOException("synthetic read failure")
                }
            }
        ).read(now)

        assertEquals(4, found.events.size)
        found.events.forEach { assertNull(it.error) }
        assertEquals(
            listOf(CrashKind.ANR, CrashKind.ANR, CrashKind.NATIVE, CrashKind.NATIVE),
            found.events.map { it.kind }
        )
        assertTrue("the reader closes the trace it opened", closed)
    }

    @Test
    fun theEvents_comeOldestFirst_whateverTheSourcesOrder() {
        val found = reader(
            exit(ExitReason.SIGNALED, now - 1_000),
            exit(ExitReason.ANR, now - 3_000),
            exit(ExitReason.LOW_MEMORY, now - 2_000)
        ).read(now)

        assertEquals(listOf(now - 3_000, now - 2_000, now - 1_000), found.events.map { it.occurredAt })
        assertEquals(now - 1_000, found.newestTimestamp)
    }

    @Test
    fun noExits_isEmpty_andNeverReadsTheMarker() {
        val found = reader().read(now)

        assertEquals(ExitReports(events = emptyList(), newestTimestamp = null), found)
        assertEquals(0, markers.reads)
        assertEquals(0, markers.writes)
    }

    @Test
    fun none_findsNothing() {
        assertEquals(ExitReports(events = emptyList(), newestTimestamp = null), ExitReportReader.NONE.read(now))
    }

    /** Review fix (R2): one marker key per process, `exit_info.last_processed.<processName>`. */
    @Test
    fun theMarkerKey_isPerProcess_andAReaderUsesOnlyItsOwn() {
        val mainKey = ExitReportReader.markerKeyFor("com.example.merchant")
        val remoteKey = ExitReportReader.markerKeyFor("com.example.merchant:remote")
        assertEquals("exit_info.last_processed.com.example.merchant", mainKey)
        assertEquals("exit_info.last_processed.com.example.merchant:remote", remoteKey)

        val main = ExitReportReader(ExitInfoSource { listOf(exit(ExitReason.ANR, now - 1_000)) }, markers, mainKey)
        main.markProcessed(main.read(now).newestTimestamp!!, now)

        assertEquals(mapOf(mainKey to (now - 1_000).toString()), markers.values)
    }

    /** Review fix (R2): two processes never share a marker, so one marking its exits never hides the other's. */
    @Test
    fun twoProcesses_keepSeparateMarkers() {
        val mainKey = ExitReportReader.markerKeyFor("com.example.merchant")
        val remoteKey = ExitReportReader.markerKeyFor("com.example.merchant:remote")
        val main = ExitReportReader(ExitInfoSource { listOf(exit(ExitReason.ANR, now - 1_000)) }, markers, mainKey)
        val remote = ExitReportReader(
            ExitInfoSource { listOf(exit(ExitReason.CRASH_NATIVE, now - 5_000)) },
            markers,
            remoteKey
        )

        main.markProcessed(main.read(now).newestTimestamp!!, now)
        val remoteFound = remote.read(now)

        assertEquals(listOf(CrashKind.NATIVE), remoteFound.events.map { it.kind })
        remote.markProcessed(remoteFound.newestTimestamp!!, now)
        assertEquals((now - 1_000).toString(), markers.values[mainKey])
        assertEquals((now - 5_000).toString(), markers.values[remoteKey])
        assertNull(markers.values[ExitReportReader.MARKER_KEY])
    }

    /** Review fix (R4): a marker later than now, stored while the clock was ahead, is read as no marker. */
    @Test
    fun aMarkerLaterThanNow_isReadAsNoMarker() {
        markers.values[ExitReportReader.MARKER_KEY] = (now + 3_600_000).toString()

        val found = reader(exit(ExitReason.ANR, now - 2_000), exit(ExitReason.LOW_MEMORY, now - 1_000)).read(now)

        assertEquals(listOf(CrashKind.ANR, CrashKind.OS_KILL), found.events.map { it.kind })
        assertEquals(now - 1_000, found.newestTimestamp)
    }

    @Test
    fun aMarkerEqualToNow_isStillAMarker() {
        markers.values[ExitReportReader.MARKER_KEY] = now.toString()

        assertTrue(reader(exit(ExitReason.ANR, now - 1)).read(now).events.isEmpty())
    }

    /** Review fix (R4): the stored marker is clamped to now, so a clock running ahead never hides later exits. */
    @Test
    fun markProcessed_clampsTheMarkerToNow() {
        val reader = reader()

        reader.markProcessed(now + 3_600_000, now)
        assertEquals(now.toString(), markers.values[ExitReportReader.MARKER_KEY])

        reader.markProcessed(now - 1, now)
        assertEquals((now - 1).toString(), markers.values[ExitReportReader.MARKER_KEY])
    }

    @Test
    fun anExitAfterAClampedMarker_isReadNextLaunch() {
        val exits = mutableListOf(exit(ExitReason.ANR, now + 60_000)) // stamped by a clock running ahead
        val reader = ExitReportReader(ExitInfoSource { exits.toList() }, markers)
        reader.markProcessed(reader.read(now).newestTimestamp!!, now)

        exits += exit(ExitReason.CRASH_NATIVE, now + 1_000)
        val next = reader.read(now + 2_000)

        assertEquals(listOf(now + 1_000), next.events.filter { it.kind == CrashKind.NATIVE }.map { it.occurredAt })
    }

    /** The version branch of [AndroidExitInfoSource.create]: no source below API 30. Building one does no I/O. */
    @Test
    fun androidExitInfoSource_create_isNoneBelowApi30_andTheAndroidSourceFrom30() {
        val context = ContextWrapper(null)

        assertSame(ExitInfoSource.NONE, AndroidExitInfoSource.create(context, sdkInt = 24))
        assertSame(ExitInfoSource.NONE, AndroidExitInfoSource.create(context, sdkInt = 29))
        assertTrue(AndroidExitInfoSource.create(context, sdkInt = 30) is AndroidExitInfoSource)
        assertTrue(AndroidExitInfoSource.create(context, sdkInt = 36) is AndroidExitInfoSource)
    }

    private companion object {
        const val SDK_FRAME =
            "com.paydock.feature.card.presentation.ui.CardDetailsWidgetKt.CardDetailsWidget(CardDetailsWidget.kt:77)"
    }
}

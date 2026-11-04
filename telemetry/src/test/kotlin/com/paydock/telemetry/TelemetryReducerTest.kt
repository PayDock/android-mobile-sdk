package com.paydock.telemetry

import com.paydock.telemetry.TelemetryEffect.Drop
import com.paydock.telemetry.TelemetryEffect.DropReason
import com.paydock.telemetry.TelemetryEffect.Enqueue
import com.paydock.telemetry.TelemetryEffect.Export
import com.paydock.telemetry.TelemetryEffect.Persist
import com.paydock.telemetry.TelemetryEffect.ScheduleFlush
import com.paydock.telemetry.TelemetryEvent.HttpMethod
import com.paydock.telemetry.TelemetryEvent.NetworkError
import com.paydock.telemetry.TelemetryReducer.COLLAPSE_WINDOW_MILLIS
import com.paydock.telemetry.TelemetryReducer.FLUSH_INTERVAL_MILLIS
import com.paydock.telemetry.TelemetryReducer.FLUSH_THRESHOLD
import com.paydock.telemetry.TelemetryReducer.MAX_BATCH
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the pure [TelemetryReducer] (R_01, R_04 to R_07), one test per rule. Times are UTC epoch
 * milliseconds; events are synthetic and carry no message text.
 */
internal class TelemetryReducerTest {

    private val t0 = 1_700_000_000_000L
    private val sessionId = "session-1"

    private val paydockFrame = "com.paydock.feature.card.CardRepository.tokenise(CardRepository.kt:42)"
    private val error = TelemetryTestFakes.sdkError(frames = listOf("java.util.Foo.bar(Foo.java:1)", paydockFrame))
    private val crash = TelemetryTestFakes.crash()

    private fun state(level: TelemetryLevel = TelemetryLevel.ALL, enabled: Boolean = true) =
        TelemetryState(level = level, enabled = enabled, sessionId = sessionId)

    private fun reduce(state: TelemetryState, intent: TelemetryIntent, now: Long = t0) =
        TelemetryReducer.reduce(state, intent, now)

    private fun report(state: TelemetryState, event: TelemetryEvent, now: Long = t0) =
        reduce(state, TelemetryIntent.Reported(event), now)

    private fun record(sequence: Long, event: TelemetryEvent, at: Long = t0, occurrences: Int = 1) = PendingRecord(
        id = "$sessionId-$sequence",
        sessionId = sessionId,
        event = event,
        firstSeenAt = at,
        occurrences = occurrences
    )

    private fun networkFailure(
        method: HttpMethod = HttpMethod.POST,
        urlTemplate: String = "/v1/payment_sources/tokens",
        failure: NetworkError = NetworkError.HttpStatus(502),
    ) = TelemetryEvent.NetworkFailure(
        method = method,
        urlTemplate = urlTemplate,
        serverAddress = "api.invalid",
        failure = failure
    )

    @Test
    fun everyIntent_isReduced() {
        val start = state()
        val intents = listOf(
            TelemetryIntent.Reported(error),
            TelemetryIntent.Flush,
            TelemetryIntent.AppBackgrounded,
            TelemetryIntent.LevelChanged(TelemetryLevel.ERRORS)
        )

        intents.forEach { intent ->
            val (next, effects) = reduce(start, intent)
            assertEquals(sessionId, next.sessionId)
            assertTrue(effects.none { it is Drop })
        }
    }

    @Test
    fun reported_disabled_dropsWithoutStateChange() {
        val start = state(enabled = false)

        val (next, effects) = report(start, crash)

        assertEquals(start, next)
        assertEquals(listOf(Drop(DropReason.DISABLED)), effects)
    }

    @Test
    fun reported_levelOff_dropsEveryEvent() {
        val start = state(level = TelemetryLevel.OFF)

        listOf(crash, error, networkFailure()).forEach { event ->
            val (next, effects) = report(start, event)

            assertEquals(start, next)
            assertEquals(listOf(Drop(DropReason.LEVEL_OFF)), effects)
        }
    }

    @Test
    fun reported_levelErrors_keepsErrorAndCrashEvents() {
        val start = state(level = TelemetryLevel.ERRORS)
        val events = listOf(
            crash,
            error,
            TelemetryEvent.UncaughtException(error = error.error),
            networkFailure()
        )

        events.forEach { event ->
            assertTrue(TelemetryReducer.isError(event))
            val (next, effects) = report(start, event)

            assertTrue(effects.none { it is Drop })
            assertEquals(1, next.nextSequence)
        }
    }

    @Test
    fun reported_levelAll_keepsEveryEvent() {
        val events = listOf(crash, error, TelemetryEvent.UncaughtException(error = error.error), networkFailure())

        events.forEach { event ->
            val (next, effects) = report(state(), event)

            assertTrue(effects.contains(Persist(record(0, event))))
            assertEquals(1, next.persisted)
        }
    }

    @Test
    fun levelChanged_setsTheLevel_withNoEffect_andAppliesToTheEventsThatFollow() {
        val (changed, effects) = reduce(state(), TelemetryIntent.LevelChanged(TelemetryLevel.OFF))

        assertEquals(state().copy(level = TelemetryLevel.OFF), changed)
        assertTrue(effects.isEmpty())
        assertEquals(listOf(Drop(DropReason.LEVEL_OFF)), report(changed, error).second)

        val (restored, _) = reduce(changed, TelemetryIntent.LevelChanged(TelemetryLevel.ALL))
        assertTrue(report(restored, error).second.contains(Persist(record(0, error))))
    }

    @Test
    fun crash_isNeverHeld() {
        val (first, firstEffects) = report(state(), crash)

        val expected = record(0, crash)
        assertEquals(listOf(Persist(expected), Enqueue(expected), ScheduleFlush(FLUSH_INTERVAL_MILLIS)), firstEffects)
        assertEquals(1, first.queued)
        assertEquals(1, first.persisted)
        assertTrue(first.collapsing.isEmpty())
        assertTrue(first.flushScheduled)

        val (second, secondEffects) = report(first, crash, t0 + 1)

        val repeat = record(1, crash, at = t0 + 1)
        assertEquals(listOf(Persist(repeat), Enqueue(repeat)), secondEffects)
        assertEquals(2, second.queued)
        assertTrue(second.collapsing.isEmpty())
    }

    @Test
    fun error_isHeld_thenCountedWithinTheWindow_thenReleasedAfterIt() {
        val key = TelemetryReducer.fingerprint(error)

        val (held, heldEffects) = report(state(), error)
        val first = record(0, error)
        assertEquals(listOf(Persist(first), ScheduleFlush(FLUSH_INTERVAL_MILLIS)), heldEffects)
        assertEquals(mapOf(key to first), held.collapsing)
        assertEquals(0, held.queued)
        assertEquals(1, held.persisted)

        val (counted, countedEffects) = report(held, error, t0 + COLLAPSE_WINDOW_MILLIS - 1)
        assertTrue(countedEffects.isEmpty())
        assertEquals(2, counted.collapsing.getValue(key).occurrences)
        assertEquals(t0, counted.collapsing.getValue(key).firstSeenAt)
        assertEquals(1, counted.persisted)
        assertEquals(0, counted.queued)

        val later = t0 + COLLAPSE_WINDOW_MILLIS
        val (released, releasedEffects) = report(counted, error, later)
        val countedFirst = first.copy(occurrences = 2)
        val next = record(2, error, at = later)
        assertEquals(listOf(Enqueue(countedFirst), Persist(countedFirst), Persist(next)), releasedEffects)
        assertEquals(mapOf(key to next), released.collapsing)
        assertEquals(1, released.queued)
        assertEquals(2, released.persisted)
    }

    @Test
    fun networkFailure_isKeyedByMethodTemplateAndFailure() {
        val base = networkFailure()

        assertEquals(
            "paydock.network.failure|||POST|/v1/payment_sources/tokens|http_502",
            TelemetryReducer.fingerprint(base)
        )
        assertEquals(
            "paydock.network.failure|java.net.SocketTimeoutException|$paydockFrame|GET|/v1/charges|transport_timeout",
            TelemetryReducer.fingerprint(
                networkFailure(
                    method = HttpMethod.GET,
                    urlTemplate = "/v1/charges",
                    failure = NetworkError.Transport(
                        kind = TelemetryEvent.TransportKind.TIMEOUT,
                        error = TelemetryEvent.ErrorInfo("java.net.SocketTimeoutException", listOf(paydockFrame))
                    )
                )
            )
        )
        assertEquals(
            "paydock.network.failure|||POST|/v1/payment_sources/tokens|decode",
            TelemetryReducer.fingerprint(networkFailure(failure = NetworkError.Decode(statusCode = 200)))
        )

        val variants = listOf(
            networkFailure(method = HttpMethod.GET),
            networkFailure(urlTemplate = "/v1/charges"),
            networkFailure(failure = NetworkError.HttpStatus(503))
        )
        variants.forEach { assertNotEquals(TelemetryReducer.fingerprint(base), TelemetryReducer.fingerprint(it)) }

        val (held, _) = variants.fold(report(state(), base).first to emptyList<TelemetryEffect>()) { (s, _), event ->
            report(s, event)
        }
        assertEquals(4, held.collapsing.size)
        val (repeated, effects) = report(held, base, t0 + 1)
        assertTrue(effects.isEmpty())
        assertEquals(2, repeated.collapsing.getValue(TelemetryReducer.fingerprint(base)).occurrences)
    }

    @Test
    fun fingerprint_isNameExceptionTypeAndTopPaydockFrame() {
        assertEquals(
            "paydock.error|java.lang.IllegalStateException|$paydockFrame",
            TelemetryReducer.fingerprint(error)
        )
        assertEquals(
            "paydock.error|java.lang.IllegalStateException|$paydockFrame",
            TelemetryReducer.fingerprint(TelemetryEvent.UncaughtException(error = error.error))
        )
        assertEquals("paydock.crash|java.lang.IllegalStateException|", TelemetryReducer.fingerprint(crash))
        assertEquals(
            "paydock.crash||",
            TelemetryReducer.fingerprint(
                TelemetryEvent.Crash(error = null, source = TelemetryEvent.CrashSource.EXIT_INFO, exitReason = 4)
            )
        )
    }

    @Test
    fun flush_releasesExpiredRecords_exportsInBatchesOf50_andReschedules() {
        val expired = record(0, error, at = t0 - COLLAPSE_WINDOW_MILLIS)
        val fresh = record(1, networkFailure(), at = t0 - 1)
        val start = state().copy(
            queued = 120,
            flushScheduled = true,
            nextSequence = 2,
            collapsing = mapOf("expired" to expired, "fresh" to fresh)
        )

        val (next, effects) = reduce(start, TelemetryIntent.Flush)

        assertEquals(
            listOf(
                Enqueue(expired),
                Export(MAX_BATCH),
                Export(MAX_BATCH),
                Export(MAX_BATCH),
                ScheduleFlush(FLUSH_INTERVAL_MILLIS)
            ),
            effects
        )
        assertEquals(0, next.queued)
        assertEquals(t0, next.lastExport)
        assertTrue(next.flushScheduled)
        assertEquals(mapOf("fresh" to fresh), next.collapsing)
    }

    @Test
    fun flush_withNothingPending_exportsNothing_andDoesNotReschedule() {
        val (next, effects) = reduce(state().copy(flushScheduled = true), TelemetryIntent.Flush)

        assertTrue(effects.isEmpty())
        assertFalse(next.flushScheduled)
        assertNull(next.lastExport)
    }

    @Test
    fun reported_atTheThresholdOf20_exportsAtOnce() {
        var current = state()
        repeat(FLUSH_THRESHOLD - 1) { index ->
            val (next, effects) = report(current, crash, t0 + index)
            assertTrue(effects.none { it is Export })
            current = next
        }
        assertEquals(FLUSH_THRESHOLD - 1, current.queued)

        val (next, effects) = report(current, crash, t0 + 100)

        assertEquals(Export(MAX_BATCH), effects.last())
        assertEquals(1, effects.count { it is Export })
        assertEquals(0, next.queued)
        assertEquals(t0 + 100, next.lastExport)
    }

    @Test
    fun appBackgrounded_releasesEveryHeldRecord_exportsEverything_andDoesNotReschedule() {
        val a = record(0, error, at = t0 - 1, occurrences = 3)
        val b = record(1, networkFailure(), at = t0)
        val start = state().copy(queued = 3, collapsing = mapOf("a" to a, "b" to b))

        val (next, effects) = reduce(start, TelemetryIntent.AppBackgrounded)

        assertEquals(listOf(Enqueue(a), Persist(a), Enqueue(b), Export(MAX_BATCH)), effects)
        assertTrue(next.collapsing.isEmpty())
        assertEquals(0, next.queued)
        assertEquals(t0, next.lastExport)
        assertFalse(next.flushScheduled)
    }

    @Test
    fun records_haveDeterministicIds_andCarryTheSessionId() {
        val (first, firstEffects) = report(state(), crash)
        val (_, secondEffects) = report(first, error, t0 + 1)

        val records = (firstEffects + secondEffects).mapNotNull { (it as? Persist)?.record }
        assertEquals(listOf("$sessionId-0", "$sessionId-1"), records.map { it.id })
        assertTrue(records.all { it.sessionId == sessionId })
    }

    @Test
    fun reduce_isPure_sameInputGivesEqualOutput_andTheInputIsNotMutated() {
        val held = record(0, error, at = t0 - COLLAPSE_WINDOW_MILLIS)
        val start = state().copy(queued = 25, collapsing = mapOf(TelemetryReducer.fingerprint(error) to held))
        val snapshot = start.copy(collapsing = HashMap(start.collapsing))
        val intents = listOf(
            TelemetryIntent.Reported(error),
            TelemetryIntent.Reported(crash),
            TelemetryIntent.Flush,
            TelemetryIntent.AppBackgrounded,
            TelemetryIntent.LevelChanged(TelemetryLevel.ERRORS)
        )

        intents.forEach { intent ->
            assertEquals(reduce(start, intent), reduce(start, intent))
            assertEquals(snapshot, start)
        }
    }
}

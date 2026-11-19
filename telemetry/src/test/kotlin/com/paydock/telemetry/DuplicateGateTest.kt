package com.paydock.telemetry

import com.paydock.telemetry.AttributeValue.Num
import com.paydock.telemetry.AttributeValue.Str
import com.paydock.telemetry.TelemetryEvent.HttpMethod
import com.paydock.telemetry.TelemetryEvent.NetworkError
import com.paydock.telemetry.TelemetryTestFakes.SESSION_ID
import com.paydock.telemetry.TelemetryTestFakes.T0
import com.paydock.telemetry.TelemetryTestFakes.record
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Unit tests for the pure [DuplicateGate] (R_06, MD-27 acceptance). */
internal class DuplicateGateTest {

    private val frame = "com.paydock.feature.card.CardRepository.tokenise(CardRepository.kt:42)"
    private val otherFrame = "com.paydock.feature.card.CardRepository.tokenise(CardRepository.kt:43)"

    private fun failure(attempt: Int = 1, code: Int = 502) = TelemetryEvent.NetworkFailure(
        method = HttpMethod.POST,
        urlTemplate = "/v1/payment_sources/tokens",
        serverAddress = "api.example.test",
        failure = NetworkError.HttpStatus(code),
        attempt = attempt
    )

    private fun notice(sequence: Long, count: Int = 1) =
        record(sequence, event = TelemetryEvent.Dropped(TelemetryEvent.DroppedReason.REJECTED, count))

    @Test
    fun theSameKindAndType_withDifferentFramesOrFields_staySeparate() {
        val records = listOf(
            record(1, event = TelemetryTestFakes.sdkError(frames = listOf(frame))),
            record(2, event = TelemetryTestFakes.sdkError(frames = listOf(otherFrame))),
            record(3, event = TelemetryEvent.SdkError(TelemetryTestFakes.sdkError().error, errorCode = "A")),
            record(4, event = TelemetryEvent.SdkError(TelemetryTestFakes.sdkError().error, errorCode = "B")),
            record(5, event = TelemetryEvent.SdkError(TelemetryTestFakes.sdkError().error, widget = "CardDetailsWidget")),
            record(6, event = failure(code = 502)),
            record(7, event = failure(code = 503))
        )

        val merged = DuplicateGate.merge(records)

        assertEquals(records, merged.map { it.record })
        assertEquals(records.map { listOf(it.id) }, merged.map { it.sourceIds })
        assertEquals(records.size, records.mapNotNull(DuplicateGate::signature).toSet().size)
    }

    @Test
    fun exactDuplicatesInOneSession_merge_withTheSummedCountAndTheHighestAttempt_keepingTheEarliestRecord() {
        val earliest = record(10, event = failure(attempt = 1), createdAt = T0)
        val withCount = record(9, event = failure(attempt = 3), createdAt = T0 + 5)
            .with("paydock.occurrences", Num(2))
        val sameTimeLaterSequence = record(11, event = failure(attempt = 2), createdAt = T0)

        val merged = DuplicateGate.merge(listOf(withCount, sameTimeLaterSequence, earliest)).single()

        assertEquals(earliest.id, merged.record.id)
        assertEquals(earliest.timeUnixMillis, merged.record.timeUnixMillis)
        assertEquals(Num(4), merged.record.value("paydock.occurrences"))
        assertEquals(Num(3), merged.record.value("paydock.http.attempt"))
        assertEquals(listOf(withCount.id, sameTimeLaterSequence.id, earliest.id), merged.sourceIds)
        val keys = merged.record.attributes.map { it.key }
        assertEquals(Allowlist.RECORD_KEYS.keys.filter { it in keys }, keys)
    }

    @Test
    fun theEarliest_isByTimeThenTheSequenceAsANumber() {
        val tenth = record(10, createdAt = T0)
        val ninth = record(9, createdAt = T0)

        val merged = DuplicateGate.merge(listOf(tenth, ninth)).single()

        assertEquals(ninth.id, merged.record.id)
        assertEquals(Num(2), merged.record.value("paydock.occurrences"))
        assertNull(merged.record.value("paydock.http.attempt"))
    }

    @Test
    fun aSingleRecord_isKeptUnchanged_withoutAnOccurrencesCount() {
        val single = record(1)

        val merged = DuplicateGate.merge(listOf(single)).single()

        assertEquals(single, merged.record)
        assertNull(merged.record.value("paydock.occurrences"))
    }

    @Test
    fun differentSessions_neverMerge() {
        val first = record(1, sessionId = SESSION_ID)
        val second = record(1, sessionId = TelemetryTestFakes.uuid(202))

        val merged = DuplicateGate.merge(listOf(first, second))

        assertEquals(listOf(first, second), merged.map { it.record })
        assertNotEquals(DuplicateGate.signature(first), DuplicateGate.signature(second))
    }

    @Test
    fun notices_neverMerge() {
        val first = notice(1)
        val second = notice(2)

        val merged = DuplicateGate.merge(listOf(first, second))

        assertEquals(listOf(first, second), merged.map { it.record })
        assertNull(DuplicateGate.signature(first))
    }

    /** R_04: a payment record is one attempt's start or end, so two equal ones are two records. */
    @Test
    fun paymentRecords_neverMerge_evenWhenIdentical() {
        val finish = TelemetryEvent.PaymentFinished(
            widget = "CardDetailsWidget",
            paymentId = TelemetryTestFakes.uuid(301),
            outcome = TelemetryEvent.PaymentOutcome.ERROR,
            errorCode = "TokenisingCardException",
            durationMs = 2_215
        )
        val start = TelemetryEvent.PaymentStarted("CardDetailsWidget", TelemetryTestFakes.uuid(301))
        val records = listOf(
            record(1, event = finish, createdAt = T0),
            record(2, event = finish, createdAt = T0),
            record(3, event = start, createdAt = T0),
            record(4, event = start, createdAt = T0)
        )

        val merged = DuplicateGate.merge(records)

        assertEquals(records, merged.map { it.record })
        assertEquals(records.map { listOf(it.id) }, merged.map { it.sourceIds })
        records.forEach { assertNull(DuplicateGate.signature(it)) }
        merged.forEach { assertNull(it.record.value("paydock.occurrences")) }
    }

    @Test
    fun aRecordWithoutSessionId_isNeverMerged() {
        val bare = record(1).let { it.copy(attributes = it.attributes.filterNot { a -> a.key == "session.id" }) }

        assertNull(DuplicateGate.signature(bare))
        assertEquals(2, DuplicateGate.merge(listOf(bare, bare.copy(id = "other-2"))).size)
    }

    @Test
    fun merge_keepsInputOrderByEachGroupsFirstRecord() {
        val a1 = record(1, event = TelemetryTestFakes.sdkError("test.A"))
        val b1 = record(2, event = TelemetryTestFakes.sdkError("test.B"))
        val a2 = record(3, event = TelemetryTestFakes.sdkError("test.A"))
        val n = notice(4)

        val merged = DuplicateGate.merge(listOf(a1, b1, n, a2))

        assertEquals(listOf(a1.id, b1.id, n.id), merged.map { it.record.id })
        assertEquals(listOf(a1.id, a2.id), merged.first().sourceIds)
    }

    @Test
    fun signature_isSessionNameAndTheSignatureKeys_inOrder() {
        val record = record(1, event = TelemetryTestFakes.sdkError("test.A"))

        val signature = DuplicateGate.signature(record)

        assertNotNull(signature)
        val stacktrace = (record.value("exception.stacktrace") as Str).value
        assertEquals(
            "$SESSION_ID\u0000paydock.error\u0000exception.type=test.A\u0000exception.stacktrace=$stacktrace" +
                "\u0000paydock.error.kind=sdk_error",
            signature
        )
    }
}

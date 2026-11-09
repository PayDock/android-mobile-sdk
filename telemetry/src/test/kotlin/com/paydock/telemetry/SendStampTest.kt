package com.paydock.telemetry

import com.paydock.telemetry.AttributeValue.Str
import com.paydock.telemetry.TelemetryLimits.ORDERING_WINDOW_MILLIS
import com.paydock.telemetry.TelemetryTestFakes.T0
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [SendStamp] (D20, R_08, R_10): a record older than the ordering window at send time goes out with
 * the send time and `paydock.occurred_at`; a fresher one keeps its own time. Both times add the clock offset, and the
 * body is never changed.
 */
internal class SendStampTest {

    private val record = TelemetryTestFakes.record(1, event = TelemetryTestFakes.crash(), createdAt = T0)

    private val networkFailure = TelemetryEvent.NetworkFailure(
        method = TelemetryEvent.HttpMethod.POST,
        urlTemplate = "/v1/payment_sources/tokens",
        serverAddress = "api-sandbox.paydock.com",
        failure = TelemetryEvent.NetworkError.HttpStatus(503),
        attempt = 1
    )

    private fun TelemetryRecord.keys(): List<String> = attributes.map { it.key }

    @Test
    fun aRecordWithinTheWindow_keepsItsTime_andGetsNoAttribute() {
        val stamped = SendStamp.stamp(record, sentAt = T0 + ORDERING_WINDOW_MILLIS, offsetMillis = 0)

        assertEquals(record, stamped)
    }

    @Test
    fun aRecordOneMillisecondPastTheWindow_isStampedWithTheSendTime() {
        val sentAt = T0 + ORDERING_WINDOW_MILLIS + 1

        val stamped = SendStamp.stamp(record, sentAt, offsetMillis = 0)

        assertEquals(sentAt, stamped.timeUnixMillis)
        assertEquals(Str(UtcTimestamp.iso8601(T0)), stamped.value(SendStamp.OCCURRED_AT))
        assertEquals(record.body, stamped.body)
        assertEquals(record.id, stamped.id)
        assertEquals(record.severity, stamped.severity)
    }

    @Test
    fun theStamp_isInsertedInAllowlistOrder_rightAfterTheGroupKey() {
        val network = TelemetryTestFakes.record(2, event = networkFailure, createdAt = T0)

        val stamped = SendStamp.stamp(network, T0 + 2 * ORDERING_WINDOW_MILLIS, offsetMillis = 0)

        val expected = network.keys().toMutableList().apply {
            add(indexOf("paydock.group.key") + 1, SendStamp.OCCURRED_AT)
        }
        assertEquals(expected, stamped.keys())
        assertEquals(network.attributes, stamped.attributes.filterNot { it.key == SendStamp.OCCURRED_AT })
    }

    @Test
    fun theStamp_ofARecordWithoutGroupKey_comesBeforeTheExceptionKeys() {
        val stamped = SendStamp.stamp(record, T0 + 2 * ORDERING_WINDOW_MILLIS, offsetMillis = 0)

        val keys = stamped.keys()
        assertEquals(keys.indexOf(SendStamp.OCCURRED_AT) + 1, keys.indexOf("exception.type"))
        assertEquals(Allowlist.RECORD_KEYS.keys.filter { it in keys }, keys)
    }

    @Test
    fun theOffset_correctsAFreshRecordsTime() {
        val stamped = SendStamp.stamp(record, sentAt = T0 + 1_000, offsetMillis = 3_600_000)

        assertEquals(T0 + 3_600_000, stamped.timeUnixMillis)
        assertNull(stamped.value(SendStamp.OCCURRED_AT))
    }

    @Test
    fun theOffset_correctsBothTimesOfALateRecord() {
        val sentAt = T0 + ORDERING_WINDOW_MILLIS + 1

        val stamped = SendStamp.stamp(record, sentAt, offsetMillis = -120_000)

        assertEquals(sentAt - 120_000, stamped.timeUnixMillis)
        assertEquals(Str(UtcTimestamp.iso8601(T0 - 120_000)), stamped.value(SendStamp.OCCURRED_AT))
    }

    @Test
    fun theWindow_isMeasuredBetweenCorrectedTimes_soTheOffsetNeverDecidesIt() {
        val sentAt = T0 + ORDERING_WINDOW_MILLIS

        listOf(-3_600_000L, 0L, 3_600_000L).forEach { offset ->
            assertNull("offset $offset", SendStamp.stamp(record, sentAt, offset).value(SendStamp.OCCURRED_AT))
        }
    }

    @Test
    fun aRecordFromTheFuture_isNeverStamped() {
        val stamped = SendStamp.stamp(record, sentAt = T0 - 10 * ORDERING_WINDOW_MILLIS, offsetMillis = 0)

        assertEquals(record, stamped)
    }

    /** Compared against a snapshot that shares nothing with the record: a new list of new attributes. */
    @Test
    fun stamping_neverChangesTheInputRecord() {
        val attributes = ArrayList(record.attributes.map { Attribute(it.key, it.value) })
        val snapshot = TelemetryRecord(record.id, record.timeUnixMillis, record.severity, record.body, attributes)
        val keys = record.keys().toList()

        val stamped = SendStamp.stamp(record, T0 + 3 * ORDERING_WINDOW_MILLIS, offsetMillis = 42)

        assertEquals(snapshot, record)
        assertEquals(keys, record.keys())
        assertNull(record.value(SendStamp.OCCURRED_AT))
        assertEquals(T0, record.timeUnixMillis)
        assertNotSame(record.attributes, stamped.attributes)
    }
}

package com.paydock.telemetry

import com.paydock.telemetry.AttributeValue.Num
import com.paydock.telemetry.AttributeValue.Str
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [TelemetryRecord]'s accessors and [TelemetryRecord.with]. */
internal class TelemetryRecordTest {

    private val record = TelemetryRecord(
        id = "s-1",
        timeUnixMillis = 1L,
        severity = Severity.WARN,
        body = "paydock.telemetry.dropped · rate_limit · 3 records",
        attributes = listOf(
            Attribute("event.name", Str("paydock.telemetry.dropped")),
            Attribute("session.id", Str("s")),
            Attribute("paydock.dropped.count", Num(3))
        )
    )

    @Test
    fun accessors_readTheirAttributes() {
        assertEquals("s", record.sessionId)
        assertEquals("paydock.telemetry.dropped", record.eventName)
        assertTrue(record.isDroppedNotice)
        assertEquals(Num(3), record.value("paydock.dropped.count"))
        assertNull(record.value("paydock.widget"))
        assertFalse(record.copy(attributes = emptyList()).isDroppedNotice)
        assertNull(record.copy(attributes = emptyList()).sessionId)
    }

    @Test
    fun with_replacesAValueInPlace() {
        val updated = record.with("paydock.dropped.count", Num(5))

        assertEquals(record.attributes.map { it.key }, updated.attributes.map { it.key })
        assertEquals(Num(5), updated.value("paydock.dropped.count"))
    }

    @Test
    fun with_insertsANewKeyInAllowlistOrder() {
        val updated = record.with("paydock.occurrences", Num(2)).with("log.record.uid", Str("s-1"))

        assertEquals(
            listOf("event.name", "session.id", "log.record.uid", "paydock.occurrences", "paydock.dropped.count"),
            updated.attributes.map { it.key }
        )
    }
}

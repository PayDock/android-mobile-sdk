package com.paydock.telemetry

import com.paydock.telemetry.AttributeValue.Bool
import com.paydock.telemetry.AttributeValue.Num
import com.paydock.telemetry.AttributeValue.Str
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Unit tests for [OtlpLogEncoder]: the record JSON, its parsing back, and the request envelope. */
internal class OtlpLogEncoderTest {

    private val record = TelemetryRecord(
        id = "$SESSION_ID-7",
        timeUnixMillis = 1_793_852_047_512L,
        severity = Severity.ERROR,
        body = "paydock.network.failure · http_5xx 503 · GET /v1/charges/{id}",
        attributes = listOf(
            Attribute("event.name", Str("paydock.network.failure")),
            Attribute("session.id", Str(SESSION_ID)),
            Attribute("log.record.uid", Str("$SESSION_ID-7")),
            Attribute("paydock.exception.in_sdk", Bool(false)),
            Attribute("http.response.status_code", Num(503))
        )
    )

    @Test
    fun recordJson_writesTimeUnixNanoAsMillisTimesAMillionInAString() {
        val json = OtlpLogEncoder.recordJson(record)

        assertEquals(JsonPrimitive("1793852047512000000"), json["timeUnixNano"])
        assertEquals(JsonPrimitive(17), json["severityNumber"])
        assertEquals(JsonPrimitive("ERROR"), json["severityText"])
        assertEquals(JsonPrimitive(record.body), json.getValue("body").jsonObject["stringValue"])
    }

    @Test
    fun recordJson_writesEachValueInItsOtlpForm() {
        val values = OtlpLogEncoder.recordJson(record).getValue("attributes").jsonArray.map { it.jsonObject }

        assertEquals(record.attributes.map { it.key }, values.map { it.getValue("key").jsonPrimitive.content })
        assertEquals(
            buildJsonObject { put("stringValue", "paydock.network.failure") },
            values[0].getValue("value")
        )
        assertEquals(buildJsonObject { put("boolValue", false) }, values[3].getValue("value"))
        assertEquals(buildJsonObject { put("intValue", "503") }, values[4].getValue("value"))
    }

    @Test
    fun parseRecord_roundTripsRecordJson() {
        assertEquals(record, OtlpLogEncoder.parseRecord(OtlpLogEncoder.recordJson(record)))
        val notice = record.copy(severity = Severity.WARN, attributes = record.attributes.take(3))
        assertEquals(notice, OtlpLogEncoder.parseRecord(OtlpLogEncoder.recordJson(notice)))
    }

    @Test
    fun parseRecord_roundTripsThroughTheSerialisedText() {
        val text = Json.encodeToString(JsonObject.serializer(), OtlpLogEncoder.recordJson(record))

        assertEquals(record, OtlpLogEncoder.parseRecord(Json.parseToJsonElement(text).jsonObject))
    }

    @Test
    fun parseRecord_isNullWithoutRecordUidTimeOrSeverity() {
        val json = OtlpLogEncoder.recordJson(record)
        val withoutUid = record.copy(attributes = record.attributes.take(2))

        assertNull(OtlpLogEncoder.parseRecord(OtlpLogEncoder.recordJson(withoutUid)))
        assertNull(OtlpLogEncoder.parseRecord(JsonObject(json - "timeUnixNano")))
        assertNull(OtlpLogEncoder.parseRecord(JsonObject(json - "severityNumber")))
        assertNull(OtlpLogEncoder.parseRecord(JsonObject(json + ("severityNumber" to JsonPrimitive(99)))))
        assertNull(OtlpLogEncoder.parseRecord(JsonObject(json + ("timeUnixNano" to JsonPrimitive("soon")))))
        assertNull(OtlpLogEncoder.parseRecord(JsonObject(emptyMap())))
    }

    @Test
    fun parseRecord_skipsAnAttributeOfAnUnknownShape() {
        val json = OtlpLogEncoder.recordJson(record)
        val odd = buildJsonObject {
            put("key", "paydock.widget")
            put("value", buildJsonObject { put("doubleValue", 1.5) })
        }
        val withOdd = JsonObject(json + ("attributes" to JsonArray(json.getValue("attributes").jsonArray + odd)))

        assertEquals(record, OtlpLogEncoder.parseRecord(withOdd))
    }

    @Test
    fun encode_writesTheResourceInSpecOrderAndOneScope() {
        val bytes = OtlpLogEncoder.encode(TelemetryTestFakes.resourceAttributes, listOf(record, record))
        val request = Json.parseToJsonElement(bytes.toString(Charsets.UTF_8)).jsonObject

        val resourceLogs = request.getValue("resourceLogs").jsonArray.single().jsonObject
        val resource = resourceLogs.getValue("resource").jsonObject.getValue("attributes").jsonArray
        assertEquals(Allowlist.RESOURCE_KEYS, resource.map { it.jsonObject.getValue("key").jsonPrimitive.content })
        val scope = resourceLogs.getValue("scopeLogs").jsonArray.single().jsonObject
        assertEquals(
            JsonPrimitive(OtlpLogEncoder.SCOPE_NAME),
            scope.getValue("scope").jsonObject["name"]
        )
        assertEquals(
            listOf(OtlpLogEncoder.recordJson(record), OtlpLogEncoder.recordJson(record)),
            scope.getValue("logRecords").jsonArray
        )
    }

    @Test
    fun encode_isDeterministic() {
        val first = OtlpLogEncoder.encode(TelemetryTestFakes.resourceAttributes, listOf(record))
        val second = OtlpLogEncoder.encode(TelemetryTestFakes.resourceAttributes, listOf(record))

        assertArrayEquals(first, second)
    }

    private companion object {
        const val SESSION_ID = "5d0c3b8e-2f71-4a96-8e04-6b1a9c7d2e53"
    }
}

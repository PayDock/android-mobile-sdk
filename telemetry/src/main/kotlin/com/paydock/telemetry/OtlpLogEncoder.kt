package com.paydock.telemetry

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * OTLP/HTTP JSON for log records (spec § 5, § 8), built with kotlinx.serialization's JsonElement builders and no
 * compiler plugin. Deterministic: the same resource and records give the same bytes. Resource attributes come out
 * in [Allowlist.RESOURCE_KEYS] order and record attributes in their record's order.
 */
internal object OtlpLogEncoder {
    const val SCOPE_NAME = "paydock.telemetry"

    private const val NANOS_PER_MILLI = 1_000_000L
    private const val RECORD_UID = "log.record.uid"
    private const val STRING_VALUE = "stringValue"
    private const val INT_VALUE = "intValue"
    private const val BOOL_VALUE = "boolValue"
    private const val ATTRIBUTES = "attributes"

    /** One export request: the resource once, then every record under one scope. UTF-8. */
    fun encode(resource: ResourceAttributes, records: List<TelemetryRecord>): ByteArray {
        val resourceValues = resource.asMap()
        val request = buildJsonObject {
            putJsonArray("resourceLogs") {
                addJsonObject {
                    putJsonObject("resource") {
                        putJsonArray(ATTRIBUTES) {
                            Allowlist.RESOURCE_KEYS.forEach { key ->
                                resourceValues[key]?.let { add(attribute(key, resourceValue(it))) }
                            }
                        }
                    }
                    putJsonArray("scopeLogs") {
                        addJsonObject {
                            putJsonObject("scope") { put("name", SCOPE_NAME) }
                            putJsonArray("logRecords") { records.forEach { add(recordJson(it)) } }
                        }
                    }
                }
            }
        }
        return Json.encodeToString(JsonElement.serializer(), request).toByteArray(Charsets.UTF_8)
    }

    /** One OTLP logRecord; also the content of the record's store file. */
    fun recordJson(record: TelemetryRecord): JsonObject = buildJsonObject {
        put("timeUnixNano", (record.timeUnixMillis * NANOS_PER_MILLI).toString())
        put("severityNumber", record.severity.number)
        put("severityText", record.severity.name)
        putJsonObject("body") { put(STRING_VALUE, record.body) }
        putJsonArray(ATTRIBUTES) {
            record.attributes.forEach { add(attribute(it.key, attributeValue(it.value))) }
        }
    }

    /**
     * The record of a [recordJson] object: the id from `log.record.uid`, the time from `timeUnixNano / 1_000_000`
     * and the severity from `severityNumber`. Null when any of them is missing; never throws. An attribute
     * of an unknown shape is skipped.
     */
    fun parseRecord(json: JsonObject): TelemetryRecord? = try {
        val attributes = (json[ATTRIBUTES] as? JsonArray).orEmpty().mapNotNull(::parseAttribute)
        val id = (attributes.firstOrNull { it.key == RECORD_UID }?.value as? AttributeValue.Str)?.value
        val nanos = (json["timeUnixNano"] as? JsonPrimitive)?.content?.toLongOrNull()
        val severityNumber = (json["severityNumber"] as? JsonPrimitive)?.intOrNull
        val severity = Severity.entries.firstOrNull { it.number == severityNumber }
        val body = ((json["body"] as? JsonObject)?.get(STRING_VALUE) as? JsonPrimitive)?.takeIf { it.isString }
        if (id == null || nanos == null || severity == null) {
            null
        } else {
            TelemetryRecord(id, nanos / NANOS_PER_MILLI, severity, body?.content.orEmpty(), attributes)
        }
    } catch (ignored: Exception) {
        null
    }

    private fun attribute(key: String, value: JsonObject): JsonObject = buildJsonObject {
        put("key", key)
        put("value", value)
    }

    private fun attributeValue(value: AttributeValue): JsonObject = buildJsonObject {
        when (value) {
            is AttributeValue.Str -> put(STRING_VALUE, value.value)
            is AttributeValue.Num -> put(INT_VALUE, value.value.toString())
            is AttributeValue.Bool -> put(BOOL_VALUE, value.value)
        }
    }

    /** The resource's documented types: Int for spec_version, Boolean for debuggable, String otherwise. */
    private fun resourceValue(value: Any): JsonObject = attributeValue(
        when (value) {
            is Boolean -> AttributeValue.Bool(value)
            is Int -> AttributeValue.Num(value.toLong())
            is Long -> AttributeValue.Num(value)
            else -> AttributeValue.Str(value.toString())
        }
    )

    private fun parseAttribute(element: JsonElement): Attribute? {
        val entry = element as? JsonObject
        val key = (entry?.get("key") as? JsonPrimitive)?.takeIf { it.isString }?.content
        val value = entry?.get("value") as? JsonObject
        if (key == null || value == null) return null
        val string = (value[STRING_VALUE] as? JsonPrimitive)?.takeIf { it.isString }?.content
        val number = (value[INT_VALUE] as? JsonPrimitive)?.content?.toLongOrNull()
        val bool = (value[BOOL_VALUE] as? JsonPrimitive)?.takeUnless { it.isString }?.booleanOrNull
        val parsed = when {
            string != null -> AttributeValue.Str(string)
            number != null -> AttributeValue.Num(number)
            bool != null -> AttributeValue.Bool(bool)
            else -> null
        }
        return parsed?.let { Attribute(key, it) }
    }
}

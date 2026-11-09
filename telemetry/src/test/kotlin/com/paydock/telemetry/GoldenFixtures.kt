package com.paydock.telemetry

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import java.io.File
import java.time.Instant

/**
 * The golden fixtures copied unchanged from observability-map `docs/mvp/fixtures` (spec § 9.2), and the mapping of
 * an `*.input.json` to the [PendingRecord] the reducer would have built for it.
 *
 * The working directory of a module's unit tests is the module directory, so the copies are read from
 * `src/test/resources/fixtures`.
 */
internal object GoldenFixtures {

    val root = File("src/test/resources/fixtures")

    /** Every `*.input.json` of `shared/` and `android/`, sorted by path. */
    fun inputs(): List<File> = listOf("shared", "android")
        .flatMap { folder -> File(root, folder).listFiles().orEmpty().toList() }
        .filter { it.name.endsWith(INPUT_SUFFIX) }
        .sortedBy { it.path }

    fun expectedFile(input: File): File =
        File(input.parentFile, input.name.removeSuffix(INPUT_SUFFIX) + ".android.expected.json")

    fun read(file: File): JsonObject = Json.parseToJsonElement(file.readText(Charsets.UTF_8)).jsonObject

    /** The resource of `context.android.json`; its fixed values must match [ResourceAttributes]' constants. */
    fun resource(): ResourceAttributes {
        val context = read(File(root, "context.android.json"))
        fun string(key: String) = context.getValue(key).jsonPrimitive.content
        assertEquals(ResourceAttributes.SERVICE_NAME, string("service.name"))
        assertEquals(ResourceAttributes.SERVICE_NAMESPACE, string("service.namespace"))
        assertEquals(ResourceAttributes.SDK_PLATFORM, string("paydock.sdk.platform"))
        assertEquals(
            ResourceAttributes.SPEC_VERSION,
            context.getValue("paydock.telemetry.spec_version").jsonPrimitive.int
        )
        assertEquals(ResourceAttributes.OS_NAME, string("os.name"))
        return ResourceAttributes(
            serviceVersion = string("service.version"),
            deploymentEnvironmentName = string("deployment.environment.name"),
            osVersion = string("os.version"),
            deviceManufacturer = string("device.manufacturer"),
            deviceModelIdentifier = string("device.model.identifier"),
            appInstallationId = string("app.installation.id"),
            appId = string("paydock.app.id"),
            appVersion = string("paydock.app.version"),
            appDebuggable = context.getValue("paydock.app.debuggable").jsonPrimitive.boolean
        )
    }

    fun connection(input: JsonObject): NetworkConnectionType {
        val value = input.getValue("connection").jsonPrimitive.content
        return NetworkConnectionType.entries.single { it.value == value }
    }

    /**
     * `id = "<sessionId>-<sequence>"` and `createdAt = builtTime`. An `exit_info` crash carries `time` as its own
     * time; any other input must have `time == builtTime`.
     */
    fun pendingRecord(input: JsonObject): PendingRecord {
        val sessionId = input.getValue("sessionId").jsonPrimitive.content
        val builtTime = millis(input.getValue("builtTime").jsonPrimitive.content)
        val time = input["time"]?.takeUnless { it is JsonNull }?.let { millis(it.jsonPrimitive.content) }
        val eventJson = input.getValue("event").jsonObject
        val isExitInfo = eventJson.string("type") == "Crash" && eventJson.string("source") == "exit_info"
        if (!isExitInfo && time != null && time != builtTime) {
            fail("a fixture without its own time must have time == builtTime: $input")
        }
        return PendingRecord(
            id = "$sessionId-${input.getValue("sequence").jsonPrimitive.long}",
            sessionId = sessionId,
            event = event(eventJson, occurredAt = if (isExitInfo) time else null),
            createdAt = builtTime
        )
    }

    /**
     * RecordFactory, then Allowlist.filter, then the send stamp at `sentTime` with `clockOffsetMs` (0 when absent),
     * then OtlpLogEncoder.encode: what the SDK sends for this input, as `generate.py` does.
     */
    fun encode(input: JsonObject, resource: ResourceAttributes = resource()): String {
        val record = Allowlist.filter(RecordFactory.create(pendingRecord(input), connection(input)))
        val sentAt = millis(input.getValue("sentTime").jsonPrimitive.content)
        val offset = input["clockOffsetMs"]?.jsonPrimitive?.long ?: 0L
        return OtlpLogEncoder.encode(resource, listOf(SendStamp.stamp(record, sentAt, offset))).toString(Charsets.UTF_8)
    }

    private fun millis(iso: String): Long = Instant.parse(iso).toEpochMilli()

    private fun event(json: JsonObject, occurredAt: Long?): TelemetryEvent = when (val type = json.string("type")) {
        "Crash" -> TelemetryEvent.Crash(
            error = json.error("error"),
            source = TelemetryEvent.CrashSource.entries.single { it.value == json.string("source") },
            kind = TelemetryEvent.CrashKind.entries.single { it.value == json.string("kind") },
            reason = json.string("reason"),
            threadName = json.string("thread"),
            occurredAt = occurredAt
        )
        "SdkError" -> TelemetryEvent.SdkError(
            error = json.error("error") ?: error("SdkError without error"),
            errorCode = json.string("code"),
            widget = json.string("widget")
        )
        "UncaughtException" -> TelemetryEvent.UncaughtException(
            error = json.error("error") ?: error("UncaughtException without error"),
            threadName = json.string("thread"),
            widget = json.string("widget")
        )
        "NetworkFailure" -> TelemetryEvent.NetworkFailure(
            method = TelemetryEvent.HttpMethod.valueOf(json.string("method")!!),
            urlTemplate = json.string("urlTemplate")!!,
            serverAddress = json.string("serverAddress")!!,
            failure = networkError(json.getValue("failure").jsonObject),
            attempt = json.getValue("attempt").jsonPrimitive.int,
            apiError = json["apiError"]?.jsonObject?.let(::apiError),
            widget = json.string("widget")
        )
        "WidgetOpened" -> TelemetryEvent.WidgetOpened(widget = json.string("widget")!!)
        "Dropped" -> TelemetryEvent.Dropped(
            reason = TelemetryEvent.DroppedReason.entries.single { it.value == json.string("reason") },
            count = json.getValue("count").jsonPrimitive.int
        )
        else -> error("unknown fixture event type $type")
    }

    private fun networkError(json: JsonObject): TelemetryEvent.NetworkError = when (val kind = json.string("kind")) {
        "http" -> TelemetryEvent.NetworkError.HttpStatus(json.getValue("status").jsonPrimitive.int)
        "decode" -> TelemetryEvent.NetworkError.Decode(
            statusCode = json["status"]?.jsonPrimitive?.int,
            error = json.error("error")
        )
        else -> TelemetryEvent.NetworkError.Transport(
            kind = TelemetryEvent.TransportKind.entries.single { it.value == kind },
            error = json.error("error")
        )
    }

    private fun apiError(json: JsonObject) = TelemetryEvent.ApiError(
        status = json["status"]?.jsonPrimitive?.int,
        code = json.string("code"),
        summaryCode = json.string("summaryCode"),
        gatewayCodes = (json["gatewayCodes"] as? JsonArray).orEmpty().map { it.jsonPrimitive.content }
    )

    private fun JsonObject.string(key: String): String? =
        this[key]?.takeUnless { it is JsonNull }?.jsonPrimitive?.content

    private fun JsonObject.error(key: String): TelemetryEvent.ErrorInfo? =
        this[key]?.takeUnless { it is JsonNull }?.jsonObject?.let { errorInfo(it) }

    private fun errorInfo(json: JsonObject): TelemetryEvent.ErrorInfo = TelemetryEvent.ErrorInfo(
        type = json.string("type"),
        frames = (json["frames"] as? JsonArray).orEmpty().map(JsonElement::jsonPrimitive).map { it.content },
        cause = json.error("cause")
    )

    private const val INPUT_SUFFIX = ".input.json"
}

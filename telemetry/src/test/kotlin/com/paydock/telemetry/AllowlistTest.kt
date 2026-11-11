package com.paydock.telemetry

import com.paydock.telemetry.AttributeValue.Bool
import com.paydock.telemetry.AttributeValue.Num
import com.paydock.telemetry.AttributeValue.Str
import com.paydock.telemetry.TelemetryEvent.ErrorInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The build-failing allowlist guard (R_02, R_03, principle 11). It encodes every golden fixture and a record of
 * every event variant, built from throwables whose messages hold a card number, an email and message text, and fails
 * on an unlisted key, a value of the wrong type, a sensitive value, a missing or malformed ID, or the message text
 * anywhere. Self-tests prove the scan reports each of these when it is injected.
 *
 * The card number is computed at run time from a non-issuer prefix, so no card-shaped literal is committed.
 */
internal class AllowlistTest {

    // ---- the guard ----

    @Test
    fun everyFixture_encodesOnlyAllowedKeysAndValues() {
        GoldenFixtures.inputs().forEach { input ->
            val json = GoldenFixtures.encode(GoldenFixtures.read(input))

            assertEquals(input.name, emptyList<String>(), OutputScan.violations(json))
        }
    }

    @Test
    fun everyEventVariant_encodesOnlyAllowedKeysAndValues() {
        val records = everyEventVariant().mapIndexed { index, event ->
            val pending = PendingRecord("$SESSION_ID-${index + 1}", SESSION_ID, event, createdAt = T0)
            Allowlist.filter(RecordFactory.create(pending, NetworkConnectionType.WIFI))
        }

        val json = OtlpLogEncoder.encode(resource, records).toString(Charsets.UTF_8)

        assertEquals(emptyList<String>(), OutputScan.violations(json))
        assertEquals(records.size, recordsOf(json).size)
        records.forEach { record ->
            assertEquals(SESSION_ID, record.sessionId)
            assertTrue(record.value("log.record.uid") is Str)
        }
    }

    @Test
    fun everyEventVariant_stampedLate_encodesOnlyAllowedKeysAndValues() {
        val records = everyEventVariant().mapIndexed { index, event ->
            val pending = PendingRecord("$SESSION_ID-${index + 1}", SESSION_ID, event, createdAt = T0)
            val record = Allowlist.filter(RecordFactory.create(pending, NetworkConnectionType.WIFI))
            SendStamp.stamp(record, sentAt = T0 + TelemetryLimits.ORDERING_WINDOW_MILLIS + 1, offsetMillis = 3_600_000)
        }

        val json = OtlpLogEncoder.encode(resource, records).toString(Charsets.UTF_8)

        assertEquals(emptyList<String>(), OutputScan.violations(json))
        records.forEach { assertTrue(it.value(SendStamp.OCCURRED_AT) is Str) }
    }

    // ---- self-tests: the scan reports every violation it guards against ----

    @Test
    fun scan_reportsAnUnlistedResourceKey() {
        val json = encode(validRecord()).replace("\"device.model.identifier\"", "\"device.serial.number\"")

        assertReported("unlisted resource key device.serial.number", json)
    }

    @Test
    fun scan_reportsAnUnlistedRecordKey() {
        assertReported("unlisted record key user.name", encode(validRecord().plus("user.name", Str("someone"))))
    }

    @Test
    fun scan_reportsAValueOfTheWrongType() {
        assertReported(
            "wrong type for paydock.http.attempt",
            encode(validRecord().plus("paydock.http.attempt", Str("1")))
        )
        assertReported(
            "wrong type for paydock.app.debuggable",
            encode(validRecord()).replace("{\"boolValue\":true}", "{\"stringValue\":\"true\"}")
        )
    }

    @Test
    fun scan_reportsASensitiveValue() {
        assertReported("sensitive paydock.error.code", encode(validRecord().plus("paydock.error.code", Str(EMAIL))))
        assertReported(
            "sensitive paydock.error.code",
            encode(validRecord().plus("paydock.error.code", Str("declined $cardNumber at checkout")))
        )
        assertReported("sensitive body", encode(validRecord().copy(body = "paydock.error · $EMAIL")))
    }

    @Test
    fun scan_reportsAMissingOrMalformedInstallationId() {
        assertReported(
            "missing app.installation.id",
            encode(validRecord()).replace("\"app.installation.id\"", "\"service.instance.id\"")
        )
        assertReported(
            "malformed app.installation.id",
            encode(validRecord(), resource.copy(appInstallationId = "installation-test-0001"))
        )
    }

    @Test
    fun scan_reportsARecordWithoutSessionIdOrRecordUid() {
        val record = validRecord()

        assertReported(
            "record without session.id",
            encode(record.copy(attributes = record.attributes.filterNot { it.key == "session.id" }))
        )
        assertReported(
            "record without log.record.uid",
            encode(record.copy(attributes = record.attributes.filterNot { it.key == "log.record.uid" }))
        )
        assertReported("malformed session.id", encode(record.with("session.id", Str("session-1"))))
        assertReported("malformed log.record.uid", encode(record.with("log.record.uid", Str("$SESSION_ID-0"))))
    }

    @Test
    fun scan_reportsTheMessageTextAnywhere() {
        assertReported("message text", encode(validRecord().copy(body = "paydock.error · $SECRET_TEXT")))
    }

    // ---- matching ----

    @Test
    fun isSensitive_catchesACardNumberInsideALongerValue() {
        val spaced = cardNumber.chunked(4).joinToString(" ")
        val dashed = cardNumber.chunked(4).joinToString("-")

        assertTrue(Allowlist.isSensitive(cardNumber))
        assertTrue(Allowlist.isSensitive("payment for $cardNumber was declined"))
        assertTrue(Allowlist.isSensitive("card=[$spaced]"))
        assertTrue(Allowlist.isSensitive("pan:$dashed;"))
    }

    @Test
    fun isSensitive_ignoresDigitRunsThatAreNotCardNumbers() {
        val failsLuhn = cardNumber.dropLast(1) + ((cardNumber.last() - '0' + 1) % 10)

        assertFalse(Allowlist.isSensitive(failsLuhn))
        assertFalse(Allowlist.isSensitive("x$cardNumber")) // touches a letter
        assertFalse(Allowlist.isSensitive("$cardNumber-1234")) // one run of 20 digits
        assertFalse(Allowlist.isSensitive("CardDetailsViewModel.tokeniseCard(CardDetailsViewModel.kt:142)"))
        assertFalse(Allowlist.isSensitive("/v1/gateways/{id}/wallet-config"))
        assertFalse(Allowlist.isSensitive("INVALID_REQUEST:INVALID:sourceOfFunds.provided.card.securityCode"))
    }

    @Test
    fun isSensitive_catchesEmailJwtAuthorizationAndLongSecrets() {
        assertTrue(Allowlist.isSensitive("contact $EMAIL"))
        assertTrue(Allowlist.isSensitive("token eyJhbGciOi.eyJzdWIiOi.c2lnbmF0dXJl"))
        assertTrue(Allowlist.isSensitive("Authorization: Bearer abcdefgh12345678"))
        assertTrue(Allowlist.isSensitive("basic  c3ludGhldGljOnZhbHVl"))
        assertTrue(Allowlist.isSensitive("key=" + "a1".repeat(20)))
        assertFalse(Allowlist.isSensitive("a".repeat(60))) // no digit
        assertFalse(Allowlist.isSensitive("1".repeat(12) + "-" + "2".repeat(12))) // no letter, not a card run
    }

    @Test
    fun luhnPassingUuidIds_areKeptByTheFilterAndTheScan() {
        val (sessionId, recordUid) = luhnPassingIds()
        assertTrue("the session.id run must pass Luhn", Allowlist.isSensitive(sessionId))
        assertTrue("the log.record.uid run must pass Luhn", Allowlist.isSensitive(recordUid))
        val record = validRecord(sessionId = sessionId, recordUid = recordUid)

        val filtered = Allowlist.filter(record)

        assertEquals(record, filtered)
        assertEquals(emptyList<String>(), OutputScan.violations(encode(filtered)))
    }

    // ---- the runtime filter ----

    @Test
    fun filter_dropsUnlistedKeysWrongTypesAndSensitiveValues() {
        val record = validRecord()
            .plus("user.email", Str("someone"))
            .plus("paydock.http.attempt", Str("1"))
            .plus("paydock.error.code", Str("declined $cardNumber"))
            .plus("thread.name", Str(EMAIL))

        assertEquals(validRecord(), Allowlist.filter(record))
    }

    @Test
    fun filter_dropsAMalformedSessionIdOrRecordUid() {
        val badSession = Allowlist.filter(validRecord().with("session.id", Str("not-a-uuid")))
        val otherSession = Allowlist.filter(validRecord().with("log.record.uid", Str("$OTHER_SESSION_ID-1")))
        val zeroSequence = Allowlist.filter(validRecord().with("log.record.uid", Str("$SESSION_ID-0")))

        assertEquals(null, badSession.sessionId)
        assertEquals(null, badSession.value("log.record.uid"))
        assertEquals(null, otherSession.value("log.record.uid"))
        assertEquals(null, zeroSequence.value("log.record.uid"))
        assertEquals(Str(SESSION_ID), zeroSequence.value("session.id"))
    }

    @Test
    fun filter_keepsTheFirstOfARepeatedKey() {
        val record = validRecord()
        val repeated = record.copy(attributes = record.attributes + Attribute("paydock.widget", Str("Other")))

        assertEquals(Str("CardDetailsWidget"), Allowlist.filter(repeated).value("paydock.widget"))
    }

    // ---- the runtime filter: the body carries only what the filter keeps ----

    @Test
    fun filter_rebuildsTheBody_withoutASensitiveErrorCode() {
        val error = ErrorInfo("java.lang.IllegalStateException", emptyList())

        val filtered = filtered(TelemetryEvent.SdkError(error, errorCode = EMAIL))

        assertEquals(null, filtered.value("paydock.error.code"))
        assertEquals("paydock.error · sdk_error · java.lang.IllegalStateException", filtered.body)
        assertFalse(filtered.body.contains("@"))
        assertBodyIsClean(filtered)
    }

    @Test
    fun filter_rebuildsTheBody_withoutASensitiveCrashReason() {
        val event = TelemetryEvent.Crash(
            ErrorInfo("java.lang.Error", emptyList()),
            TelemetryEvent.CrashSource.EXIT_INFO,
            TelemetryEvent.CrashKind.NATIVE,
            reason = "declined $cardNumber"
        )

        val filtered = filtered(event)

        assertEquals(null, filtered.value("paydock.crash.reason"))
        assertEquals("paydock.crash · native · java.lang.Error", filtered.body)
        assertBodyIsClean(filtered)
    }

    @Test
    fun filter_rebuildsTheBody_withoutASensitiveUrlTemplate() {
        val event = TelemetryEvent.NetworkFailure(
            method = TelemetryEvent.HttpMethod.GET,
            urlTemplate = "/v1/customers/$EMAIL",
            serverAddress = "api-sandbox.paydock.com",
            failure = TelemetryEvent.NetworkError.HttpStatus(404),
            attempt = 1
        )

        val filtered = filtered(event)

        assertEquals(null, filtered.value("url.template"))
        assertEquals(null, filtered.value("paydock.group.key"))
        assertEquals("paydock.network.failure · http_4xx 404", filtered.body)
        assertBodyIsClean(filtered)
    }

    /** R_05, R_06: a template that is not made of literal words, versions and placeholders is dropped with its key. */
    @Test
    fun filter_dropsATemplateWithAnIdNumberQueryOrFragment_withTheGroupKey() {
        listOf(
            "/v1/charges/wallet/0000aaaa1111bbbb2222cccc/decline",
            "/v1/gateways/12345/wallet-config",
            "/v1/charges/wallet/callback?mobile=true",
            "/v1/payment_sources/tokens#section",
            "/v1/Payment_Sources/tokens",
            "v1/payment_sources/tokens",
            "/v1//tokens",
            "",
        ).forEach { template ->
            val filtered = filtered(networkFailure(template))

            assertEquals(template, null, filtered.value("url.template"))
            assertEquals(template, null, filtered.value("paydock.group.key"))
            assertEquals(template, "paydock.network.failure · http_4xx 400", filtered.body)
            assertBodyIsClean(filtered)
        }
    }

    @Test
    fun filter_keepsATemplateOfLiteralsVersionsAndPlaceholders_withTheGroupKey() {
        listOf(
            "/",
            "/v1/payment_sources/tokens",
            "/v1/payment_sources/setup-tokens/{id}/tokens",
            "/v1/gateways/{id}/wallet-config",
            "/v2/{id}/{id}",
        ).forEach { template ->
            val filtered = filtered(networkFailure(template))

            assertEquals(template, Str(template), filtered.value("url.template"))
            assertEquals(template, Str("http POST $template 400"), filtered.value("paydock.group.key"))
            assertBodyIsClean(filtered)
        }
    }

    @Test
    fun filter_dropsAMalformedGroupKey_andKeepsAValidOne() {
        val record = filtered(networkFailure("/v1/payment_sources/tokens"))
        val malformed = listOf(
            "http POST /v1/payment_sources/0000aaaa1111bbbb2222cccc 400",
            "http POST /v1/payment_sources/tokens?x=1 400",
            "http post /v1/payment_sources/tokens 400",
            "http POST /v1/payment_sources/tokens 4000",
            "http POST /v1/payment_sources/tokens",
            "POST /v1/payment_sources/tokens 400",
        )
        malformed.forEach { key ->
            val withKey = record.copy(
                attributes = record.attributes.filterNot { it.key == "paydock.group.key" } +
                    Attribute("paydock.group.key", Str(key))
            )
            assertEquals(key, null, Allowlist.filter(withKey).value("paydock.group.key"))
        }
        assertEquals(Str("http POST /v1/payment_sources/tokens 400"), record.value("paydock.group.key"))
    }

    @Test
    fun filter_rebuildsTheBody_withoutASensitiveWidgetName() {
        val filtered = filtered(TelemetryEvent.WidgetOpened("Widget $cardNumber"))

        assertEquals(null, filtered.value("paydock.widget"))
        assertEquals("paydock.widget.opened", filtered.body)
        assertBodyIsClean(filtered)
    }

    @Test
    fun filter_bodyIsAlwaysTheBodyOfTheKeptAttributes() {
        val withSensitiveFields = listOf(
            TelemetryEvent.SdkError(ErrorInfo("java.lang.IllegalStateException", emptyList()), errorCode = EMAIL),
            TelemetryEvent.WidgetOpened(EMAIL)
        )
        (everyEventVariant() + withSensitiveFields).forEachIndexed { index, event ->
            val filtered = filtered(event, sequence = index + 1)

            assertEquals(event.toString(), RecordBody.of(filtered.attributes), filtered.body)
            assertFalse(event.toString(), Allowlist.isSensitive(filtered.body))
        }
    }

    @Test
    fun keys_matchTheSpecLists() {
        assertEquals(RESOURCE_TYPES.keys.toList(), Allowlist.RESOURCE_KEYS)
        assertEquals(30, Allowlist.RECORD_KEYS.size)
        assertEquals(Num::class, Allowlist.RECORD_KEYS["paydock.dropped.count"])
        assertEquals(Bool::class, Allowlist.RECORD_KEYS["paydock.exception.in_sdk"])
        assertEquals(Str::class, Allowlist.RECORD_KEYS[SendStamp.OCCURRED_AT])
        val order = Allowlist.RECORD_KEYS.keys.toList()
        assertEquals(order.indexOf("paydock.group.key") + 1, order.indexOf(SendStamp.OCCURRED_AT))
    }

    /** D20: no per-device stream label, so `service.instance.id` is listed nowhere and the scan reports it. */
    @Test
    fun serviceInstanceId_isNeverAllowed() {
        assertFalse(SERVICE_INSTANCE_ID in Allowlist.RESOURCE_KEYS)
        assertFalse(SERVICE_INSTANCE_ID in RESOURCE_TYPES)
        assertFalse(Allowlist.RECORD_KEYS.containsKey(SERVICE_INSTANCE_ID))

        val asResource = encode(validRecord()).replace("\"device.model.identifier\"", "\"$SERVICE_INSTANCE_ID\"")
        assertReported("unlisted resource key $SERVICE_INSTANCE_ID", asResource)
        assertReported(
            "unlisted record key $SERVICE_INSTANCE_ID",
            encode(validRecord().plus(SERVICE_INSTANCE_ID, Str(INSTALLATION_ID)))
        )
        val filtered = Allowlist.filter(validRecord().plus(SERVICE_INSTANCE_ID, Str(INSTALLATION_ID)))
        assertEquals(null, filtered.value(SERVICE_INSTANCE_ID))
    }

    // ---- helpers ----

    /** The record the SDK stores for this event: built by [RecordFactory], then filtered. */
    private fun filtered(event: TelemetryEvent, sequence: Int = 1): TelemetryRecord = Allowlist.filter(
        RecordFactory.create(
            PendingRecord("$SESSION_ID-$sequence", SESSION_ID, event, createdAt = T0),
            NetworkConnectionType.WIFI
        )
    )

    private fun assertBodyIsClean(filtered: TelemetryRecord) {
        assertFalse("sensitive body", Allowlist.isSensitive(filtered.body))
        assertEquals(RecordBody.of(filtered.attributes), filtered.body)
        assertEquals(emptyList<String>(), OutputScan.violations(encode(filtered)))
    }

    private fun assertReported(expected: String, json: String) {
        val violations = OutputScan.violations(json)
        assertTrue("expected \"$expected\" in $violations", violations.any { it.startsWith(expected) })
    }

    private fun encode(record: TelemetryRecord, resource: ResourceAttributes = this.resource): String =
        OtlpLogEncoder.encode(resource, listOf(record)).toString(Charsets.UTF_8)

    private fun networkFailure(template: String) = TelemetryEvent.NetworkFailure(
        method = TelemetryEvent.HttpMethod.POST,
        urlTemplate = template,
        serverAddress = "api-sandbox.paydock.com",
        failure = TelemetryEvent.NetworkError.HttpStatus(400)
    )

    /** Appends an attribute as is, unfiltered and unordered, so injected violations reach the encoder. */
    private fun TelemetryRecord.plus(key: String, value: AttributeValue) =
        copy(attributes = attributes + Attribute(key, value))

    private fun validRecord(sessionId: String = SESSION_ID, recordUid: String = "$SESSION_ID-1") = TelemetryRecord(
        id = recordUid,
        timeUnixMillis = T0,
        severity = Severity.INFO,
        body = "paydock.widget.opened · CardDetailsWidget",
        attributes = listOf(
            Attribute("event.name", Str("paydock.widget.opened")),
            Attribute("session.id", Str(sessionId)),
            Attribute("log.record.uid", Str(recordUid)),
            Attribute("network.connection.type", Str("wifi")),
            Attribute("paydock.widget", Str("CardDetailsWidget"))
        )
    )

    private fun recordsOf(json: String) = Json.parseToJsonElement(json).jsonObject.getValue("resourceLogs").jsonArray
        .flatMap { it.jsonObject.getValue("scopeLogs").jsonArray }
        .flatMap { it.jsonObject.getValue("logRecords").jsonArray }

    /** Every event variant, its exceptions built by [ErrorInfo.from] on throwables that carry sensitive messages. */
    private fun everyEventVariant(): List<TelemetryEvent> {
        val error = ErrorInfo.from(
            IllegalStateException("charge $cardNumber for $EMAIL failed: $SECRET_TEXT").apply {
                initCause(IllegalArgumentException("$SECRET_TEXT $cardNumber"))
                addSuppressed(UnsupportedOperationException(EMAIL))
            }
        )
        val crashes = TelemetryEvent.CrashKind.entries.flatMap { kind ->
            TelemetryEvent.CrashSource.entries.map { source ->
                TelemetryEvent.Crash(error, source, kind, reason = "CRASH_NATIVE", threadName = "main", occurredAt = T0)
            }
        } + TelemetryEvent.Crash(error = null, TelemetryEvent.CrashSource.EXIT_INFO, TelemetryEvent.CrashKind.OS_KILL)
        val failures = listOf(
            TelemetryEvent.NetworkError.HttpStatus(422),
            TelemetryEvent.NetworkError.HttpStatus(503),
            TelemetryEvent.NetworkError.Decode(statusCode = 200, error = error),
            TelemetryEvent.NetworkError.Decode()
        ) + TelemetryEvent.TransportKind.entries.map { TelemetryEvent.NetworkError.Transport(it, error) }
        val networkFailures = failures.map { failure ->
            TelemetryEvent.NetworkFailure(
                method = TelemetryEvent.HttpMethod.POST,
                urlTemplate = "/v1/payment_sources/tokens",
                serverAddress = "api-sandbox.paydock.com",
                failure = failure,
                attempt = 2,
                apiError = TelemetryEvent.ApiError(422, "validation_error", "invalid_card", listOf("GATEWAY:DECLINED")),
                widget = "CardDetailsWidget"
            )
        }
        return crashes + networkFailures + listOf(
            TelemetryEvent.UncaughtException(error, threadName = "DefaultDispatcher-worker-1", widget = "PayPalWidget"),
            TelemetryEvent.SdkError(error, errorCode = "TokenisingCardException", widget = "CardDetailsWidget"),
            TelemetryEvent.SdkError(ErrorInfo(type = null, frames = emptyList())),
            TelemetryEvent.WidgetOpened("CardDetailsWidget")
        ) + TelemetryEvent.DroppedReason.entries.map { TelemetryEvent.Dropped(it, 3) }
    }

    /**
     * A session.id whose last group is all digits and whose digit run passes Luhn, and a log.record.uid of it whose
     * longer run passes Luhn too: IDs the sensitive-value scan would flag, which the format checks keep.
     */
    private fun luhnPassingIds(): Pair<String, String> = (0L until 1_000_000L).asSequence()
        .map { n -> "abcdefab-cdef-4abc-8000-" + "%012d".format(n) }
        .map { it to "$it-7" }
        .first { (session, uid) -> Allowlist.isSensitive(session) && Allowlist.isSensitive(uid) }

    /** Scans encoded OTLP JSON; every violation is a line starting with what was found. */
    private object OutputScan {
        private val uuid = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
        private val recordUid = Regex("(${uuid.pattern})-[1-9][0-9]*")

        fun violations(json: String): List<String> {
            val found = mutableListOf<String>()
            if (json.contains(SECRET_TEXT)) found += "message text"
            Json.parseToJsonElement(json).jsonObject.getValue("resourceLogs").jsonArray.forEach { resourceLogs ->
                val resource = resourceLogs.jsonObject.getValue("resource").jsonObject
                scanResource(resource.getValue("attributes").jsonArray.map { it.jsonObject }, found)
                resourceLogs.jsonObject.getValue("scopeLogs").jsonArray
                    .flatMap { it.jsonObject.getValue("logRecords").jsonArray }
                    .forEach { scanRecord(it.jsonObject, found) }
            }
            return found
        }

        private fun scanResource(attributes: List<JsonObject>, found: MutableList<String>) {
            val values = attributes.associate { key(it) to valueOf(it) }
            values.forEach { (key, value) ->
                val expected = RESOURCE_TYPES[key]
                when {
                    expected == null -> found += "unlisted resource key $key"
                    value.first != expected -> found += "wrong type for $key: ${value.first}"
                    key != "app.installation.id" && value.first == "stringValue" &&
                        Allowlist.isSensitive(value.second) -> found += "sensitive $key"
                }
            }
            val installationId = values["app.installation.id"]?.second
            when {
                installationId == null -> found += "missing app.installation.id"
                !uuid.matches(installationId) -> found += "malformed app.installation.id"
            }
        }

        private fun scanRecord(record: JsonObject, found: MutableList<String>) {
            val body = record["body"]?.jsonObject?.get("stringValue")?.let { (it as JsonPrimitive).content }.orEmpty()
            if (Allowlist.isSensitive(body)) found += "sensitive body"
            val values = record.getValue("attributes").jsonArray
                .map { it.jsonObject }
                .associate { key(it) to valueOf(it) }
            values.forEach { (key, value) ->
                val expected = Allowlist.RECORD_KEYS[key]?.let { RECORD_TYPES.getValue(it) }
                when {
                    expected == null -> found += "unlisted record key $key"
                    value.first != expected -> found += "wrong type for $key: ${value.first}"
                    key !in FORMAT_CHECKED && value.first == "stringValue" &&
                        Allowlist.isSensitive(value.second) -> found += "sensitive $key"
                }
            }
            val sessionId = values["session.id"]?.second
            val uid = values["log.record.uid"]?.second
            when {
                sessionId == null -> found += "record without session.id"
                !uuid.matches(sessionId) -> found += "malformed session.id"
            }
            when {
                uid == null -> found += "record without log.record.uid"
                recordUid.matchEntire(uid)?.groupValues?.get(1) != sessionId -> found += "malformed log.record.uid"
            }
        }

        private fun key(attribute: JsonObject) = (attribute.getValue("key") as JsonPrimitive).content

        /** The value's single OTLP form and its content. */
        private fun valueOf(attribute: JsonObject): Pair<String, String> {
            val value = attribute.getValue("value").jsonObject
            val form = value.keys.singleOrNull() ?: "invalid ${value.keys}"
            return form to ((value[form] as? JsonPrimitive)?.content ?: "")
        }
    }

    private val cardNumber: String = luhnComplete("990000000000123")

    /** Appends the Luhn check digit. */
    private fun luhnComplete(payload: String): String {
        val sum = payload.reversed().mapIndexed { index, char ->
            val digit = char - '0'
            if (index % 2 == 0) (digit * 2).let { if (it > 9) it - 9 else it } else digit
        }.sum()
        return payload + (10 - sum % 10) % 10
    }

    private val resource = TelemetryTestFakes.resourceAttributes.copy(appInstallationId = INSTALLATION_ID)

    private companion object {
        const val T0 = 1_793_852_047_512L
        const val SESSION_ID = "5d0c3b8e-2f71-4a96-8e04-6b1a9c7d2e53"
        const val OTHER_SESSION_ID = "b2f6c0a4-1d3e-4e8f-9a71-5c2d8e0f4b13"
        const val INSTALLATION_ID = "3f9c6b1e-7a42-4d0b-9e15-2c8a5d71b3e4"
        const val EMAIL = "someone@example.com"
        const val SERVICE_INSTANCE_ID = "service.instance.id"
        const val SECRET_TEXT = "secret-message-text"

        val FORMAT_CHECKED = setOf("session.id", "log.record.uid")

        val RESOURCE_TYPES: Map<String, String> = linkedMapOf(
            "service.name" to "stringValue",
            "service.namespace" to "stringValue",
            "service.version" to "stringValue",
            "paydock.sdk.platform" to "stringValue",
            "paydock.telemetry.spec_version" to "intValue",
            "deployment.environment.name" to "stringValue",
            "os.name" to "stringValue",
            "os.version" to "stringValue",
            "device.manufacturer" to "stringValue",
            "device.model.identifier" to "stringValue",
            "app.installation.id" to "stringValue",
            "paydock.app.id" to "stringValue",
            "paydock.app.version" to "stringValue",
            "paydock.app.debuggable" to "boolValue"
        )

        val RECORD_TYPES = mapOf(Str::class to "stringValue", Num::class to "intValue", Bool::class to "boolValue")
    }
}

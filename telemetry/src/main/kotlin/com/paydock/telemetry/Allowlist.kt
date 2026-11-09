package com.paydock.telemetry

import kotlin.reflect.KClass

/**
 * The only keys a batch may carry (spec § 5.1 to § 5.8, principle 11), and the run-time half of their
 * enforcement: [filter] drops an unlisted key, a value of the wrong type and a sensitive-looking value from every
 * record before it is stored. `AllowlistTest` is the build-time half.
 */
internal object Allowlist {

    /** The 14 § 5.1 resource keys, in § 5.1 order. */
    val RESOURCE_KEYS: List<String> = listOf(
        "service.name",
        "service.namespace",
        "service.version",
        "paydock.sdk.platform",
        "paydock.telemetry.spec_version",
        "deployment.environment.name",
        "os.name",
        "os.version",
        "device.manufacturer",
        "device.model.identifier",
        "app.installation.id",
        "paydock.app.id",
        "paydock.app.version",
        "paydock.app.debuggable"
    )

    /** The record keys, in encoding order (the fixtures' `ORDER`), each with its value type. */
    val RECORD_KEYS: Map<String, KClass<out AttributeValue>> = linkedMapOf(
        // Common
        "event.name" to AttributeValue.Str::class,
        "session.id" to AttributeValue.Str::class,
        "log.record.uid" to AttributeValue.Str::class,
        "network.connection.type" to AttributeValue.Str::class,
        "paydock.widget" to AttributeValue.Str::class,
        "paydock.occurrences" to AttributeValue.Num::class,
        "paydock.group.key" to AttributeValue.Str::class,
        "paydock.occurred_at" to AttributeValue.Str::class,
        // Exception
        "exception.type" to AttributeValue.Str::class,
        "exception.stacktrace" to AttributeValue.Str::class,
        "paydock.exception.cause_types" to AttributeValue.Str::class,
        "paydock.exception.in_sdk" to AttributeValue.Bool::class,
        "paydock.error.kind" to AttributeValue.Str::class,
        "paydock.error.code" to AttributeValue.Str::class,
        "paydock.crash.kind" to AttributeValue.Str::class,
        "paydock.crash.source" to AttributeValue.Str::class,
        "paydock.crash.reason" to AttributeValue.Str::class,
        "thread.name" to AttributeValue.Str::class,
        // Network
        "http.request.method" to AttributeValue.Str::class,
        "url.template" to AttributeValue.Str::class,
        "server.address" to AttributeValue.Str::class,
        "http.response.status_code" to AttributeValue.Num::class,
        "error.type" to AttributeValue.Str::class,
        "paydock.api.status" to AttributeValue.Num::class,
        "paydock.api.error.code" to AttributeValue.Str::class,
        "paydock.api.error.summary_code" to AttributeValue.Str::class,
        "paydock.api.gateway.codes" to AttributeValue.Str::class,
        "paydock.http.attempt" to AttributeValue.Num::class,
        // Dropped
        "paydock.dropped.reason" to AttributeValue.Str::class,
        "paydock.dropped.count" to AttributeValue.Num::class
    )

    private const val SESSION_ID = "session.id"
    private const val RECORD_UID = "log.record.uid"
    private const val UUID_PATTERN = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
    private const val CARD_MIN_DIGITS = 13
    private const val CARD_MAX_DIGITS = 19
    private const val LUHN_MODULUS = 10
    private const val LUHN_DOUBLE_OVER = 9

    private val UUID = Regex(UUID_PATTERN)
    private val RECORD_UID_FORMAT = Regex("""($UUID_PATTERN)-[1-9]\d{0,18}""")
    private val DIGIT_RUN = Regex("""\d(?:[ -]?\d)*""")
    private val EMAIL = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
    private val JWT = Regex("""eyJ[A-Za-z0-9_-]{5,}\.[A-Za-z0-9_-]{5,}\.""")
    private val AUTHORIZATION = Regex("""(bearer|basic)\s+[A-Za-z0-9+/=._-]{8,}""", RegexOption.IGNORE_CASE)
    private val SECRET_RUN = Regex("""[A-Za-z0-9+/=]{40,}""")

    /**
     * Keeps the attributes whose key is listed, whose value has the key's type and, for a string, passes its
     * check: a UUID `session.id`, a `log.record.uid` of that session and a positive sequence, and no sensitive value
     * for every other key. A repeated key keeps its first value. The body is then rebuilt by [RecordBody.of] from
     * the kept attributes alone, so a dropped value never survives in it. Never throws: on a failure it keeps no
     * attribute and a blank body, so the record is then rejected for its missing `session.id`.
     */
    fun filter(record: TelemetryRecord): TelemetryRecord = try {
        val kept = record.attributes.distinctBy { it.key }.filter { isAllowed(it, record.sessionId) }
        record.copy(body = RecordBody.of(kept), attributes = kept)
    } catch (ignored: Throwable) {
        record.copy(body = "", attributes = emptyList())
    }

    /**
     * True when the value holds, anywhere inside it, a card number (13 to 19 digits, single spaces or dashes
     * allowed between them, touching no other digit or letter, passing Luhn), an email address, a JWT, an
     * authorization value or a long secret (40 or more base64 characters holding a letter and a digit).
     */
    fun isSensitive(value: String): Boolean =
        containsCardNumber(value) ||
            EMAIL.containsMatchIn(value) ||
            JWT.containsMatchIn(value) ||
            AUTHORIZATION.containsMatchIn(value) ||
            containsLongSecret(value)

    private fun isAllowed(attribute: Attribute, sessionId: String?): Boolean {
        val type = RECORD_KEYS[attribute.key]
        val text = (attribute.value as? AttributeValue.Str)?.value
        return when {
            type == null || !type.isInstance(attribute.value) -> false
            text == null -> true
            attribute.key == SESSION_ID -> UUID.matches(text)
            attribute.key == RECORD_UID -> RECORD_UID_FORMAT.matchEntire(text)?.groupValues?.get(1) == sessionId
            else -> !isSensitive(text)
        }
    }

    /** Each maximal run of digits, single spaces and dashes is one candidate; a run touching a letter is not. */
    private fun containsCardNumber(value: String): Boolean = DIGIT_RUN.findAll(value).any { run ->
        val before = value.getOrNull(run.range.first - 1)
        val after = value.getOrNull(run.range.last + 1)
        val digits = run.value.filter { it.isDigit() }
        !isAsciiLetterOrDigit(before) &&
            !isAsciiLetterOrDigit(after) &&
            digits.length in CARD_MIN_DIGITS..CARD_MAX_DIGITS &&
            passesLuhn(digits)
    }

    private fun containsLongSecret(value: String): Boolean = SECRET_RUN.findAll(value).any { run ->
        run.value.any { it.isLetter() } && run.value.any { it.isDigit() }
    }

    private fun isAsciiLetterOrDigit(char: Char?): Boolean =
        char != null && (char in 'a'..'z' || char in 'A'..'Z' || char in '0'..'9')

    private fun passesLuhn(digits: String): Boolean {
        val sum = digits.reversed().mapIndexed { index, char ->
            val digit = char - '0'
            if (index % 2 == 1) (digit * 2).let { if (it > LUHN_DOUBLE_OVER) it - LUHN_DOUBLE_OVER else it } else digit
        }.sum()
        return sum % LUHN_MODULUS == 0
    }
}

package com.paydock.telemetry

/**
 * The one body rule (spec § 5.2, `generate.py` of the golden fixtures): `"<event.name> · <details>"`, read only from
 * a record's own attributes. [RecordFactory] builds the body with it, and [Allowlist.filter] rebuilds it from the
 * attributes it keeps, so a value the filter drops never reaches the gateway through the body. A detail whose
 * attribute is absent is left out; an unknown event name gives the name alone, and no `event.name` gives `""`.
 */
internal object RecordBody {
    private const val SEPARATOR = " · "

    fun of(attributes: List<Attribute>): String {
        val values = attributes.distinctBy { it.key }.associate { it.key to it.value }
        val name = values.text("event.name") ?: return ""
        val details = when (name) {
            "paydock.crash" -> crashDetails(values)
            "paydock.error" -> errorDetails(values)
            "paydock.network.failure" -> networkDetails(values)
            "paydock.widget.opened" -> listOfNotNull(values.text("paydock.widget"))
            "paydock.telemetry.dropped" -> listOfNotNull(
                values.text("paydock.dropped.reason"),
                values.number("paydock.dropped.count")?.let { "$it records" }
            )
            else -> emptyList()
        }
        return (listOf(name) + details).joinToString(SEPARATOR)
    }

    /** The kind, then the reason or else the exception type, left out when empty or equal to the kind. */
    private fun crashDetails(values: Map<String, AttributeValue>): List<String> {
        val kind = values.text("paydock.crash.kind")
        val detail = (values.text("paydock.crash.reason") ?: values.text("exception.type"))
            ?.takeIf { it.isNotEmpty() && !it.equals(kind, ignoreCase = true) }
        return listOfNotNull(kind, detail)
    }

    /** The kind, then the error code or else the exception type. */
    private fun errorDetails(values: Map<String, AttributeValue>): List<String> = listOfNotNull(
        values.text("paydock.error.kind"),
        values.text("paydock.error.code") ?: values.text("exception.type")
    )

    /** `"<error.type>[ <status>]"`, then `"<method> <url.template>"` when both are present. */
    private fun networkDetails(values: Map<String, AttributeValue>): List<String> {
        val failure = values.text("error.type")?.let { type ->
            type + (values.number("http.response.status_code")?.let { " $it" } ?: "")
        }
        val method = values.text("http.request.method")
        val template = values.text("url.template")
        val request = if (method != null && template != null) "$method $template" else null
        return listOfNotNull(failure, request)
    }

    private fun Map<String, AttributeValue>.text(key: String): String? = (this[key] as? AttributeValue.Str)?.value

    private fun Map<String, AttributeValue>.number(key: String): Long? = (this[key] as? AttributeValue.Num)?.value
}

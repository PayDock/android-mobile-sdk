package com.paydock.core.telemetry

/** The url.template of every SDK endpoint on the Paydock API client (R_05). The MR description lists TEMPLATES. */
internal object RouteTemplates {

    private const val PLACEHOLDER = "{id}"
    private const val ROOT = "/"
    private val VERSION = Regex("""v[0-9]+""")

    /** The eight templates, in this order. */
    val TEMPLATES: List<String> = listOf(
        "/v1/payment_sources/tokens",
        "/v1/payment_sources/external_checkout",
        "/v1/payment_sources/setup-tokens",
        "/v1/payment_sources/setup-tokens/{id}/tokens",
        "/v1/charges/wallet/capture",
        "/v1/charges/wallet/{id}/decline",
        "/v1/charges/wallet/callback",
        "/v1/gateways/{id}/wallet-config",
    )

    private val templateSegments: List<Pair<String, List<String>>> = TEMPLATES.map { it to segmentsOf(it) }

    /** Every literal segment of a template: the only request segments the fallback keeps. */
    private val literals: Set<String> = templateSegments.flatMap { it.second }.filterNot { it == PLACEHOLDER }.toSet()

    /**
     * The template for an encoded request path: the query and fragment are dropped; a path matching a template
     * (each {id} matching exactly one non-empty segment) gives that template; any other path keeps each segment that
     * is a literal of some template or a version name (v1, v2, …) and replaces every other segment with {id}.
     * "" and "/" give "/". Never returns a segment from the request that is not a known literal.
     */
    fun template(encodedPath: String): String {
        val segments = segmentsOf(encodedPath.substringBefore('?').substringBefore('#'))
        if (segments.isEmpty()) return ROOT
        templateSegments.firstOrNull { (_, template) -> matches(template, segments) }?.let { return it.first }
        return segments.joinToString(separator = "/", prefix = "/") { segment ->
            if (segment in literals || VERSION.matches(segment)) segment else PLACEHOLDER
        }
    }

    private fun matches(template: List<String>, segments: List<String>): Boolean =
        template.size == segments.size &&
            template.indices.all { index -> template[index] == PLACEHOLDER || template[index] == segments[index] }

    /** The non-empty segments of a path, so a missing segment never matches a placeholder. */
    private fun segmentsOf(path: String): List<String> = path.split('/').filter { it.isNotEmpty() }
}

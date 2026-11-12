package com.paydock.core.telemetry

import com.paydock.core.telemetry.NetworkFailureTestData.HEX_ID
import com.paydock.core.telemetry.NetworkFailureTestData.HOST
import com.paydock.core.telemetry.NetworkFailureTestData.TOKEN_LIKE
import com.paydock.telemetry.AttributeValue
import com.paydock.telemetry.TelemetryEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Unit tests for [RouteTemplates] (R_05): every path the repositories build gives its template, a query and a fragment
 * are dropped, an unknown path keeps no unknown segment, every template passes the allowlist's shape rule, and a scan
 * of the repositories' sources finds no endpoint the table misses.
 *
 * The working directory of a module's unit tests is the module directory.
 */
internal class RouteTemplatesTest {

    /** Each path the repositories build, with sample IDs, and the template it must give. */
    private val endpoints: List<Pair<String, String>> = listOf(
        "/v1/payment_sources/tokens" to "/v1/payment_sources/tokens",
        "/v1/payment_sources/external_checkout" to "/v1/payment_sources/external_checkout",
        "/v1/payment_sources/setup-tokens" to "/v1/payment_sources/setup-tokens",
        "/v1/payment_sources/setup-tokens/$TOKEN_LIKE/tokens" to "/v1/payment_sources/setup-tokens/{id}/tokens",
        "/v1/payment_sources/setup-tokens/$HEX_ID/tokens" to "/v1/payment_sources/setup-tokens/{id}/tokens",
        "/v1/charges/wallet/capture" to "/v1/charges/wallet/capture",
        "/v1/charges/wallet/$HEX_ID/decline" to "/v1/charges/wallet/{id}/decline",
        "/v1/charges/wallet/callback" to "/v1/charges/wallet/callback",
        "/v1/gateways/$HEX_ID/wallet-config" to "/v1/gateways/{id}/wallet-config",
        "/v1/gateways/$TOKEN_LIKE/wallet-config" to "/v1/gateways/{id}/wallet-config",
    )

    @Test
    fun templates_areTheEightSdkEndpoints_inOrder() {
        assertEquals(
            listOf(
                "/v1/payment_sources/tokens",
                "/v1/payment_sources/external_checkout",
                "/v1/payment_sources/setup-tokens",
                "/v1/payment_sources/setup-tokens/{id}/tokens",
                "/v1/charges/wallet/capture",
                "/v1/charges/wallet/{id}/decline",
                "/v1/charges/wallet/callback",
                "/v1/gateways/{id}/wallet-config",
            ),
            RouteTemplates.TEMPLATES
        )
    }

    @Test
    fun everyEndpointPath_givesItsTemplate() {
        endpoints.forEach { (path, template) -> assertEquals(path, template, RouteTemplates.template(path)) }
        assertEquals(RouteTemplates.TEMPLATES.toSet(), endpoints.map { it.second }.toSet())
    }

    @Test
    fun aTemplate_givesItself() {
        RouteTemplates.TEMPLATES.forEach { assertEquals(it, RouteTemplates.template(it)) }
    }

    @Test
    fun theQueryAndFragment_areDropped() {
        assertEquals("/v1/charges/wallet/callback", RouteTemplates.template("/v1/charges/wallet/callback?mobile=true"))
        assertEquals(
            "/v1/gateways/{id}/wallet-config",
            RouteTemplates.template("/v1/gateways/$HEX_ID/wallet-config?token=$TOKEN_LIKE#section")
        )
        assertEquals("/v1/payment_sources/tokens", RouteTemplates.template("/v1/payment_sources/tokens#$HEX_ID"))
        assertEquals("/", RouteTemplates.template("?id=$HEX_ID"))
    }

    @Test
    fun emptyAndRoot_giveRoot() {
        assertEquals("/", RouteTemplates.template(""))
        assertEquals("/", RouteTemplates.template("/"))
        assertEquals("/", RouteTemplates.template("//"))
    }

    @Test
    fun aPlaceholder_matchesExactlyOneSegment_emptyIncluded() {
        assertEquals("/v1/charges/wallet/{id}/decline", RouteTemplates.template("/v1/charges/wallet//decline"))
        assertEquals("/v1/gateways/{id}/wallet-config", RouteTemplates.template("/v1/gateways//wallet-config"))
        assertEquals(
            "/v1/charges/wallet/{id}/{id}/decline",
            RouteTemplates.template("/v1/charges/wallet/$HEX_ID/$TOKEN_LIKE/decline")
        )
    }

    @Test
    fun anUnknownPath_keepsOnlyKnownLiteralsAndVersions() {
        assertEquals("/v2/charges/{id}", RouteTemplates.template("/v2/charges/$HEX_ID"))
        assertEquals("/v1/{id}/{id}/tokens", RouteTemplates.template("/v1/customers/$HEX_ID/tokens"))
        assertEquals("/{id}/{id}", RouteTemplates.template("/someone@example.com/V1"))
        assertEquals("/v1/payment_sources/{id}", RouteTemplates.template("/v1/payment_sources/$TOKEN_LIKE"))
        assertEquals("/v1/{id}/{id}/tokens", RouteTemplates.template("/v1/customers//tokens"))
        val unknown = RouteTemplates.template("/v1/customers/$HEX_ID/cards/$TOKEN_LIKE?email=x#y")
        assertFalse(unknown, unknown.contains(HEX_ID) || unknown.contains(TOKEN_LIKE) || unknown.contains("customers"))
    }

    /** Every template, as url.template and in the group key, passes the allowlist's shape rules. */
    @Test
    fun everyTemplate_passesTheAllowlistShapeRule() {
        RouteTemplates.TEMPLATES.forEach { template ->
            val record = NetworkFailureTestData.storedRecord(
                TelemetryEvent.NetworkFailure(
                    method = TelemetryEvent.HttpMethod.POST,
                    urlTemplate = template,
                    serverAddress = HOST,
                    failure = TelemetryEvent.NetworkError.HttpStatus(400)
                )
            )
            assertEquals(template, AttributeValue.Str(template), record.value("url.template"))
            assertEquals(template, AttributeValue.Str("http POST $template 400"), record.value("paydock.group.key"))
        }
    }

    /**
     * The "every endpoint" test: each `path("…")` in a repository source, with its interpolations replaced by a
     * sample ID, gives one of [RouteTemplates.TEMPLATES]. An endpoint added without a template fails here.
     */
    @Test
    fun everyRepositoryPath_mapsToATemplate() {
        val files = File("src/main/kotlin/com/paydock/feature").walkTopDown()
            .filter { it.isFile && it.extension == "kt" && it.parentFile.invariantSeparatorsPath.endsWith("/data/repository") }
            .toList()
        assertTrue("no repository sources found", files.isNotEmpty())

        val paths = files.flatMap { file ->
            PATH_CALL.findAll(file.readText()).map { file.name to it.groupValues[1] }.toList()
        }
        assertTrue("found only ${paths.size} path(…) calls", paths.size >= ELEVEN_CALL_SITES)

        val mapped = paths.map { (file, path) ->
            val concrete = path.replace(INTERPOLATION, HEX_ID)
            val template = RouteTemplates.template(concrete)
            assertTrue("$file: path(\"$path\") gives $template, not in TEMPLATES", template in RouteTemplates.TEMPLATES)
            template
        }
        assertEquals(RouteTemplates.TEMPLATES.toSet(), mapped.toSet())
    }

    private companion object {
        const val ELEVEN_CALL_SITES = 11
        val PATH_CALL = Regex("""\bpath\(\s*"([^"]*)"\s*\)""")
        val INTERPOLATION = Regex("""\$\{[^}]*}|\$[A-Za-z_][A-Za-z0-9_]*""")
    }
}

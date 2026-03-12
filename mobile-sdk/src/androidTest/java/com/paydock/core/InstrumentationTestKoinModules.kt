package com.paydock.core

import com.paydock.binprocessor.BIN_DATA_HTTP_CLIENT_QUALIFIER
import com.paydock.core.network.NetworkClientBuilder
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import org.koin.dsl.module

/**
 * Mock modules for instrumentation tests. Duplicated from test source set to avoid
 * pulling in JVM-only dependencies (turbine, MockResponseFileReader, etc.).
 *
 * These provide BinDataCacheManager and BinDataRefreshCoordinator with no real network calls.
 */
internal object InstrumentationTestKoinModules {

    private const val MOCK_LAST_MODIFIED = "Wed, 18 Feb 2026 10:00:00 GMT"
    private const val MOCK_ETAG = "\"abc123\""
    private const val MOCK_BIN_DATA_CONTENT =
        """{"2":{},"4":{},"6":{},"8":{},"v":1,"s":{"v":"visa"},"r":{"2":[],"4":[],"6":[],"8":[]}}"""

    val mockBinDataSuccessModule = module {
        single<HttpClient>(BIN_DATA_HTTP_CLIENT_QUALIFIER) {
            val mockEngine = MockEngine { request ->
                when (request.method) {
                    HttpMethod.Head -> respond(
                        content = "",
                        status = HttpStatusCode.OK,
                        headers = headersOf(
                            HttpHeaders.LastModified to listOf(MOCK_LAST_MODIFIED)
                        )
                    )
                    HttpMethod.Get -> respond(
                        content = MOCK_BIN_DATA_CONTENT,
                        status = HttpStatusCode.OK,
                        headers = headersOf(
                            HttpHeaders.ContentType to listOf("application/json"),
                            HttpHeaders.LastModified to listOf(MOCK_LAST_MODIFIED),
                            HttpHeaders.ETag to listOf(MOCK_ETAG)
                        )
                    )
                    else -> respond(
                        content = "",
                        status = HttpStatusCode.MethodNotAllowed
                    )
                }
            }
            NetworkClientBuilder.create()
                .setBaseUrl("https://d25lng5khxpk7i.cloudfront.net")
                .setMockEngine(mockEngine)
                .build()
        }
    }

    /** Minimal mock HttpClient for API calls - returns 200 for any request. */
    val mockNetworkModule = module {
        single {
            val mockEngine = MockEngine {
                respond(
                    content = """{}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType to listOf("application/json"))
                )
            }
            NetworkClientBuilder.create()
                .setBaseUrl("https://example.com")
                .setMockEngine(mockEngine)
                .build()
        }
    }
}

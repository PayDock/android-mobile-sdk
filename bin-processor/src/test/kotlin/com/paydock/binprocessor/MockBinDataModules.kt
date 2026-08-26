package com.paydock.binprocessor

import com.paydock.core.network.NetworkClientBuilder
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Mock module for BIN data HTTP client with successful responses (Last-Modified only).
 * Provides HEAD with Last-Modified header and GET with BIN data content.
 */
val mockBinDataSuccessModule = module {
    single<HttpClient>(named("binDataHttpClient")) {
        val mockEngine = MockEngine { request ->
            when (request.method) {
                HttpMethod.Head -> respond(
                    content = "",
                    status = HttpStatusCode.OK,
                    headers = headersOf(
                        HttpHeaders.LastModified to listOf(BinDataTestConstants.MOCK_LAST_MODIFIED)
                    )
                )
                HttpMethod.Get -> respond(
                    content = BinDataTestConstants.MOCK_BIN_DATA_CONTENT,
                    status = HttpStatusCode.OK,
                    headers = headersOf(
                        HttpHeaders.ContentType to listOf("application/json"),
                        HttpHeaders.LastModified to listOf(BinDataTestConstants.MOCK_LAST_MODIFIED),
                        HttpHeaders.ETag to listOf(BinDataTestConstants.MOCK_ETAG)
                    )
                )
                else -> respondError(HttpStatusCode.MethodNotAllowed)
            }
        }
        NetworkClientBuilder.create()
            .setBaseUrl("https://d25lng5khxpk7i.cloudfront.net")
            .setMockEngine(mockEngine)
            .build()
    }
}

/**
 * Mock module for BIN data HTTP client with ETag-only responses (no Last-Modified).
 */
val mockBinDataEtagOnlyModule = module {
    single<HttpClient>(named("binDataHttpClient")) {
        val mockEngine = MockEngine { request ->
            when (request.method) {
                HttpMethod.Head -> respond(
                    content = "",
                    status = HttpStatusCode.OK,
                    headers = headersOf(
                        HttpHeaders.ETag to listOf(BinDataTestConstants.MOCK_ETAG)
                    )
                )
                HttpMethod.Get -> respond(
                    content = BinDataTestConstants.MOCK_BIN_DATA_CONTENT,
                    status = HttpStatusCode.OK,
                    headers = headersOf(
                        HttpHeaders.ContentType to listOf("application/json"),
                        HttpHeaders.ETag to listOf(BinDataTestConstants.MOCK_ETAG)
                    )
                )
                else -> respondError(HttpStatusCode.MethodNotAllowed)
            }
        }
        NetworkClientBuilder.create()
            .setBaseUrl("https://d25lng5khxpk7i.cloudfront.net")
            .setMockEngine(mockEngine)
            .build()
    }
}

/**
 * Mock module for BIN data HTTP client with network failure responses.
 */
val mockBinDataFailureModule = module {
    single<HttpClient>(named("binDataHttpClient")) {
        val mockEngine = MockEngine {
            respondError(HttpStatusCode.InternalServerError)
        }
        NetworkClientBuilder.create()
            .setBaseUrl("https://d25lng5khxpk7i.cloudfront.net")
            .setMockEngine(mockEngine)
            .build()
    }
}

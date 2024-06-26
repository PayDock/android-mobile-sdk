package com.paydock.core.data.injection.modules

import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.network.error.ApiErrorInterceptor
import com.paydock.core.extensions.convertToDataClass
import com.paydock.core.utils.MockResponseFileReader
import com.paydock.feature.card.data.api.auth.CardAuthInterceptor
import com.paydock.feature.wallet.data.api.dto.WalletCallbackRequest
import com.paydock.feature.wallet.domain.model.WalletType
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.appendIfNameAbsent
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.json.Json
import okhttp3.CertificatePinner
import org.koin.dsl.module

/**
 * This module defines a single `HttpClientEngine` and a single `HttpClient`.
 */
val mockSuccessNetworkModule = module {

    single {
        provideSuccessHttpMockEngine()
    }

    single {
        // Create a `HttpClient` that uses the mock HTTP engine.
        provideHttpMockClient(get(), get())
    }
}

/**
 * This module defines a single `HttpClientEngine` and a single `HttpClient`.
 */
val mockFailureNetworkModule = module {

    single {
        provideFailureHttpMockEngine()
    }

    single {
        // Create a `HttpClient` that uses the mock HTTP engine.
        provideHttpMockClient(get(), get())
    }
}

/**
 * This module defines a single `CertificatePinner` and a single `HttpClient`.
 */
val sslFailNetworkTestModule = module {

    single {
        // Create a `CertificatePinner` that is configured to fail if the certificate for the domain `paydock.com` is not pinned.
        provideSSLFailCertificate()
    }

    single {
        // Create a `HttpClientEngine` that uses the `CertificatePinner` to verify the certificate for the domain `paydock.com`.
        provideSslHttpEngine(get())
    }

    single {
        // Create a `HttpClient` that uses the `HttpClientEngine`.
        provideHttpMockClient(get(), get())
    }
}

/**
 * This module defines a single `CertificatePinner` and a single `HttpClient`.
 */
val sslSuccessNetworkTestModule = module {

    single {
        // Create a `CertificatePinner` that is configured to succeed if the certificate for the domain `paydock.com` is pinned.
        provideSSLCertificate()
    }

    single {
        // Create a `HttpClientEngine` that uses the `CertificatePinner` to verify the certificate for the domain `paydock.com`.
        provideSslHttpEngine(get())
    }

    single {
        // Create a `HttpClient` that uses the `HttpClientEngine`.
        provideHttpMockClient(get(), get())
    }
}

/**
 * This module defines a single `OKHttp` Engine with AuthInterceptor and a single `HttpClient`.
 */
val mockAuthInterceptorOkHttpModule = module {

    single {
        provideAuthInterceptorHttpOKHttpEngine()
    }

    single {
        // Create a `HttpClient` that uses the `HttpClientEngine`.
        provideHttpMockClient(get(), get())
    }
}

/**
 * This module defines a single `OKHttp` Engine with AuthInterceptor and a single `HttpClient`.
 */
val mockApiInterceptorOkHttpModule = module {

    single {
        provideApiInterceptorHttpOKHttpEngine()
    }

    single {
        // Create a `HttpClient` that uses the `HttpClientEngine`.
        provideHttpMockClient(get(), get())
    }
}

/**
 * This function returns a `CertificatePinner` that is configured to fail if the certificate for the domain `paydock.com` is not pinned.
 */
fun provideSSLFailCertificate(): CertificatePinner {

    // Create a `CertificatePinner` that is configured to fail if the certificate for the domain `paydock.com` is not pinned.
    return CertificatePinner.Builder()
        .add("paydock.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        .build()
}

/**
 * This function returns a `HttpClientEngine` that is configured to use the `CertificatePinner` to verify the
 * certificate for the domain `paydock.com`.
 */
fun provideSslHttpEngine(
    certificatePinner: CertificatePinner
): HttpClientEngine {

    // Create a `HttpClientEngine` that uses the `CertificatePinner` to verify the certificate for the domain `paydock.com`.
    return OkHttp.create {
        config {
            // Certificate Pinning
            certificatePinner(certificatePinner)
            hostnameVerifier { hostname, _ -> hostname == "paydock.com" }
        }

        ResponseObserver(responseHandler = { response ->
            // Print the response body for debugging purposes
            println(response.bodyAsText())
        })
    }
}

/**
 * This function returns a `HttpClientEngine` that is created using the `MockEngine.create()` function.
 */
fun provideSuccessHttpMockEngine(): HttpClientEngine {
    // Create a mock HTTP engine.
    return MockEngine.create {
        // Add a handler that will handle all requests.
        addHandler { request ->
            handleRequest(request)
        }

        ResponseObserver(responseHandler = { response ->
            // Print the response body for debugging purposes
            println(response.bodyAsText())
        })
    }
}

fun provideFailureHttpMockEngine(): HttpClientEngine {
    // Create a mock HTTP engine.
    return MockEngine.create {
        // Add a handler that will handle all requests.
        addHandler { request ->
            handleFailureRequest(request)
        }

        ResponseObserver(responseHandler = { response ->
            // Print the response body for debugging purposes
            println(response.bodyAsText())
        })
    }
}

/**
 * Provides an instance of the OkHttp engine for the HttpClient.
 *
 * @return An instance of the OkHttp engine with the AuthInterceptor added.
 */
fun provideAuthInterceptorHttpOKHttpEngine(): HttpClientEngine {
    // Sample public key
    val publicKey = "sample_public_key"
    // Create a mock HTTP engine using OkHttp.
    return OkHttp.create {
        // Set the OkHttp client with the AuthInterceptor
        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .addInterceptor(CardAuthInterceptor(publicKey)) // Add the AuthInterceptor with the public key
            .build()

        ResponseObserver(responseHandler = { response ->
            // Print the response body for debugging purposes
            println(response.bodyAsText())
        })

        preconfigured =
            okHttpClient // Assign the OkHttp client to the preconfigured property of the OkHttp engine
    }
}

/**
 * Provides an instance of the OkHttp engine for the HttpClient.
 *
 * @return An instance of the OkHttp engine with the ApiErrorInterceptor added.
 */
fun provideApiInterceptorHttpOKHttpEngine(): HttpClientEngine {
    // Create a mock HTTP engine using OkHttp.
    return OkHttp.create {
        // Set the OkHttp client with the ApiErrorInterceptor
        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .addInterceptor(ApiErrorInterceptor())
            .build()

        ResponseObserver(responseHandler = { response ->
            // Print the response body for debugging purposes
            println(response.bodyAsText())
        })

        preconfigured =
            okHttpClient // Assign the OkHttp client to the preconfigured property of the OkHttp engine
    }
}

/**
 * This function returns a `HttpClient` that is created using the `HttpClient(engine)` function.
 */
fun provideHttpMockClient(
    json: Json,
    engine: HttpClientEngine
): HttpClient {

    // Create a `HttpClient` that uses the mock HTTP engine.
    return HttpClient(engine) {
        expectSuccess = true
        defaultRequest {
            headers {
                appendIfNameAbsent(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.contentType
                )
            }
            contentType(ContentType.Application.Json)
        }
        install(ContentNegotiation) {
            json(json)
        }
    }
}

/**
 * This function is used to handle requests that are made to the mock HTTP engine
 **/
@Suppress("LongMethod")
private fun MockRequestHandleScope.handleRequest(request: HttpRequestData): HttpResponseData {
    return when {
        // Check for matching endpoint path
        request.url.encodedPath.endsWith("/payment_sources/tokens") -> {
            // Check if the custom header indicating success is present
            respond(
                content = MockResponseFileReader("card/success_card_token_response.json").content,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        request.url.encodedPath.endsWith("/charges/wallet/capture") -> {
            // Check if the custom header indicating success is present
            respond(
                content = MockResponseFileReader("wallet/success_capture_wallet_response.json").content,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        request.url.encodedPath.endsWith("/charges/wallet/callback") -> {
            if (request.body is OutgoingContent.ByteArrayContent) {
                // Need to determine which request are we using and respond correctly
                val callbackRequest = (request.body as OutgoingContent.ByteArrayContent).bytes()
                    .decodeToString().convertToDataClass<WalletCallbackRequest>()
                when (callbackRequest.walletType) {
                    WalletType.PAY_PAL.type -> {
                        // Check if the custom header indicating success is present
                        respond(
                            content = MockResponseFileReader("wallet/success_paypal_wallet_callback_response.json").content,
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }

                    WalletType.AFTER_PAY.type -> {
                        // Check if the custom header indicating success is present
                        respond(
                            content = MockResponseFileReader("wallet/success_afterpay_wallet_callback_response.json").content,
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }

                    WalletType.FLY_PAY.type -> {
                        // Check if the custom header indicating success is present
                        respond(
                            content = MockResponseFileReader("wallet/success_flypay_wallet_callback_response.json").content,
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }

                    else -> errorResponse()
                }
            } else {
                errorResponse()
            }
        }

        request.url.encodedPath.endsWith("/charges/wallet/${MobileSDKTestConstants.Charge.MOCK_CHARGE_ID}/decline") -> {
            // Check if the custom header indicating success is present
            respond(
                content = MockResponseFileReader("wallet/success_afterpay_decline_wallet_charge_response.json").content,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        request.url.encodedPath.endsWith("/") -> {
            // This is purely for example purposes
            respond(
                content = ByteReadChannel("""{"ip":"127.0.0.1"}"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        else -> errorResponse()
    }
}

private fun MockRequestHandleScope.handleFailureRequest(request: HttpRequestData): HttpResponseData {
    return when {
        // Check for matching endpoint path
        request.url.encodedPath.endsWith("/payment_sources/tokens") -> {
            respondError(
                content = MockResponseFileReader("card/failure_credit_card_token_response.json").content,
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        request.url.encodedPath.endsWith("/charges/wallet/capture") -> {
            respondError(
                content = MockResponseFileReader("wallet/failure_capture_wallet_response.json").content,
                status = HttpStatusCode.Forbidden,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        request.url.encodedPath.endsWith("/charges/wallet/callback") -> {
            respondError(
                content = MockResponseFileReader("wallet/failure_wallet_callback_response.json").content,
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        request.url.encodedPath.endsWith("/charges/wallet/${MobileSDKTestConstants.Charge.MOCK_INVALID_CHARGE_ID}/decline") -> {
            respondError(
                content = MockResponseFileReader("wallet/failure_decline_wallet_invalid_chargeid_response.json").content,
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        else -> errorResponse()
    }
}

/**
 * This function is used to handle error responses that are made to the mock HTTP engine
 **/
private fun MockRequestHandleScope.errorResponse(): HttpResponseData {
    return respond(
        content = "",
        status = HttpStatusCode.BadRequest,
        headers = headersOf(HttpHeaders.ContentType, "application/json")
    )
}
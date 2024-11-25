package com.paydock.core.data.injection.modules

import com.paydock.MobileSDK
import com.paydock.api.charges.data.dto.WalletCallbackRequest
import com.paydock.api.charges.domain.model.WalletType
import com.paydock.core.MobileSDKConstants
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.domain.mapper.mapToBaseUrl
import com.paydock.core.network.NetworkClientBuilder
import com.paydock.core.network.addInterceptor
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.core.network.interceptor.AuthInterceptor
import com.paydock.core.utils.MockResponseFileReader
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import org.koin.dsl.module

/**
 * This module defines a single `HttpClientEngine` and a single `HttpClient`.
 */
internal val mockSuccessNetworkModule = module {
    single<MockEngine> {
        MockEngine.create {
            addHandler { request -> handleSuccessRequest(request) }
        } as MockEngine
    }

    single {
        // Create a `HttpClient` that uses the mock HTTP engine.
        NetworkClientBuilder.create()
            .setBaseUrl("https://example.com")
            .setMockEngine(get())
            .build()
    }
}

/**
 * This module defines a single `HttpClientEngine` and a single `HttpClient`.
 */
internal val mockFailureNetworkModule = module {

    single<MockEngine> {
        MockEngine.create {
            addHandler { request -> handleFailureRequest(request) }
        } as MockEngine
    }

    single {
        // Create a `HttpClient` that uses the mock HTTP engine.
        NetworkClientBuilder.create()
            .setBaseUrl("https://example.com")
            .setMockEngine(get())
            .build()
    }
}

/**
 * This module defines a single `CertificatePinner` and a single `HttpClient`.
 */
internal val sslFailNetworkTestModule = module {
    single {
        // Create a `HttpClient` that uses the mock HTTP engine.
        NetworkClientBuilder.create()
            .setBaseUrl("example.com")
            .setSslPins(listOf("sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="))
            .build()
    }
}

/**
 * This module defines a single `CertificatePinner` and a single `HttpClient`.
 */
internal val sslSuccessNetworkTestModule = module {

    single {
        // Create a `HttpClient` that uses the mock HTTP engine.
        NetworkClientBuilder.create()
            .setBaseUrl(MobileSDK.getInstance().environment.mapToBaseUrl())
            .setSslPins(listOf(MobileSDKConstants.Network.SSH_HASH))
            .build()
    }
}

/**
 * This module defines a single `OKHttp` Engine with AuthInterceptor and a single `HttpClient`.
 */
internal val mockAuthInterceptorOkHttpModule = module {

    single {
        // Create a `HttpClient` that uses the `HttpClientEngine`.
        NetworkClientBuilder.create()
            .setBaseUrl("paydock.com")
            .setProtocol(URLProtocol.HTTP)
            .addInterceptor(AuthInterceptor("sample_public_key"))
            .build()
    }
}

/**
 * This module defines a single `OKHttp` Engine with default ApiErrorInterceptor and a single `HttpClient`.
 */
internal val mockApiInterceptorOkHttpModule = module {

    single {
        // Create a `HttpClient` that uses the mock HTTP engine.
        NetworkClientBuilder.create()
            .setProtocol(URLProtocol.HTTP)
            .setBaseUrl("paydock.com")
            .build()
    }
}

/**
 * This function is used to handle requests that are made to the mock HTTP engine
 **/
@Suppress("LongMethod", "CyclomaticComplexMethod")
private fun MockRequestHandleScope.handleSuccessRequest(request: HttpRequestData): HttpResponseData {
    return when {
        // Check for matching endpoint path
        request.url.encodedPath.endsWith("/payment_sources/tokens") -> {
            // Check if the custom header indicating success is present
            respond(
                content = MockResponseFileReader("token/success_card_token_response.json").content,
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        request.url.encodedPath.endsWith("/payment_sources/setup-tokens/${MobileSDKTestConstants.PayPalVault.MOCK_SETUP_TOKEN}/tokens") -> {
            // Check if the custom header indicating success is present
            respond(
                content = MockResponseFileReader("token/success_payment_token_response.json").content,
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        request.url.encodedPath.endsWith("/payment_sources/oauth-tokens") -> {
            // Check if the custom header indicating success is present
            respond(
                content = MockResponseFileReader("token/success_session_auth_response.json").content,
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        request.url.encodedPath.endsWith("/payment_sources/setup-tokens") -> {
            // Check if the custom header indicating success is present
            respond(
                content = MockResponseFileReader("token/success_setup_token_response.json").content,
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        request.url.encodedPath.endsWith("/charges/wallet/capture") -> {
            // Check if the custom header indicating success is present
            respond(
                content = MockResponseFileReader("charges/success_capture_wallet_response.json").content,
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
                            content = MockResponseFileReader("charges/success_paypal_wallet_callback_response.json").content,
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }

                    WalletType.AFTER_PAY.type -> {
                        // Check if the custom header indicating success is present
                        respond(
                            content = MockResponseFileReader("charges/success_afterpay_wallet_callback_response.json").content,
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }

                    WalletType.FLY_PAY.type -> {
                        // Check if the custom header indicating success is present
                        respond(
                            content = MockResponseFileReader("charges/success_flypay_wallet_callback_response.json").content,
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
                content = MockResponseFileReader("charges/success_afterpay_decline_wallet_charge_response.json").content,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        request.url.encodedPath.endsWith("/gateways/${MobileSDKTestConstants.General.MOCK_GATEWAY_ID}/wallet-config") -> {
            respondError(
                content = MockResponseFileReader("gateway/success_wallet_config_response.json").content,
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

@Suppress("LongMethod")
private fun MockRequestHandleScope.handleFailureRequest(request: HttpRequestData): HttpResponseData {
    return when {
        // Check for matching endpoint path
        request.url.encodedPath.endsWith("/payment_sources/tokens") -> {
            respondError(
                content = MockResponseFileReader("token/failure_credit_card_token_response.json").content,
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        request.url.encodedPath.endsWith("/payment_sources/setup-tokens/${MobileSDKTestConstants.PayPalVault.MOCK_SETUP_TOKEN}/tokens") -> {
            respondError(
                content = MockResponseFileReader("token/failure_payment_token_response.json").content,
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        request.url.encodedPath.endsWith("/payment_sources/oauth-tokens") -> {
            respondError(
                content = MockResponseFileReader("token/failure_session_auth_response.json").content,
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        request.url.encodedPath.endsWith("/payment_sources/setup-tokens") -> {
            respondError(
                content = MockResponseFileReader("token/failure_setup_token_response.json").content,
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        request.url.encodedPath.endsWith("/charges/wallet/capture") -> {
            respondError(
                content = MockResponseFileReader("charges/failure_capture_wallet_response.json").content,
                status = HttpStatusCode.Forbidden,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        request.url.encodedPath.endsWith("/charges/wallet/callback") -> {
            respondError(
                content = MockResponseFileReader("charges/failure_wallet_callback_response.json").content,
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        request.url.encodedPath.endsWith("/charges/wallet/${MobileSDKTestConstants.Charge.MOCK_INVALID_CHARGE_ID}/decline") -> {
            respondError(
                content = MockResponseFileReader("charges/failure_decline_wallet_invalid_chargeid_response.json").content,
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        request.url.encodedPath.endsWith("/gateways/${MobileSDKTestConstants.General.MOCK_INVALID_GATEWAY_ID}/wallet-config") -> {
            respondError(
                content = MockResponseFileReader("gateway/failure_wallet_config_response.json").content,
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
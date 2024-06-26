package com.paydock.core.data.injection.modules

import com.paydock.BuildConfig
import com.paydock.MobileSDK
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.network.error.ApiErrorInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.appendIfNameAbsent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

/**
 * Network-based module for handling network components using Ktor.
 *
 * This module provides singleton and factory components for network operations.
 * - Singleton components are kept unique within the Koin container.
 * - Factory components provide a new instance each time they are requested.
 */
val networkModule = module {
    includes(dispatchersModule)

    // Provides Interceptors
    factory {
        HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
    }
    factory { ApiErrorInterceptor() }

    // Provides JSON serializer
    single { provideJson() }

    // Provides SSL Certificate Pinner
    single { provideSSLCertificate() }
}

/**
 * Creates a lazily initialized instance of Json with various serialization options.
 */
@OptIn(ExperimentalSerializationApi::class)
private val json: Json by lazy {
    Json {
        encodeDefaults = true
        explicitNulls = false
        prettyPrint = true
        isLenient = false
        ignoreUnknownKeys = true
        allowSpecialFloatingPointValues = true
        useArrayPolymorphism = false
    }
}

/**
 * Provides a configured instance of Json with various serialization options.
 *
 * @return Configured Json instance.
 */
fun provideJson(): Json = json

/**
 * Provides a configured HTTP client engine using OkHttp.
 *
 * @param certificatePinner The SSL certificate pinner.
 * @param authInterceptor Optional authentication interceptor.
 * @param apiErrorInterceptor The API error interceptor.
 * @param loggingInterceptor The logging interceptor.
 * @return Configured HttpClientEngine instance.
 */
fun provideHttpEngine(
    certificatePinner: CertificatePinner,
    authInterceptor: Interceptor?,
    apiErrorInterceptor: ApiErrorInterceptor,
    loggingInterceptor: HttpLoggingInterceptor
): HttpClientEngine {
    return OkHttp.create {
        config {
            connectTimeout(MobileSDKConstants.NetworkConfig.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(MobileSDKConstants.NetworkConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(MobileSDKConstants.NetworkConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
            retryOnConnectionFailure(true)
            // Certificate Pinning
            certificatePinner(certificatePinner)
            hostnameVerifier { hostname, _ -> hostname == MobileSDK.getInstance().baseUrl }
            // Interceptors
            authInterceptor?.let { addInterceptor(it) }
            addInterceptor(apiErrorInterceptor)
            if (BuildConfig.DEBUG) {
                addInterceptor(loggingInterceptor)
            }
        }
    }
}

/**
 * Provides SSL Certificate Pinner with pre-configured certificates.
 *
 * @return Configured CertificatePinner instance.
 */
fun provideSSLCertificate(): CertificatePinner {
    // Load and return the list of SSL pinned certificate hashes or subjects.
    // Certificates can be obtained from a trusted source or embedded within the app.
    return CertificatePinner.Builder()
        .add(MobileSDK.getInstance().baseUrl, "sha256/g3M/GJUTddzhjBySoIBl4U7M+8j3KgSf1EwPpBIlsHs=")
        .build()
}

/**
 * Provides a configured HttpClient instance.
 *
 * @param json The JSON serializer instance.
 * @param engine The HTTP client engine.
 * @return Configured HttpClient instance.
 */
fun provideHttpClient(
    json: Json,
    engine: HttpClientEngine
): HttpClient = HttpClient(engine) {
    expectSuccess = true
    defaultRequest {
        headers {
            appendIfNameAbsent(
                HttpHeaders.ContentType,
                ContentType.Application.Json.contentType
            )
        }
        url {
            protocol = URLProtocol.HTTPS
            host = MobileSDK.getInstance().baseUrl
        }
        contentType(ContentType.Application.Json)
    }
    install(ContentNegotiation) {
        json(json)
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }
    //    install(Resources)
}

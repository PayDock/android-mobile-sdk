/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 5:48 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paydock.core.data.injection.modules

import com.paydock.BuildConfig
import com.paydock.MobileSDK
import com.paydock.core.CONNECTION_TIMEOUT
import com.paydock.core.READ_TIMEOUT
import com.paydock.core.WRITE_TIMEOUT
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
 * Network based module for handling network components - using Ktor
 *
 * - singleton component means that Koin container will keep a unique instance of your declared component
 * - factory component declaration is a definition that will gives you a new instance each time you ask for this definition
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
    // Provides HTTP Engine and Client

    single { provideJson() }
    single { provideSSLCertificate() }
}

@OptIn(ExperimentalSerializationApi::class)
fun provideJson(): Json = Json {
    encodeDefaults = true
    explicitNulls = true
    prettyPrint = true
    isLenient = false
    ignoreUnknownKeys = true
    allowSpecialFloatingPointValues = true
    useArrayPolymorphism = false
}

fun provideHttpEngine(
    certificatePinner: CertificatePinner,
    authInterceptor: Interceptor?,
    apiErrorInterceptor: ApiErrorInterceptor,
    loggingInterceptor: HttpLoggingInterceptor
): HttpClientEngine {
    return OkHttp.create {
        config {
            connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            retryOnConnectionFailure(true)
            // Certificate Pinning
            certificatePinner(certificatePinner)
            // Interceptors
            authInterceptor?.let { addInterceptor(it) }
            addInterceptor(apiErrorInterceptor)
            if (BuildConfig.DEBUG) {
                addInterceptor(loggingInterceptor)
            }
        }
    }
}

fun provideSSLCertificate(): CertificatePinner {
    // Load and return the list of SSL pinned certificate hashes or subjects
    // You can obtain the certificates from a trusted source or embed them within your app
    return CertificatePinner.Builder()
        .add("paydock.com", "sha256/NGKfDItRieVy86A22OPQZDKvZrhSBgA9JpgrGv8veAk=")
        .add("paydock.com", "sha256/J2/oqMTsdhFWW/n85tys6b4yDBtb6idZayIEBx7QTxA=")
        .add("paydock.com", "sha256/diGVwiVYbubAI3RW4hB9xU8e/CH2GnkuvVFZE8zmgzI=")
        .add("paydock.com", "sha256/C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M=")
        .build()
}

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
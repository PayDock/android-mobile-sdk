package com.paydock.core.data.injection.modules

import com.paydock.BuildConfig
import com.paydock.MobileSDK
import com.paydock.core.network.NetworkClientBuilder
import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin qualifier for the HTTP client used for BIN data (CloudFront). */
internal val binDataHttpClientQualifier = named("binDataHttpClient")

/** Base URL for BIN data (CloudFront); must match host used by [BinDataCacheManager]. */
private const val BIN_DATA_BASE_URL = "https://d25lng5khxpk7i.cloudfront.net"

/**
 * Network-based module for handling network components using Ktor.
 * All networking is provided by the Network lib via [NetworkClientBuilder].
 */
internal val networkModule = module {
    includes(dispatchersModule)
    single {
        NetworkClientBuilder.create()
            .setBaseUrl(MobileSDK.getInstance().baseUrl)
            .setDebug(BuildConfig.DEBUG)
            .build()
    }

    // BIN file client (CloudFront). Used for HEAD/GET at SDK init and on app foreground;
    // requests go through the same engine (proxy-aware). Uses BIN base URL and no SSL pins.
    // The Network lib must use the host part of baseUrl for hostname verification (not the full URL)
    // so that this client accepts the CloudFront host; otherwise BIN download will fail with
    // SSLPeerUnverifiedException.
    single<HttpClient>(binDataHttpClientQualifier) {
        NetworkClientBuilder.create()
            .setBaseUrl(BIN_DATA_BASE_URL)
            .setDebug(BuildConfig.DEBUG)
            .build()
    }
}

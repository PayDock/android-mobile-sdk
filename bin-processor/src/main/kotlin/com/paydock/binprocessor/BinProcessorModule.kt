package com.paydock.binprocessor

import android.content.Context
import com.paydock.binprocessor.data.cache.BinDataCacheManager
import com.paydock.binprocessor.data.refresh.BinDataRefreshCoordinator
import com.paydock.binprocessor.domain.repository.BinDataRepository
import com.paydock.binprocessor.domain.repository.BinDataRepositoryImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin qualifier for the HTTP client used for BIN data (CloudFront).
 * The parent application must provide an HttpClient with this qualifier.
 */
val BIN_DATA_HTTP_CLIENT_QUALIFIER = named("binDataHttpClient")

/**
 * Koin module for BIN (Bank Identification Number) data processing components.
 *
 * This module provides:
 * - [BinDataCacheManager] for downloading and caching BIN data from CloudFront
 * - [BinDataRepository] for accessing BIN data
 * - [BinDataRefreshCoordinator] for coordinating BIN data refresh on app lifecycle events
 *
 * Requirements:
 * - The parent application must provide an Android [Context]
 * - The parent application must provide an [HttpClient] with qualifier [BIN_DATA_HTTP_CLIENT_QUALIFIER]
 *
 * Usage:
 * ```kotlin
 * startKoin {
 *     modules(
 *         binProcessorModule,
 *         // Parent modules that provide Context and HttpClient
 *     )
 * }
 * ```
 */
val binProcessorModule = module {
    single<BinDataCacheManager> {
        BinDataCacheManager(
            context = get<Context>(),
            httpClient = get(BIN_DATA_HTTP_CLIENT_QUALIFIER)
        )
    }

    single<BinDataRepository> {
        BinDataRepositoryImpl(cacheManager = get())
    }

    single {
        BinDataRefreshCoordinator(
            binDataCacheManager = get(),
            scope = BinDataRefreshCoordinator.applicationScope()
        )
    }
}

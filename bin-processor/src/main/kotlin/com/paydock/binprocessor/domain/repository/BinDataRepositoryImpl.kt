package com.paydock.binprocessor.domain.repository

import com.paydock.binprocessor.data.cache.BinDataCacheManager
import com.paydock.binprocessor.data.dto.BinDataResponse
import kotlinx.serialization.json.Json

/**
 * Implementation of [BinDataRepository] that wraps [BinDataCacheManager].
 *
 * Provides a clean interface for accessing BIN data while hiding the
 * caching implementation details.
 *
 * @param cacheManager The cache manager for fetching and caching BIN data
 * @param json JSON serializer for parsing BIN data
 */
internal class BinDataRepositoryImpl(
    private val cacheManager: BinDataCacheManager,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : BinDataRepository {

    override fun getBinDataContent(): String =
        cacheManager.getBinDataFileContent()

    override fun getCachedBinDataPath(): String? =
        cacheManager.getCachedBinDataPath()

    override fun shouldRefreshOnForeground(): Boolean =
        cacheManager.shouldRefreshOnForeground()

    override suspend fun shouldDownloadFullFile(): Boolean =
        cacheManager.shouldDownloadFullFile()

    override suspend fun refreshBinDataIfNeeded(): Result<String> =
        cacheManager.refreshBinDataIfNeeded()

    override suspend fun downloadAndCacheBinData(): Result<String> =
        cacheManager.downloadAndCacheBinData()

    override fun getBinData(): BinDataResponse {
        val content = getBinDataContent()
        return json.decodeFromString<BinDataResponse>(content)
    }
}

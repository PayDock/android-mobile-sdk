package com.paydock.binprocessor.domain.repository

import com.paydock.binprocessor.data.dto.BinDataResponse

/**
 * Repository interface for accessing BIN (Bank Identification Number) data.
 *
 * Provides methods for fetching and caching BIN data used for card scheme detection.
 * The implementation handles the caching strategy, including:
 * - Fetching from remote CloudFront CDN
 * - Falling back to bundled assets
 * - Managing cache expiration
 */
interface BinDataRepository {

    /**
     * Gets the BIN data file content, preferring cached version over bundled asset.
     *
     * @return The BIN data file content as a JSON string
     */
    fun getBinDataContent(): String

    /**
     * Gets the path to the cached BIN data file, if available.
     *
     * @return File path to cached file, or null if not cached
     */
    fun getCachedBinDataPath(): String?

    /**
     * Determines if a refresh check should be performed on app foreground.
     *
     * Returns true if:
     * - No cached file exists
     * - Cached file exists but last successful fetch was more than 1 hour ago
     *
     * @return true if refresh should be attempted
     */
    fun shouldRefreshOnForeground(): Boolean

    /**
     * Determines if a full file download is needed based on remote cache headers.
     *
     * Performs a HEAD request to check Last-Modified/ETag against stored values.
     *
     * @return true if remote file is newer or no cache exists
     */
    suspend fun shouldDownloadFullFile(): Boolean

    /**
     * Refreshes BIN data if needed using HEAD-then-conditional-GET strategy.
     *
     * Only downloads the full file if:
     * - No cache exists
     * - Remote file is newer (based on Last-Modified/ETag)
     *
     * @return Result with cached file path on success, or failure with exception
     */
    suspend fun refreshBinDataIfNeeded(): Result<String>

    /**
     * Forces download and caching of BIN data from CloudFront.
     *
     * @return Result containing the cached file path if successful, or error
     */
    suspend fun downloadAndCacheBinData(): Result<String>

    /**
     * Parses and returns the BIN data response object.
     *
     * @return The parsed [BinDataResponse] object
     */
    fun getBinData(): BinDataResponse
}

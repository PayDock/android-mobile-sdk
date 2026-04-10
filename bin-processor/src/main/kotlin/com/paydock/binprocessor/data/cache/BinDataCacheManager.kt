package com.paydock.binprocessor.data.cache

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

/**
 * Manages fetching and caching of the BIN validation file from CloudFront so the app
 * always checks against the latest list of BIN files.
 *
 * Fresh fetch is performed at:
 * - SDK initialisation
 * - App foregrounding when there is no cached file
 * - App foregrounding when the cached file exists and last successful fetch was more than 1 hour ago
 *
 * Uses a HEAD request (Last-Modified / ETag) to avoid full download when the remote file is unchanged.
 * The latest file is saved in cache; all requests use the provided [httpClient] (proxy-aware).
 *
 * @param context Android context for file operations and preferences
 * @param httpClient Ktor HTTP client for BIN requests (used for both HEAD and GET; must be proxy-aware)
 */
internal class BinDataCacheManager(
    private val context: Context,
    private val httpClient: HttpClient
) {
    companion object {
        private const val CLOUDFRONT_URL = "https://d25lng5khxpk7i.cloudfront.net/bin-data-au-v1.json"
        internal const val ASSET_FILENAME = "card-schemes.json"
        private const val CACHED_FILENAME = "card-schemes-cached.json"
        private const val PREFS_NAME = "paydock_bin_cache"
        private const val KEY_LAST_MODIFIED = "bin_last_modified"
        private const val KEY_ETAG = "bin_etag"
        private const val KEY_LAST_FETCH_TIME_MS = "bin_last_fetch_time_ms"
        private const val CACHE_REFRESH_INTERVAL_MS = 60 * 60 * 1000L // 1 hour
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Gets the path to the cached BIN data file.
     *
     * @return File path to cached file, or null if not cached
     */
    fun getCachedBinDataPath(): String? {
        val filesDir = try { context.filesDir } catch (e: Exception) { null } ?: return null
        return try {
            val cachedFile = File(filesDir, CACHED_FILENAME)
            if (cachedFile.exists() && cachedFile.length() > 0) {
                cachedFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            // Handle potential NPE from File constructor when using mocks in tests
            null
        }
    }

    /**
     * Gets the BIN data file content, preferring cached version over asset.
     *
     * @return The file content as a string
     */
    fun getBinDataFileContent(): String {
        val cachedPath = getCachedBinDataPath()
        if (cachedPath != null) {
            try {
                val cachedFile = File(cachedPath)
                return cachedFile.readText()
            } catch (e: Exception) {
                // Fall through to asset if cache read fails
            }
        }
        return readTextFromAsset(ASSET_FILENAME)
    }

    /**
     * Reads text content from an asset file.
     *
     * @param fileName The name of the asset file
     * @return The text content of the asset file
     */
    private fun readTextFromAsset(fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use {
            it.readText()
        }
    }

    /**
     * True if we should run a refresh check on app foreground: no cached file, or
     * cached file exists and last successful fetch was more than 1 hour ago.
     */
    fun shouldRefreshOnForeground(): Boolean {
        if (getCachedBinDataPath() == null) return true
        val lastFetchMs = prefs.getLong(KEY_LAST_FETCH_TIME_MS, 0L)
        if (lastFetchMs == 0L) return true
        return (System.currentTimeMillis() - lastFetchMs) >= CACHE_REFRESH_INTERVAL_MS
    }

    /**
     * Performs HEAD request and returns true if we should download the full file:
     * no cache, or remote Last-Modified/ETag differs from stored.
     */
    suspend fun shouldDownloadFullFile(): Boolean = withContext(Dispatchers.IO) {
        if (getCachedBinDataPath() == null) return@withContext true
        val remote = fetchRemoteCacheHeaders() ?: return@withContext false
        val storedLastModified = prefs.getString(KEY_LAST_MODIFIED, null)
        val storedEtag = prefs.getString(KEY_ETAG, null)
        when {
            remote.lastModified != null -> remote.lastModified != storedLastModified
            remote.etag != null -> remote.etag != storedEtag
            else -> storedLastModified == null && storedEtag == null
        }
    }

    /**
     * Refreshes BIN data if needed: HEAD first, then GET only when remote is newer or no cache.
     *
     * @return Result with cached file path on success, or failure
     */
    suspend fun refreshBinDataIfNeeded(): Result<String> = withContext(Dispatchers.IO) {
        if (!shouldDownloadFullFile()) {
            val path = getCachedBinDataPath()
            return@withContext if (path != null) Result.success(path) else Result.failure(Exception("Cache path is null"))
        }
        downloadAndCacheBinData()
    }

    /**
     * Downloads the latest BIN data from CloudFront and caches it.
     * Persists Last-Modified and ETag from response and updates last fetch time.
     *
     * @return Result containing the file path if successful, or error
     */
    suspend fun downloadAndCacheBinData(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = httpClient.get(CLOUDFRONT_URL)
            val content: String = response.body()
            val lastModified = response.headers[HttpHeaders.LastModified]
            val etag = response.headers[HttpHeaders.ETag]

            val filesDir = try { context.filesDir } catch (e: Exception) { null }
                ?: return@withContext Result.failure(Exception("filesDir is null"))

            val cachedFile = try {
                File(filesDir, CACHED_FILENAME)
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }

            FileWriter(cachedFile).use { writer ->
                writer.write(content)
            }

            prefs.edit()
                .putString(KEY_LAST_MODIFIED, lastModified)
                .putString(KEY_ETAG, etag)
                .putLong(KEY_LAST_FETCH_TIME_MS, System.currentTimeMillis())
                .apply()

            Result.success(cachedFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Performs HEAD request and returns cache-relevant headers (Last-Modified preferred, ETag fallback).
     */
    private suspend fun fetchRemoteCacheHeaders(): CacheHeaders? = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = httpClient.head(CLOUDFRONT_URL)
            val lastModified = response.headers[HttpHeaders.LastModified]
            val etag = response.headers[HttpHeaders.ETag]
            if (lastModified != null || etag != null) {
                CacheHeaders(lastModified, etag)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private data class CacheHeaders(val lastModified: String?, val etag: String?)
}

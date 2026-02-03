package com.paydock.feature.card.data.cache

import android.content.Context
import com.paydock.core.presentation.extensions.readTextFromAsset
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

/**
 * Manages downloading and caching of BIN data file from CloudFront.
 *
 * @param context Android context for file operations
 * @param httpClient Ktor HTTP client for downloading
 */
internal class BinDataCacheManager(
    private val context: Context,
    private val httpClient: HttpClient
) {
    companion object {
        private const val CLOUDFRONT_URL = "https://d192wsfqzdoj1n.cloudfront.net/bin-data-au-v1.json"
        private const val ASSET_FILENAME = "card-schemes.json"
        private const val CACHED_FILENAME = "ard-schemes-cached.json"
    }

    /**
     * Gets the path to the cached BIN data file.
     *
     * @return File path to cached file, or null if not cached
     */
    fun getCachedBinDataPath(): String? {
        val cachedFile = File(context.filesDir, CACHED_FILENAME)
        return if (cachedFile.exists() && cachedFile.length() > 0) {
            cachedFile.absolutePath
        } else {
            null
        }
    }

    /**
     * Gets the BIN data file content, preferring cached version over asset.
     *
     * @return The file content as a string
     */
    fun getBinDataFileContent(): String {
        // Try cached file first
        val cachedPath = getCachedBinDataPath()
        if (cachedPath != null) {
            try {
                val cachedFile = File(cachedPath)
                return cachedFile.readText()
            } catch (e: Exception) {
                // Fall through to asset if cache read fails
            }
        }

        // Fallback to bundled asset
        return context.readTextFromAsset(ASSET_FILENAME)
    }

    /**
     * Downloads the latest BIN data from CloudFront and caches it.
     *
     * This operation runs asynchronously and does not block. If download fails,
     * the cached file (if exists) or asset file will be used.
     *
     * @return Result containing the file path if successful, or error
     */
    suspend fun downloadAndCacheBinData(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = httpClient.get(CLOUDFRONT_URL)
            val content: String = response.body()

            // Write to cache file
            val cachedFile = File(context.filesDir, CACHED_FILENAME)
            FileWriter(cachedFile).use { writer ->
                writer.write(content)
            }

            Result.success(cachedFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.paydock.binprocessor.data.cache

import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import com.paydock.binprocessor.BaseUnitTest
import com.paydock.binprocessor.BinDataTestConstants
import com.paydock.binprocessor.mockBinDataEtagOnlyModule
import com.paydock.binprocessor.mockBinDataFailureModule
import com.paydock.binprocessor.mockBinDataSuccessModule
import io.ktor.client.HttpClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.test.inject
import java.io.ByteArrayInputStream
import java.io.File

/**
 * Unit tests for [BinDataCacheManager].
 *
 * Tests verify the caching logic for BIN data including:
 * - Cache vs asset fallback behavior
 * - HEAD request comparison for cache freshness
 * - Download and cache persistence
 * - Foreground refresh timing logic
 */
internal class BinDataCacheManagerTest : BaseUnitTest() {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockPrefsEditor: SharedPreferences.Editor
    private lateinit var mockAssetManager: AssetManager
    private lateinit var filesDir: File

    private val assetBinDataContent = """{"v":0,"s":{"v":"visa-asset"},"2":{},"4":{},"6":{},"r":{"2":[],"4":[],"6":[]}}"""

    @Before
    override fun setUpMocks() {
        super.setUpMocks()

        filesDir = tempFolder.newFolder("files")

        mockPrefsEditor = mockk(relaxed = true) {
            every { putString(any(), any()) } returns this
            every { putLong(any(), any()) } returns this
            every { apply() } returns Unit
        }

        mockPrefs = mockk {
            every { getString(any(), any()) } returns null
            every { getLong(any(), any()) } returns 0L
            every { edit() } returns mockPrefsEditor
        }

        mockAssetManager = mockk {
            every { open("card-schemes.json") } answers { ByteArrayInputStream(assetBinDataContent.toByteArray()) }
        }

        mockContext = mockk {
            every { filesDir } returns this@BinDataCacheManagerTest.filesDir
            every { getSharedPreferences("paydock_bin_cache", Context.MODE_PRIVATE) } returns mockPrefs
            every { assets } returns mockAssetManager
        }

        startKoin { modules(emptyList()) }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun createCacheManagerWithSuccessClient(): BinDataCacheManager {
        loadKoinModules(mockBinDataSuccessModule)
        val httpClient: HttpClient by inject(named("binDataHttpClient"))
        return BinDataCacheManager(mockContext, httpClient)
    }

    private fun createCacheManagerWithFailureClient(): BinDataCacheManager {
        loadKoinModules(mockBinDataFailureModule)
        val httpClient: HttpClient by inject(named("binDataHttpClient"))
        return BinDataCacheManager(mockContext, httpClient)
    }

    private fun createCacheManagerWithEtagOnlyClient(): BinDataCacheManager {
        loadKoinModules(mockBinDataEtagOnlyModule)
        val httpClient: HttpClient by inject(named("binDataHttpClient"))
        return BinDataCacheManager(mockContext, httpClient)
    }

    // ===========================================
    // getCachedBinDataPath Tests
    // ===========================================

    @Test
    fun `getCachedBinDataPath returns null when no cached file exists`() {
        val cacheManager = createCacheManagerWithSuccessClient()

        val result = cacheManager.getCachedBinDataPath()

        assertNull(result)
    }

    @Test
    fun `getCachedBinDataPath returns path when cached file exists and has content`() {
        val cachedFile = File(filesDir, "card-schemes-cached.json")
        cachedFile.writeText(BinDataTestConstants.MOCK_BIN_DATA_CONTENT)

        val cacheManager = createCacheManagerWithSuccessClient()

        val result = cacheManager.getCachedBinDataPath()

        assertNotNull(result)
        assertEquals(cachedFile.absolutePath, result)
    }

    @Test
    fun `getCachedBinDataPath returns null when cached file is empty`() {
        val cachedFile = File(filesDir, "card-schemes-cached.json")
        cachedFile.writeText("")

        val cacheManager = createCacheManagerWithSuccessClient()

        val result = cacheManager.getCachedBinDataPath()

        assertNull(result)
    }

    // ===========================================
    // getBinDataFileContent Tests
    // ===========================================

    @Test
    fun `getBinDataFileContent returns cached file content when cache exists and is valid`() {
        val cachedFile = File(filesDir, "card-schemes-cached.json")
        cachedFile.writeText(BinDataTestConstants.MOCK_BIN_DATA_CONTENT)

        val cacheManager = createCacheManagerWithSuccessClient()

        val result = cacheManager.getBinDataFileContent()

        assertEquals(BinDataTestConstants.MOCK_BIN_DATA_CONTENT, result)
    }

    @Test
    fun `getBinDataFileContent returns asset content when no cache exists`() {
        val cacheManager = createCacheManagerWithSuccessClient()

        val result = cacheManager.getBinDataFileContent()

        assertEquals(assetBinDataContent, result)
    }

    @Test
    fun `getBinDataFileContent returns asset content when cache file is corrupted or unreadable`() {
        val cachedFile = File(filesDir, "card-schemes-cached.json")
        cachedFile.writeText(BinDataTestConstants.MOCK_BIN_DATA_CONTENT)
        cachedFile.setReadable(false)

        // Skip when running as root (e.g. CI): setReadable(false) doesn't prevent root from reading
        Assume.assumeTrue(
            "Skipping: file remains readable after setReadable(false) - likely running as root",
            !cachedFile.canRead()
        )

        val cacheManager = createCacheManagerWithSuccessClient()

        val result = cacheManager.getBinDataFileContent()

        assertEquals(assetBinDataContent, result)

        cachedFile.setReadable(true)
    }

    // ===========================================
    // shouldRefreshOnForeground Tests
    // ===========================================

    @Test
    fun `shouldRefreshOnForeground returns true when no cache exists`() {
        val cacheManager = createCacheManagerWithSuccessClient()

        val result = cacheManager.shouldRefreshOnForeground()

        assertTrue(result)
    }

    @Test
    fun `shouldRefreshOnForeground returns true when last fetch time is zero`() {
        val cachedFile = File(filesDir, "card-schemes-cached.json")
        cachedFile.writeText(BinDataTestConstants.MOCK_BIN_DATA_CONTENT)

        every { mockPrefs.getLong("bin_last_fetch_time_ms", 0L) } returns 0L

        val cacheManager = createCacheManagerWithSuccessClient()

        val result = cacheManager.shouldRefreshOnForeground()

        assertTrue(result)
    }

    @Test
    fun `shouldRefreshOnForeground returns true when last fetch was more than 1 hour ago`() {
        val cachedFile = File(filesDir, "card-schemes-cached.json")
        cachedFile.writeText(BinDataTestConstants.MOCK_BIN_DATA_CONTENT)

        val twoHoursAgo = System.currentTimeMillis() - (2 * 60 * 60 * 1000L)
        every { mockPrefs.getLong("bin_last_fetch_time_ms", 0L) } returns twoHoursAgo

        val cacheManager = createCacheManagerWithSuccessClient()

        val result = cacheManager.shouldRefreshOnForeground()

        assertTrue(result)
    }

    @Test
    fun `shouldRefreshOnForeground returns false when last fetch was less than 1 hour ago`() {
        val cachedFile = File(filesDir, "card-schemes-cached.json")
        cachedFile.writeText(BinDataTestConstants.MOCK_BIN_DATA_CONTENT)

        val thirtyMinutesAgo = System.currentTimeMillis() - (30 * 60 * 1000L)
        every { mockPrefs.getLong("bin_last_fetch_time_ms", 0L) } returns thirtyMinutesAgo

        val cacheManager = createCacheManagerWithSuccessClient()

        val result = cacheManager.shouldRefreshOnForeground()

        assertFalse(result)
    }

    @Test
    fun `shouldRefreshOnForeground returns true when cache exists but exactly 1 hour has passed`() {
        val cachedFile = File(filesDir, "card-schemes-cached.json")
        cachedFile.writeText(BinDataTestConstants.MOCK_BIN_DATA_CONTENT)

        val exactlyOneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000L)
        every { mockPrefs.getLong("bin_last_fetch_time_ms", 0L) } returns exactlyOneHourAgo

        val cacheManager = createCacheManagerWithSuccessClient()

        val result = cacheManager.shouldRefreshOnForeground()

        assertTrue(result)
    }

    // ===========================================
    // shouldDownloadFullFile Tests
    // ===========================================

    @Test
    fun `shouldDownloadFullFile returns true when no cache exists`() = runTest {
        val cacheManager = createCacheManagerWithSuccessClient()

        val result = cacheManager.shouldDownloadFullFile()

        assertTrue(result)
    }

    @Test
    fun `shouldDownloadFullFile returns true when Last-Modified header differs from stored`() = runTest {
        val cachedFile = File(filesDir, "card-schemes-cached.json")
        cachedFile.writeText(BinDataTestConstants.MOCK_BIN_DATA_CONTENT)

        every { mockPrefs.getString("bin_last_modified", null) } returns BinDataTestConstants.MOCK_LAST_MODIFIED_OLD
        every { mockPrefs.getString("bin_etag", null) } returns null

        val cacheManager = createCacheManagerWithSuccessClient()

        val result = cacheManager.shouldDownloadFullFile()

        assertTrue(result)
    }

    @Test
    fun `shouldDownloadFullFile returns true when ETag differs from stored`() = runTest {
        val cachedFile = File(filesDir, "card-schemes-cached.json")
        cachedFile.writeText(BinDataTestConstants.MOCK_BIN_DATA_CONTENT)

        every { mockPrefs.getString("bin_last_modified", null) } returns null
        every { mockPrefs.getString("bin_etag", null) } returns BinDataTestConstants.MOCK_ETAG_OLD

        val cacheManager = createCacheManagerWithEtagOnlyClient()

        val result = cacheManager.shouldDownloadFullFile()

        assertTrue(result)
    }

    @Test
    fun `shouldDownloadFullFile returns false when Last-Modified header matches stored`() = runTest {
        val cachedFile = File(filesDir, "card-schemes-cached.json")
        cachedFile.writeText(BinDataTestConstants.MOCK_BIN_DATA_CONTENT)

        every { mockPrefs.getString("bin_last_modified", null) } returns BinDataTestConstants.MOCK_LAST_MODIFIED
        every { mockPrefs.getString("bin_etag", null) } returns null

        val cacheManager = createCacheManagerWithSuccessClient()

        val result = cacheManager.shouldDownloadFullFile()

        assertFalse(result)
    }

    @Test
    fun `shouldDownloadFullFile returns false when ETag matches stored`() = runTest {
        val cachedFile = File(filesDir, "card-schemes-cached.json")
        cachedFile.writeText(BinDataTestConstants.MOCK_BIN_DATA_CONTENT)

        every { mockPrefs.getString("bin_last_modified", null) } returns null
        every { mockPrefs.getString("bin_etag", null) } returns BinDataTestConstants.MOCK_ETAG

        val cacheManager = createCacheManagerWithEtagOnlyClient()

        val result = cacheManager.shouldDownloadFullFile()

        assertFalse(result)
    }

    @Test
    fun `shouldDownloadFullFile returns false when HEAD request fails - graceful degradation with existing cache`() = runTest {
        val cachedFile = File(filesDir, "card-schemes-cached.json")
        cachedFile.writeText(BinDataTestConstants.MOCK_BIN_DATA_CONTENT)

        val cacheManager = createCacheManagerWithFailureClient()

        val result = cacheManager.shouldDownloadFullFile()

        assertFalse(result)
    }

    // ===========================================
    // downloadAndCacheBinData Tests
    // ===========================================

    @Test
    fun `downloadAndCacheBinData saves file and updates preferences on success`() = runTest {
        val cacheManager = createCacheManagerWithSuccessClient()

        val result = cacheManager.downloadAndCacheBinData()

        assertTrue(result.isSuccess)

        val cachedFile = File(filesDir, "card-schemes-cached.json")
        assertTrue(cachedFile.exists())
        assertEquals(BinDataTestConstants.MOCK_BIN_DATA_CONTENT, cachedFile.readText())

        verify { mockPrefsEditor.putString("bin_last_modified", BinDataTestConstants.MOCK_LAST_MODIFIED) }
        verify { mockPrefsEditor.putString("bin_etag", BinDataTestConstants.MOCK_ETAG) }
        verify { mockPrefsEditor.putLong(eq("bin_last_fetch_time_ms"), any()) }
        verify { mockPrefsEditor.apply() }
    }

    @Test
    fun `downloadAndCacheBinData returns failure when network request fails`() = runTest {
        val cacheManager = createCacheManagerWithFailureClient()

        val result = cacheManager.downloadAndCacheBinData()

        assertTrue(result.isFailure)
    }

    // ===========================================
    // refreshBinDataIfNeeded Tests
    // ===========================================

    @Test
    fun `refreshBinDataIfNeeded returns cached path when download not needed`() = runTest {
        val cachedFile = File(filesDir, "card-schemes-cached.json")
        cachedFile.writeText(BinDataTestConstants.MOCK_BIN_DATA_CONTENT)

        every { mockPrefs.getString("bin_last_modified", null) } returns BinDataTestConstants.MOCK_LAST_MODIFIED

        val cacheManager = createCacheManagerWithSuccessClient()

        val result = cacheManager.refreshBinDataIfNeeded()

        assertTrue(result.isSuccess)
        assertEquals(cachedFile.absolutePath, result.getOrNull())
    }

    @Test
    fun `refreshBinDataIfNeeded downloads and returns new path when no cache exists`() = runTest {
        every { mockPrefs.getString("bin_last_modified", null) } returns null
        every { mockPrefs.getString("bin_etag", null) } returns null

        val cacheManager = createCacheManagerWithSuccessClient()

        val result = cacheManager.refreshBinDataIfNeeded()

        assertTrue(result.isSuccess)

        val cachedFile = File(filesDir, "card-schemes-cached.json")
        assertTrue(cachedFile.exists())
    }
}

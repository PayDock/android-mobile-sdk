package com.paydock.binprocessor.domain.repository

import com.paydock.binprocessor.BinDataTestConstants
import com.paydock.binprocessor.data.cache.BinDataCacheManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [BinDataRepositoryImpl].
 *
 * Tests verify that the repository correctly delegates to [BinDataCacheManager]
 * and properly parses BIN data JSON into [BinDataResponse].
 */
internal class BinDataRepositoryImplTest {

    private lateinit var mockCacheManager: BinDataCacheManager
    private lateinit var repository: BinDataRepositoryImpl

    @Before
    fun setUp() {
        mockCacheManager = mockk(relaxed = true)
        repository = BinDataRepositoryImpl(mockCacheManager)
    }

    // ===========================================
    // getBinDataContent Tests
    // ===========================================

    @Test
    fun `getBinDataContent delegates to cacheManager`() {
        val expectedContent = BinDataTestConstants.MOCK_BIN_DATA_CONTENT
        every { mockCacheManager.getBinDataFileContent() } returns expectedContent

        val result = repository.getBinDataContent()

        assertEquals(expectedContent, result)
        verify(exactly = 1) { mockCacheManager.getBinDataFileContent() }
    }

    // ===========================================
    // getCachedBinDataPath Tests
    // ===========================================

    @Test
    fun `getCachedBinDataPath delegates to cacheManager and returns path`() {
        val expectedPath = "/path/to/cached/file.json"
        every { mockCacheManager.getCachedBinDataPath() } returns expectedPath

        val result = repository.getCachedBinDataPath()

        assertEquals(expectedPath, result)
        verify(exactly = 1) { mockCacheManager.getCachedBinDataPath() }
    }

    @Test
    fun `getCachedBinDataPath returns null when no cache exists`() {
        every { mockCacheManager.getCachedBinDataPath() } returns null

        val result = repository.getCachedBinDataPath()

        assertNull(result)
        verify(exactly = 1) { mockCacheManager.getCachedBinDataPath() }
    }

    // ===========================================
    // shouldRefreshOnForeground Tests
    // ===========================================

    @Test
    fun `shouldRefreshOnForeground delegates to cacheManager`() {
        every { mockCacheManager.shouldRefreshOnForeground() } returns true

        val result = repository.shouldRefreshOnForeground()

        assertTrue(result)
        verify(exactly = 1) { mockCacheManager.shouldRefreshOnForeground() }
    }

    // ===========================================
    // shouldDownloadFullFile Tests
    // ===========================================

    @Test
    fun `shouldDownloadFullFile delegates to cacheManager`() = runTest {
        coEvery { mockCacheManager.shouldDownloadFullFile() } returns true

        val result = repository.shouldDownloadFullFile()

        assertTrue(result)
        coVerify(exactly = 1) { mockCacheManager.shouldDownloadFullFile() }
    }

    // ===========================================
    // refreshBinDataIfNeeded Tests
    // ===========================================

    @Test
    fun `refreshBinDataIfNeeded delegates to cacheManager on success`() = runTest {
        val expectedPath = "/path/to/cached/file.json"
        coEvery { mockCacheManager.refreshBinDataIfNeeded() } returns Result.success(expectedPath)

        val result = repository.refreshBinDataIfNeeded()

        assertTrue(result.isSuccess)
        assertEquals(expectedPath, result.getOrNull())
        coVerify(exactly = 1) { mockCacheManager.refreshBinDataIfNeeded() }
    }

    @Test
    fun `refreshBinDataIfNeeded delegates to cacheManager on failure`() = runTest {
        val exception = Exception("Network error")
        coEvery { mockCacheManager.refreshBinDataIfNeeded() } returns Result.failure(exception)

        val result = repository.refreshBinDataIfNeeded()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { mockCacheManager.refreshBinDataIfNeeded() }
    }

    // ===========================================
    // downloadAndCacheBinData Tests
    // ===========================================

    @Test
    fun `downloadAndCacheBinData delegates to cacheManager on success`() = runTest {
        val expectedPath = "/path/to/cached/file.json"
        coEvery { mockCacheManager.downloadAndCacheBinData() } returns Result.success(expectedPath)

        val result = repository.downloadAndCacheBinData()

        assertTrue(result.isSuccess)
        assertEquals(expectedPath, result.getOrNull())
        coVerify(exactly = 1) { mockCacheManager.downloadAndCacheBinData() }
    }

    @Test
    fun `downloadAndCacheBinData delegates to cacheManager on failure`() = runTest {
        val exception = Exception("Network error")
        coEvery { mockCacheManager.downloadAndCacheBinData() } returns Result.failure(exception)

        val result = repository.downloadAndCacheBinData()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { mockCacheManager.downloadAndCacheBinData() }
    }

    // ===========================================
    // getBinData Tests
    // ===========================================

    @Test
    fun `getBinData parses JSON and returns BinDataResponse`() {
        val jsonContent = """{"v":1,"s":{"v":"visa"},"2":{},"4":{},"6":{},"r":{"2":[],"4":[],"6":[]}}"""
        every { mockCacheManager.getBinDataFileContent() } returns jsonContent

        val result = repository.getBinData()

        assertNotNull(result)
        assertEquals(1, result.version)
        assertEquals("visa", result.schemes["v"])
        verify(exactly = 1) { mockCacheManager.getBinDataFileContent() }
    }

    @Test
    fun `getBinData handles complex BIN data JSON`() {
        val complexJson = """
            {
                "v": 2,
                "s": {"v": "visa", "m": "mastercard"},
                "2": {"4": "v", "51": "m"},
                "4": {"6011": "d"},
                "6": {"411111": "v"},
                "r": {
                    "2": [["53", "55", "m"]],
                    "4": [],
                    "6": []
                }
            }
        """.trimIndent()
        every { mockCacheManager.getBinDataFileContent() } returns complexJson

        val result = repository.getBinData()

        assertNotNull(result)
        assertEquals(2, result.version)
        assertEquals("visa", result.schemes["v"])
        assertEquals("mastercard", result.schemes["m"])
        assertEquals("v", result.prefix2["4"])
        assertEquals("m", result.prefix2["51"])
        assertEquals("d", result.prefix4["6011"])
        assertEquals("v", result.prefix6["411111"])
        assertEquals(1, result.ranges.range2.size)
    }

    @Test
    fun `getBinData returns empty collections for missing fields`() {
        val minimalJson = """{"v": 1}"""
        every { mockCacheManager.getBinDataFileContent() } returns minimalJson

        val result = repository.getBinData()

        assertNotNull(result)
        assertEquals(1, result.version)
        assertTrue(result.schemes.isEmpty())
        assertTrue(result.prefix2.isEmpty())
        assertTrue(result.prefix4.isEmpty())
        assertTrue(result.prefix6.isEmpty())
        assertTrue(result.ranges.range2.isEmpty())
        assertTrue(result.ranges.range4.isEmpty())
        assertTrue(result.ranges.range6.isEmpty())
    }
}

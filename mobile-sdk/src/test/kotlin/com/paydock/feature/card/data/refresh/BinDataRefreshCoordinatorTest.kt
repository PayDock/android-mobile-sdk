package com.paydock.feature.card.data.refresh

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.paydock.core.BaseUnitTest
import com.paydock.feature.card.data.cache.BinDataCacheManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [BinDataRefreshCoordinator].
 *
 * Tests verify the lifecycle-based refresh logic including:
 * - SDK initialization refresh trigger
 * - App foreground refresh when cache is stale or missing
 * - Skipping refresh when cache is fresh
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class BinDataRefreshCoordinatorTest : BaseUnitTest() {

    private lateinit var mockCacheManager: BinDataCacheManager
    private lateinit var mockLifecycleOwner: LifecycleOwner
    private lateinit var mockLifecycle: Lifecycle
    private lateinit var testScope: CoroutineScope

    @Before
    fun setUp() {
        mockCacheManager = mockk(relaxed = true)
        mockLifecycleOwner = mockk(relaxed = true)
        mockLifecycle = mockk(relaxed = true)
        testScope = CoroutineScope(UnconfinedTestDispatcher())

        mockkStatic(ProcessLifecycleOwner::class)
        every { ProcessLifecycleOwner.get() } returns mockLifecycleOwner
        every { mockLifecycleOwner.lifecycle } returns mockLifecycle
    }

    @After
    fun tearDown() {
        unmockkStatic(ProcessLifecycleOwner::class)
    }

    // ===========================================
    // refreshAtSdkInit Tests
    // ===========================================

    @Test
    fun `refreshAtSdkInit triggers refreshBinDataIfNeeded`() = runTest {
        coEvery { mockCacheManager.refreshBinDataIfNeeded() } returns Result.success("/path/to/cached/file")

        val coordinator = BinDataRefreshCoordinator(mockCacheManager, testScope)

        coordinator.refreshAtSdkInit()

        coVerify(exactly = 1) { mockCacheManager.refreshBinDataIfNeeded() }
    }

    @Test
    fun `refreshAtSdkInit handles refresh failure gracefully`() = runTest {
        coEvery { mockCacheManager.refreshBinDataIfNeeded() } returns Result.failure(Exception("Network error"))

        val coordinator = BinDataRefreshCoordinator(mockCacheManager, testScope)

        coordinator.refreshAtSdkInit()

        coVerify(exactly = 1) { mockCacheManager.refreshBinDataIfNeeded() }
    }

    // ===========================================
    // onStart (Foreground) Tests
    // ===========================================

    @Test
    fun `onStart triggers refresh when shouldRefreshOnForeground returns true`() = runTest {
        every { mockCacheManager.shouldRefreshOnForeground() } returns true
        coEvery { mockCacheManager.refreshBinDataIfNeeded() } returns Result.success("/path/to/cached/file")

        val coordinator = BinDataRefreshCoordinator(mockCacheManager, testScope)

        coordinator.onStart(mockLifecycleOwner)

        verify(exactly = 1) { mockCacheManager.shouldRefreshOnForeground() }
        coVerify(exactly = 1) { mockCacheManager.refreshBinDataIfNeeded() }
    }

    @Test
    fun `onStart skips refresh when shouldRefreshOnForeground returns false`() = runTest {
        every { mockCacheManager.shouldRefreshOnForeground() } returns false

        val coordinator = BinDataRefreshCoordinator(mockCacheManager, testScope)

        coordinator.onStart(mockLifecycleOwner)

        verify(exactly = 1) { mockCacheManager.shouldRefreshOnForeground() }
        coVerify(exactly = 0) { mockCacheManager.refreshBinDataIfNeeded() }
    }

    @Test
    fun `onStart handles refresh failure gracefully when refresh is needed`() = runTest {
        every { mockCacheManager.shouldRefreshOnForeground() } returns true
        coEvery { mockCacheManager.refreshBinDataIfNeeded() } returns Result.failure(Exception("Network error"))

        val coordinator = BinDataRefreshCoordinator(mockCacheManager, testScope)

        coordinator.onStart(mockLifecycleOwner)

        verify(exactly = 1) { mockCacheManager.shouldRefreshOnForeground() }
        coVerify(exactly = 1) { mockCacheManager.refreshBinDataIfNeeded() }
    }

    // ===========================================
    // Multiple Foreground Events Tests
    // ===========================================

    @Test
    fun `multiple onStart calls each check shouldRefreshOnForeground`() = runTest {
        every { mockCacheManager.shouldRefreshOnForeground() } returnsMany listOf(true, false, true)
        coEvery { mockCacheManager.refreshBinDataIfNeeded() } returns Result.success("/path/to/cached/file")

        val coordinator = BinDataRefreshCoordinator(mockCacheManager, testScope)

        coordinator.onStart(mockLifecycleOwner)
        coordinator.onStart(mockLifecycleOwner)
        coordinator.onStart(mockLifecycleOwner)

        verify(exactly = 3) { mockCacheManager.shouldRefreshOnForeground() }
        coVerify(exactly = 2) { mockCacheManager.refreshBinDataIfNeeded() }
    }

    // ===========================================
    // Companion Object Tests
    // ===========================================

    @Test
    fun `applicationScope creates non-null CoroutineScope`() {
        val scope = BinDataRefreshCoordinator.applicationScope()

        assertNotNull(scope)
    }
}

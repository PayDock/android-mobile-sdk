package com.paydock

import android.app.Application
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ProcessLifecycleOwner
import com.paydock.core.BaseUnitTest
import com.paydock.core.domain.model.Environment
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.stopKoin
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class MobileSDKTest : BaseUnitTest() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Application

    @Before
    override fun setUpMocks() {
        super.setUpMocks()

        // Mock ProcessLifecycleOwner for BinDataRefreshCoordinator
        mockkObject(ProcessLifecycleOwner)
        val processLifecycleOwner = mockk<ProcessLifecycleOwner>(relaxed = true)
        val lifecycleRegistry = LifecycleRegistry(processLifecycleOwner)
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        every { ProcessLifecycleOwner.get() } returns processLifecycleOwner
        every { processLifecycleOwner.lifecycle } returns lifecycleRegistry

        // Mock Application so Koin can resolve androidApplication() in presentationModule
        context = mockk<Application>(relaxed = true)
        every { context.applicationContext } returns context

        // Mock filesDir for BinDataCacheManager.getCachedBinDataPath()
        val tempDir = File(System.getProperty("java.io.tmpdir"), "paydock_test")
        tempDir.mkdirs()
        every { context.filesDir } returns tempDir

        // Mock SharedPreferences for BinDataCacheManager
        val sharedPrefs = mockk<SharedPreferences>(relaxed = true)
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { sharedPrefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.apply() } returns Unit
        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs
    }

    @After
    fun resetMocks() {
        MobileSDK.reset() // Reset MobileSDK before each test
    }

    @After
    fun tearDownKoin() {
        // As the SDK will startKoin, we need to ensure that after each test we stop koin to be able to restart it in each test
        stopKoin()
    }

    @Test
    fun `initializeMobileSDK should initialize MobileSDK with provided configuration`() {
        val environment = Environment.SANDBOX

        context.initializeMobileSDK(environment)

        val sdk = MobileSDK.getInstance()
        assertNotNull(sdk)
        assertEquals(environment, sdk.environment)
    }

    @Test
    fun `initializeMobileSDK without environment should initialize MobileSDK with default configuration`() {
        context.initializeMobileSDK()

        val sdk = MobileSDK.getInstance()
        assertNotNull(sdk)
        assertEquals(Environment.PRODUCTION, sdk.environment)
    }

    @Test
    fun `initializeMobileSDK twice should throw an exception`() {
        context.initializeMobileSDK()

        assertFailsWith<IllegalStateException> {
            context.initializeMobileSDK()
        }
    }

    @Test
    fun `getInstance without initialization should throw an exception`() {
        assertFailsWith<IllegalStateException> {
            MobileSDK.getInstance()
        }
    }

    @Test
    fun `initialize should initialize MobileSDK with provided configuration`() {
        val environment = Environment.SANDBOX

        MobileSDK.initialize(context, environment, false)

        val sdk = MobileSDK.getInstance()
        assertNotNull(sdk)
        assertEquals(environment, sdk.environment)
    }

    @Test
    fun `initialize without environment and theme should initialize MobileSDK with default configuration`() {

        MobileSDK.initialize(context)

        val sdk = MobileSDK.getInstance()
        assertNotNull(sdk)
        assertEquals(Environment.PRODUCTION, sdk.environment)
    }

    @Test
    fun `initialize twice should throw an exception`() {
        MobileSDK.initialize(context)

        assertFailsWith<IllegalStateException> {
            MobileSDK.initialize(context)
        }
    }

    @Test
    fun `MobileSDK Builder should build and initialize MobileSDK with provided configuration`() {
        val environment = Environment.STAGING

        val sdk = MobileSDK.Builder()
            .environment(environment)
            .build(context)

        assertNotNull(sdk)
        assertEquals(environment, sdk.environment)
    }

    @Test
    fun `MobileSDK Builder without environment should build and initialize MobileSDK with default configuration`() {

        val sdk = MobileSDK.Builder()
            .build(context)

        assertNotNull(sdk)
        assertEquals(Environment.PRODUCTION, sdk.environment)
    }

    @Test
    fun `MobileSDK checking if sdk is initialised should return true if sdk is initialised`() {
        assertFalse(MobileSDK.isInitialised())
        val sdk = MobileSDK.Builder()
            .build(context)

        assertNotNull(sdk)
        assertTrue(MobileSDK.isInitialised())
    }

}
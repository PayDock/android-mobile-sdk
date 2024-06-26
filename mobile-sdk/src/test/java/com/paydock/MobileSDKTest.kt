package com.paydock

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.paydock.core.BaseUnitTest
import com.paydock.core.domain.model.Environment
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.stopKoin
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class MobileSDKTest : BaseUnitTest() {

    private lateinit var context: Context

    @Before
    override fun setUpMocks() {
        super.setUpMocks()
        // Mock the Context object
        context = mockk()
        // Configure the getApplicationContext() method to return the mock Context
        every { context.applicationContext } returns context
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
        val publicKey = "sample_public_key"
        val environment = Environment.SANDBOX

        context.initializeMobileSDK(publicKey, environment)

        val sdk = MobileSDK.getInstance()
        assertNotNull(sdk)
        assertEquals(publicKey, sdk.publicKey)
        assertEquals(environment, sdk.environment)
    }

    @Test
    fun `initializeMobileSDK without environment should initialize MobileSDK with default configuration`() {
        val publicKey = "sample_public_key"

        context.initializeMobileSDK(publicKey)

        val sdk = MobileSDK.getInstance()
        assertNotNull(sdk)
        assertEquals(publicKey, sdk.publicKey)
        assertEquals(Environment.PRODUCTION, sdk.environment)
    }

    @Test
    fun `initializeMobileSDK twice should throw an exception`() {
        val publicKey = "sample_public_key"
        context.initializeMobileSDK(publicKey)

        assertFailsWith<IllegalStateException> {
            context.initializeMobileSDK(publicKey)
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
        val publicKey = "sample_public_key"
        val environment = Environment.SANDBOX
        val theme = MobileSDKTheme()

        MobileSDK.initialize(context, publicKey, environment, theme)

        val sdk = MobileSDK.getInstance()
        assertNotNull(sdk)
        assertEquals(publicKey, sdk.publicKey)
        assertEquals(environment, sdk.environment)
        assertEquals(theme, sdk.sdkTheme)
    }

    @Test
    fun `initialize without environment and theme should initialize MobileSDK with default configuration`() {
        val publicKey = "sample_public_key"

        MobileSDK.initialize(context, publicKey)

        val sdk = MobileSDK.getInstance()
        assertNotNull(sdk)
        assertEquals(publicKey, sdk.publicKey)
        assertEquals(Environment.PRODUCTION, sdk.environment)
        assertNotNull(sdk.sdkTheme)
    }

    @Test
    fun `initialize twice should throw an exception`() {
        val publicKey = "sample_public_key"
        MobileSDK.initialize(context, publicKey)

        assertFailsWith<IllegalStateException> {
            MobileSDK.initialize(context, publicKey)
        }
    }

    @Test
    fun `MobileSDK Builder should build and initialize MobileSDK with provided configuration`() {
        val publicKey = "sample_public_key"
        val environment = Environment.STAGING
        val theme = MobileSDKTheme()

        val sdk = MobileSDK.Builder(publicKey)
            .environment(environment)
            .applyTheme(theme)
            .build(context)

        assertNotNull(sdk)
        assertEquals(publicKey, sdk.publicKey)
        assertEquals(environment, sdk.environment)
        assertEquals(theme, sdk.sdkTheme)
    }

    @Test
    fun `MobileSDK Builder without environment should build and initialize MobileSDK with default configuration`() {
        val publicKey = "sample_public_key"

        val sdk = MobileSDK.Builder(publicKey)
            .build(context)

        assertNotNull(sdk)
        assertEquals(publicKey, sdk.publicKey)
        assertEquals(Environment.PRODUCTION, sdk.environment)
    }

    @Test
    fun `MobileSDK update sdk theme should update SDK default configuration`() {
        val publicKey = "sample_public_key"
        val customTheme = MobileSDKTheme(
            colours = MobileSDKTheme.Colours.themeColours(
                light = MobileSDKTheme.Colours.lightThemeColors(
                    primary = Color.Blue
                ),
                dark = MobileSDKTheme.Colours.darkThemeColors(
                    primary = Color.Red
                ),
            )
        )
        val sdk = MobileSDK.Builder(publicKey)
            .build(context)

        assertNotNull(sdk)
        assertNotEquals(sdk.sdkTheme, customTheme)

        sdk.updateTheme(customTheme)

        assertEquals(sdk.sdkTheme, customTheme)

    }

}
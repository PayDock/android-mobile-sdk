package com.paydock.binprocessor

import android.net.Uri
import android.util.Log
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.slot
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.koin.test.KoinTest
import org.koin.test.mock.MockProviderRule
import org.mockito.Mockito
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Base unit test class for bin-processor module tests.
 *
 * Provides common mocking setup for Android classes (Log, Uri, Base64)
 * that are commonly used but not available in unit tests.
 */
abstract class BaseUnitTest : KoinTest {

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        Mockito.mock(clazz.java)
    }

    @Before
    open fun setUpMocks() {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        mockkStatic(Uri::class)

        mockkStatic(android.util.Base64::class)
        val b64Encoded = slot<String>()
        val flags = slot<Int>()
        every { android.util.Base64.decode(capture(b64Encoded), capture(flags)) } coAnswers {
            java.util.Base64.getDecoder().decode(this.args[0] as String)
        }
    }

    @After
    fun tearDownMocks() {
        clearAllMocks()
    }

    fun readResourceFile(path: String): String {
        val file = "src/test/resources/$path"
        return String(Files.readAllBytes(Paths.get(file)))
    }
}

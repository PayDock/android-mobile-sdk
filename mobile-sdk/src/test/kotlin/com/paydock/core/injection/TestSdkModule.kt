package com.paydock.core.injection

import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.utils.TestDispatchersProviderImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.io.File
import kotlin.coroutines.CoroutineContext

internal val testSdkModule = module {
    single {
        val mockContext = Mockito.mock(Context::class.java)
        val tempDir = File(System.getProperty("java.io.tmpdir"), "paydock_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        val mockPrefsEditor = Mockito.mock(SharedPreferences.Editor::class.java).apply {
            whenever(putString(any(), any())).thenReturn(this)
            whenever(putLong(any(), any())).thenReturn(this)
        }
        val mockPrefs = Mockito.mock(SharedPreferences::class.java).apply {
            whenever(getString(any(), any())).thenReturn(null)
            whenever(getLong(any(), any())).thenReturn(0L)
            whenever(edit()).thenReturn(mockPrefsEditor)
        }

        val assetContent = """{"version":1,"schemes":{},"prefix2":{},"prefix4":{},"prefix6":{},"prefix8":{},"ranges":{"range2":[],"range4":[],"range6":[],"range8":[]}}"""
        val mockAssetManager = Mockito.mock(AssetManager::class.java).apply {
            whenever(open(any())).thenAnswer { ByteArrayInputStream(assetContent.toByteArray()) }
        }

        whenever(mockContext.filesDir).thenReturn(tempDir)
        whenever(mockContext.getSharedPreferences(any(), any())).thenReturn(mockPrefs)
        whenever(mockContext.assets).thenReturn(mockAssetManager)

        mockContext
    }

    singleOf(::TestCoroutineScheduler)
    factory<CoroutineDispatcher> { StandardTestDispatcher(scheduler = get()) }
    factory<CoroutineContext> { StandardTestDispatcher(scheduler = get()) }
    single { TestScope(context = get()) }

    single<DispatchersProvider> { TestDispatchersProviderImpl }
}
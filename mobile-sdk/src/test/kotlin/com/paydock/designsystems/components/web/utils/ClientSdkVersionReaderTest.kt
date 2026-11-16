package com.paydock.designsystems.components.web.utils

import android.util.Log
import android.webkit.ValueCallback
import android.webkit.WebView
import com.paydock.MobileSDK
import com.paydock.core.BaseUnitTest
import com.paydock.core.MobileSDKConstants
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

internal class ClientSdkVersionReaderTest : BaseUnitTest() {

    @After
    fun resetSdk() {
        MobileSDK.reset()
    }

    @Test
    fun `a json string is the loaded version`() {
        assertEquals("v1.150.2", ClientSdkVersionReader.parse("\"v1.150.2\""))
    }

    @Test
    fun `null, an empty string, a number, an object and malformed json are no version`() {
        listOf(null, "null", "", "\"\"", "1150", "true", "{\"version\":\"v1.150.2\"}", "[\"v1.150.2\"]", "v1.150.2", "\"v1")
            .forEach { result -> assertNull(ClientSdkVersionReader.parse(result), "result $result") }
    }

    @Test
    fun `a page reporting its version records it on the sdk and logs it at debug level`() {
        val sdk = initialisedSdk()

        readPage(result = "\"v1.150.2\"")

        verify { sdk.loadedClientSdkVersion = "v1.150.2" }
        verify { Log.d(MobileSDKConstants.MOBILE_SDK_TAG, "Client SDK version loaded: v1.150.2") }
    }

    @Test
    fun `a page reporting no version leaves the last kept version unchanged`() {
        val sdk = initialisedSdk()

        listOf("null", "42", "not json").forEach { result -> readPage(result) }

        verify(exactly = 0) { sdk.loadedClientSdkVersion = any() }
        verify(exactly = 0) { Log.d(any(), match { it.startsWith("Client SDK version loaded") }) }
    }

    @Test
    fun `a version reported while the sdk is not initialised is ignored`() {
        readPage(result = "\"v1.150.2\"")

        assertFalse(MobileSDK.isInitialised())
        verify(exactly = 0) { Log.d(any(), match { it.startsWith("Client SDK version loaded") }) }
    }

    @Test
    fun `the page is asked for the client sdk version accessor`() {
        assertEquals("window.paydock && window.paydock.version", ClientSdkVersionReader.VERSION_SCRIPT)
    }

    private fun initialisedSdk(): MobileSDK = mockk<MobileSDK>(relaxed = true).also(MobileSDK::registerInstance)

    private fun readPage(result: String?) {
        val webView = mockk<WebView> {
            every { evaluateJavascript(ClientSdkVersionReader.VERSION_SCRIPT, any()) } answers {
                secondArg<ValueCallback<String?>>().onReceiveValue(result)
            }
        }
        ClientSdkVersionReader.read(webView)
    }
}

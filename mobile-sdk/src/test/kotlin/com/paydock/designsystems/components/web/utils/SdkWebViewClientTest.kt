package com.paydock.designsystems.components.web.utils

import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.paydock.feature.threeDS.standalone.presentation.ui.isEssentialStandalone3DSHost
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SdkWebViewClientTest {

    private val webView: WebView = mockk(relaxed = true)
    private val criticalCodes = listOf(
        WebViewClient.ERROR_HOST_LOOKUP,
        WebViewClient.ERROR_CONNECT,
        WebViewClient.ERROR_TIMEOUT,
        WebViewClient.ERROR_IO,
    )

    @Test
    fun `default policy keeps critical sub-resource network errors fatal`() {
        criticalCodes.forEach { code ->
            val errors = mutableListOf<Int>()
            val client = client(onError = { errors += it })

            client.onReceivedError(webView, request("acs.issuer.example", mainFrame = false), error(code))

            assertEquals(listOf(code), errors, "Code $code should be fatal by default")
        }
    }

    @Test
    fun `standalone 3DS policy ignores critical network errors on third-party sub-resources`() {
        criticalCodes.forEach { code ->
            val errors = mutableListOf<Int>()
            val client = client(onError = { errors += it }, policy = ::isEssentialStandalone3DSHost)

            client.onReceivedError(webView, request("acs.issuer.example", mainFrame = false), error(code))

            assertTrue(errors.isEmpty(), "Code $code on a third-party host should not be fatal")
        }
    }

    @Test
    fun `standalone 3DS policy keeps critical network errors on Paydock and GPayments hosts fatal`() {
        listOf("widget.paydock.com", "api.paydock.com", "paydock.as1.gpayments.net").forEach { host ->
            val errors = mutableListOf<Int>()
            val client = client(onError = { errors += it }, policy = ::isEssentialStandalone3DSHost)

            client.onReceivedError(webView, request(host, mainFrame = false), error(WebViewClient.ERROR_HOST_LOOKUP))

            assertEquals(listOf(WebViewClient.ERROR_HOST_LOOKUP), errors, "$host should be fatal")
        }
    }

    @Test
    fun `main frame errors are fatal regardless of host or policy`() {
        val errors = mutableListOf<Int>()
        val client = client(onError = { errors += it }, policy = { false })

        client.onReceivedError(webView, request("acs.issuer.example", mainFrame = true), error(WebViewClient.ERROR_UNKNOWN))

        assertEquals(listOf(WebViewClient.ERROR_UNKNOWN), errors)
    }

    @Test
    fun `non-critical sub-resource errors are never fatal`() {
        val errors = mutableListOf<Int>()
        val client = client(onError = { errors += it })

        client.onReceivedError(webView, request("widget.paydock.com", mainFrame = false), error(WebViewClient.ERROR_UNKNOWN))

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `only the first fatal error in a session is reported`() {
        val errors = mutableListOf<Int>()
        val client = client(onError = { errors += it })

        client.onReceivedError(webView, request("widget.paydock.com", mainFrame = false), error(WebViewClient.ERROR_CONNECT))
        client.onReceivedError(webView, request("api.paydock.com", mainFrame = false), error(WebViewClient.ERROR_TIMEOUT))

        assertEquals(listOf(WebViewClient.ERROR_CONNECT), errors)
    }

    @Test
    fun `essential host matching is domain based and case insensitive`() {
        assertTrue(isEssentialStandalone3DSHost("paydock.com"))
        assertTrue(isEssentialStandalone3DSHost("Widget.Paydock.com."))
        assertTrue(isEssentialStandalone3DSHost("paydock-test.api.as1.gpayments.net"))
        assertTrue(isEssentialStandalone3DSHost(null))
        assertFalse(isEssentialStandalone3DSHost("notpaydock.com"))
        assertFalse(isEssentialStandalone3DSHost("paydock.com.evil.example"))
        assertFalse(isEssentialStandalone3DSHost("h.online-metrix.net"))
    }

    private fun client(
        onError: (Int) -> Unit,
        policy: (String?) -> Boolean = { true },
    ) = SdkWebViewClient(
        onWebViewError = { code, _ -> onError(code) },
        delegateToAccompanist = false,
        isSubResourceNetworkErrorFatal = policy,
    )

    private fun request(host: String, mainFrame: Boolean): WebResourceRequest {
        val uri = mockk<Uri> {
            every { this@mockk.host } returns host
            every { this@mockk.toString() } returns "https://$host/resource"
        }
        return mockk {
            every { url } returns uri
            every { isForMainFrame } returns mainFrame
        }
    }

    private fun error(code: Int): WebResourceError = mockk {
        every { errorCode } returns code
    }
}

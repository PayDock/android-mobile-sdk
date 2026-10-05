package com.paydock.designsystems.components.web.utils

import android.net.Uri
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.paydock.feature.threeDS.standalone.presentation.ui.isEssentialStandalone3DSHost
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
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

    @Test
    fun `ignored third-party network errors are logged as warnings with host only`() {
        mockkStatic(Log::class)
        try {
            val message = slot<String>()
            every { Log.w(any(), capture(message)) } returns 0
            val errors = mutableListOf<Int>()
            val client = client(onError = { errors += it }, policy = ::isEssentialStandalone3DSHost)

            client.onReceivedError(
                webView,
                request("h.online-metrix.net", mainFrame = false, path = "/fp/tags.js", query = "session=secret"),
                error(WebViewClient.ERROR_HOST_LOOKUP, "net::ERR_NAME_NOT_RESOLVED"),
            )

            assertTrue(errors.isEmpty())
            verify(exactly = 1) { Log.w(any(), any<String>()) }
            assertTrue(message.captured.contains("code ${WebViewClient.ERROR_HOST_LOOKUP}"))
            assertTrue(message.captured.contains("net::ERR_NAME_NOT_RESOLVED"))
            assertTrue(message.captured.contains("host: h.online-metrix.net"))
            assertFalse(message.captured.contains("/fp/tags.js"))
            assertFalse(message.captured.contains("secret"))
        } finally {
            unmockkStatic(Log::class)
        }
    }

    @Test
    fun `non-critical sub-resource errors are not logged as warnings`() {
        mockkStatic(Log::class)
        try {
            every { Log.w(any(), any<String>()) } returns 0
            every { Log.d(any(), any()) } returns 0
            val client = client(onError = {}, policy = ::isEssentialStandalone3DSHost)

            client.onReceivedError(webView, request("acs.issuer.example", mainFrame = false), error(WebViewClient.ERROR_UNKNOWN))

            verify(exactly = 0) { Log.w(any(), any<String>()) }
        } finally {
            unmockkStatic(Log::class)
        }
    }

    @Test
    fun `fatal errors are logged at error level without query string`() {
        mockkStatic(Log::class)
        try {
            val message = slot<String>()
            every { Log.e(any(), capture(message)) } returns 0
            val client = client(onError = {}, policy = ::isEssentialStandalone3DSHost)

            client.onReceivedError(
                webView,
                request("paydock-test.as1.gpayments.net", mainFrame = false, path = "/api/v2/brw/result/mon", query = "t=token"),
                error(WebViewClient.ERROR_CONNECT, "net::ERR_CONNECTION_RESET"),
            )

            verify(exactly = 1) { Log.e(any(), any<String>()) }
            assertTrue(message.captured.contains("net::ERR_CONNECTION_RESET"))
            assertTrue(message.captured.contains("https://paydock-test.as1.gpayments.net/api/v2/brw/result/mon"))
            assertFalse(message.captured.contains("token"))
        } finally {
            unmockkStatic(Log::class)
        }
    }

    @Test
    fun `failed request description drops the query string`() {
        val uri = request("paydock-test.as1.gpayments.net", mainFrame = false, path = "/api/v2/brw/result/mon", query = "t=token").url

        assertEquals("https://paydock-test.as1.gpayments.net/api/v2/brw/result/mon", SdkWebViewClient.describeFailedRequest(uri))
        assertEquals("unknown", SdkWebViewClient.describeFailedRequest(null))
    }

    @Test
    fun `url description drops query string and fragment`() {
        assertEquals(
            "https://paydock-test.as1.gpayments.net/api/v2/brw/callback",
            SdkWebViewClient.describeUrl("https://paydock-test.as1.gpayments.net/api/v2/brw/callback?transId=1&t=token#x"),
        )
        assertEquals("https://paydock.com/", SdkWebViewClient.describeUrl("https://paydock.com/#token"))
        assertEquals("unknown", SdkWebViewClient.describeUrl(null))
    }

    @Test
    fun `query strings are redacted from urls inside free text`() {
        assertEquals(
            "LOG: Collecting browser info ... at https://paydock-test.as1.gpayments.net/api/v2/brw/callback done",
            SdkWebViewClient.redactUrlQueries(
                "LOG: Collecting browser info ... at https://paydock-test.as1.gpayments.net/api/v2/brw/callback?transId=1&t=token done"
            ),
        )
        assertEquals("no urls here", SdkWebViewClient.redactUrlQueries("no urls here"))
    }

    private fun client(
        onError: (Int) -> Unit,
        policy: (String?) -> Boolean = { true },
    ) = SdkWebViewClient(
        onWebViewError = { code, _ -> onError(code) },
        delegateToAccompanist = false,
        isSubResourceNetworkErrorFatal = policy,
    )

    private fun request(
        host: String,
        mainFrame: Boolean,
        path: String = "/resource",
        query: String? = null,
    ): WebResourceRequest {
        val uri = mockk<Uri> {
            every { this@mockk.host } returns host
            every { scheme } returns "https"
            every { this@mockk.path } returns path
            every { this@mockk.toString() } returns "https://$host$path" + (query?.let { "?$it" } ?: "")
        }
        return mockk {
            every { url } returns uri
            every { isForMainFrame } returns mainFrame
        }
    }

    private fun error(code: Int, description: String = "net::ERR_FAILED"): WebResourceError = mockk {
        every { errorCode } returns code
        every { this@mockk.description } returns description
    }
}

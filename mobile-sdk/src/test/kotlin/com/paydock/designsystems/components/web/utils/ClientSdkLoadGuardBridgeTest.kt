package com.paydock.designsystems.components.web.utils

import android.webkit.WebViewClient
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ClientSdkLoadGuardBridgeTest {

    private val delegated = mutableListOf<String>()
    private val delegate = object : SdkJSBridge<String>({}) {
        override fun postMessage(eventJson: String) {
            delegated += eventJson
        }
    }

    @Test
    fun `clientSdkLoadFailed reaches onWebViewError once with the script host`() {
        val errors = mutableListOf<Triple<Int, String, String?>>()
        val client = SdkWebViewClient(
            onWebViewError = { code, message, host -> errors += Triple(code, message, host) },
            delegateToAccompanist = false,
        )
        val bridge = ClientSdkLoadGuardBridge(delegate) { client.reportClientSdkLoadFailed(SCRIPT_URL) }

        bridge.postMessage(LOAD_FAILED)
        bridge.postMessage(LOAD_FAILED)

        val expected: List<Triple<Int, String, String?>> = listOf(
            Triple(
                WebViewClient.ERROR_FILE_NOT_FOUND,
                SdkWebViewClient.mapWebViewErrorMessage(WebViewClient.ERROR_FILE_NOT_FOUND),
                "widget.paydock.com"
            )
        )
        assertEquals(expected, errors)
        assertTrue(delegated.isEmpty(), "the widget bridge never sees the guard's event")
    }

    @Test
    fun `widget events reach the widget bridge unchanged`() {
        var reports = 0
        val bridge = ClientSdkLoadGuardBridge(delegate) { reports++ }
        val events = listOf(
            """{"event":"chargeAuthSuccess","data":{"status":"authenticated"}}""",
            """{"event":"clientSdkLoaded"}""",
            """{"data":{"event":"clientSdkLoadFailed"}}""",
            """{"event":{"name":"clientSdkLoadFailed"}}""",
            "not json",
            "",
        )

        events.forEach(bridge::postMessage)

        assertEquals(events, delegated)
        assertEquals(0, reports)
    }

    @Test
    fun `only the guard's event is recognised`() {
        assertTrue(ClientSdkLoadGuardBridge.isClientSdkLoadFailed(LOAD_FAILED))
        assertTrue(ClientSdkLoadGuardBridge.isClientSdkLoadFailed("""{ "event" : "clientSdkLoadFailed" }"""))
        assertFalse(ClientSdkLoadGuardBridge.isClientSdkLoadFailed("""["clientSdkLoadFailed"]"""))
        assertFalse(ClientSdkLoadGuardBridge.isClientSdkLoadFailed("""{"event":null}"""))
        assertFalse(ClientSdkLoadGuardBridge.isClientSdkLoadFailed("""{"event":"clientSdkLoadFailed""""))
    }

    private companion object {
        const val SCRIPT_URL = "https://widget.paydock.com/sdk/v1.150.stable/widget.umd.min.js"
        const val LOAD_FAILED = """{"event":"clientSdkLoadFailed"}"""
    }
}

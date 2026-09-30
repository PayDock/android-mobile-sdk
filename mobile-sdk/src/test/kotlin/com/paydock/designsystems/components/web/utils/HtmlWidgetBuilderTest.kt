package com.paydock.designsystems.components.web.utils

import com.paydock.designsystems.components.web.config.WidgetConfig
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class HtmlWidgetBuilderTest {

    @Test
    fun `standalone 3DS html subscribes to challenge loaded and completed events`() {
        val html = HtmlWidgetBuilder.createHtml(standaloneConfig())

        listOf(
            "chargeAuthSuccess",
            "chargeAuthReject",
            "chargeAuthChallenge",
            "chargeAuthChallengeLoaded",
            "chargeAuthChallengeCompleted",
            "chargeAuthDecoupled",
            "chargeAuthInfo",
            "error"
        ).forEach { event ->
            assertTrue(html.contains("watchEvent(\"$event\");"), "Missing subscription for $event")
        }
    }

    @Test
    fun `standalone 3DS html forwards a sanitised payload guarded against failures`() {
        val html = HtmlWidgetBuilder.createHtml(standaloneConfig())

        assertTrue(html.contains("const mapEventData = function (event, data)"))
        assertTrue(html.contains("data: mapEventData(event, data)"))
        assertTrue(html.contains("try {"))
        assertTrue(html.contains("charge_3ds_id: str(d.charge_3ds_id)"))
        assertTrue(html.contains("reason: str(d.reason)"))
        assertTrue(html.contains("source: str(d.source)"))
    }

    @Test
    fun `other web widgets keep forwarding the raw event data`() {
        val html = HtmlWidgetBuilder.createHtml(
            WidgetConfig.ThreeDSConfigBase.MPGS3dsConfig(
                jsLibraryUrl = JS_LIBRARY_URL,
                environment = ENVIRONMENT,
                token = TOKEN
            )
        )

        assertTrue(html.contains("data: data"))
        assertFalse(html.contains("mapEventData"))
        assertFalse(html.contains("chargeAuthChallengeLoaded"))
    }

    private fun standaloneConfig() = WidgetConfig.ThreeDSConfigBase.Standalone3DSConfig(
        jsLibraryUrl = JS_LIBRARY_URL,
        environment = ENVIRONMENT,
        token = TOKEN
    )

    private companion object {
        const val JS_LIBRARY_URL = "https://widget.example.com/sdk/widget.umd.min.js"
        const val ENVIRONMENT = "sandbox"
        const val TOKEN = "token"
    }
}

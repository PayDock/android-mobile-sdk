package com.paydock.designsystems.components.web.utils

import com.paydock.designsystems.components.web.config.WidgetConfig
import org.junit.Test
import kotlin.test.assertEquals
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

    @Test
    fun `every widget page guards the client sdk script in order`() {
        allConfigs().forEach { config ->
            val html = HtmlWidgetBuilder.createHtml(config)

            val guard = html.indexOf("function onClientSdkLoadFailed()")
            val scriptTag = html.indexOf("<script src=\"$JS_LIBRARY_URL\" onerror=\"onClientSdkLoadFailed()\"></script>")
            val globalCheck = html.indexOf("if (typeof window.paydock === \"undefined\") {")

            assertTrue(guard >= 0, "${config.title}: guard function missing")
            assertTrue(scriptTag >= 0, "${config.title}: script tag with onerror missing")
            assertTrue(globalCheck >= 0, "${config.title}: global check missing")
            assertTrue(guard < scriptTag && scriptTag < globalCheck, "${config.title}: guard, script tag, then check")
        }
    }

    @Test
    fun `the guard posts clientSdkLoadFailed over the bridge once and never throws`() {
        val html = HtmlWidgetBuilder.createHtml(standaloneConfig())
        val guard = html.substring(
            html.indexOf("function onClientSdkLoadFailed()"),
            html.indexOf("<script src=")
        )

        assertTrue(guard.contains("if (clientSdkLoadFailedReported) {"))
        assertTrue(guard.contains("clientSdkLoadFailedReported = true;"))
        assertTrue(guard.contains("typeof PayDockMobileSDK !== \"undefined\""))
        assertTrue(guard.contains("PayDockMobileSDK.postMessage(JSON.stringify({ event: \"clientSdkLoadFailed\" }));"))
        assertTrue(guard.contains("try {") && guard.contains("} catch (e) {"))
    }

    @Test
    fun `the widget's inline script runs only inside the global check`() {
        allConfigs().forEach { config ->
            val html = HtmlWidgetBuilder.createHtml(config)
            val globalCheck = html.indexOf("if (typeof window.paydock === \"undefined\") {")
            val elseBranch = html.indexOf("} else {", globalCheck)
            val widgetScript = html.indexOf("var widget = ")
            val load = html.indexOf("widget.load();")
            val afterLoad = html.substring(load + "widget.load();".length)

            assertTrue(html.indexOf("onClientSdkLoadFailed();", globalCheck) in globalCheck..elseBranch)
            assertTrue(elseBranch in globalCheck..widgetScript, "${config.title}: widget script before the else branch")
            assertTrue(widgetScript < load, "${config.title}: widget script order")
            assertTrue(
                Regex("""^\s*}\s*</script>""").containsMatchIn(afterLoad),
                "${config.title}: the else branch must close right after the widget script"
            )
            assertEquals(1, Regex("var widget = ").findAll(html).count(), "${config.title}: one widget script")
        }
    }

    private fun allConfigs(): List<WidgetConfig> = listOf(
        standaloneConfig(),
        WidgetConfig.ThreeDSConfigBase.MPGS3dsConfig(jsLibraryUrl = JS_LIBRARY_URL, environment = ENVIRONMENT, token = TOKEN),
        WidgetConfig.ClickToPayConfig(
            jsLibraryUrl = JS_LIBRARY_URL,
            environment = ENVIRONMENT,
            accessToken = TOKEN,
            serviceId = "service",
            meta = null
        ),
    )

    private fun standaloneConfig() = WidgetConfig.ThreeDSConfigBase.Standalone3DSConfig(
        jsLibraryUrl = JS_LIBRARY_URL,
        environment = ENVIRONMENT,
        token = TOKEN
    )

    private companion object {
        const val JS_LIBRARY_URL = "https://widget.paydock.com/sdk/v1.150.stable/widget.umd.min.js"
        const val ENVIRONMENT = "sandbox"
        const val TOKEN = "token"
    }
}

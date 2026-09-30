package com.paydock.designsystems.components.web.utils

import com.paydock.core.ClientSDKConstants
import com.paydock.core.MobileSDKConstants
import com.paydock.designsystems.components.web.config.WidgetConfig
import kotlinx.html.HEAD
import kotlinx.html.SCRIPT
import kotlinx.html.STYLE
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.lang
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.stream.appendHTML
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.unsafe

/**
 * Helper object for generating HTML content for embedding widgets in a WebView.
 */
internal object HtmlWidgetBuilder {

    /**
     * Creates HTML content for embedding a widget in a WebView.
     *
     * @param config The configuration object for the widget.
     * @return The generated HTML content as a string.
     */
    fun createHtml(config: WidgetConfig): String = buildString {
        appendHTML().html {
            lang = "en"
            head { includeInlineHead(config.title) }
            body {
                div { id = ClientSDKConstants.WIDGET_CONTAINER_ID }
                script(src = config.jsLibraryUrl) {}
                script { includeInlineScript(config) }
            }
        }
    }

    /**
     * Includes meta tags, links, title, and style block in the HEAD section of the HTML document.
     *
     * @param title Title of the HTML document.
     */
    private fun HEAD.includeInlineHead(title: String) {
        meta(charset = "UTF-8")
        meta(
            name = "viewport",
            content = "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"
        )
        link(rel = "apple-touch-icon", href = "/apple-touch-icon.png")
        link(rel = "icon", type = "image/png", href = "/favicon-32x32.png")
        link(rel = "icon", type = "image/png", href = "/favicon-16x16.png")
        link(rel = "manifest", href = "/site.webmanifest")
        title(title)
        style { includeInlineStyles() }
    }

    /**
     * Includes inline styles in the STYLE block of the HTML document.
     */
    private fun STYLE.includeInlineStyles() {
        unsafe {
            raw(
                """
                body {
                    margin: 0;
                }
                
                iframe {
                    border: 0;
                    width: 100%;
                    height: 100vh;
                }
                """.trimIndent()
            )
        }
    }

    /**
     * Includes inline JavaScript code in the SCRIPT block for initializing the widget.
     *
     * @param config The configuration object for the widget.
     */
    private fun SCRIPT.includeInlineScript(config: WidgetConfig) {
        unsafe {
            raw(
                """ 
                var widget = ${config.createWidget()}
                widget.setEnv("${config.environment}");

                ${createWatchEventScript(config)}
                ${config.events.joinToString("\n") { "watchEvent(\"$it\");" }}

                widget.load();
                """.trimIndent()
            )
        }
    }

    /**
     * Creates the `watchEvent` JavaScript function that forwards widget events to the native bridge.
     *
     * If the config provides a [WidgetConfig.eventDataTransform], the event payload is built with it and
     * the forwarding is guarded so it never throws inside the page.
     *
     * @param config The configuration object for the widget.
     * @return The `watchEvent` function declaration.
     */
    private fun createWatchEventScript(config: WidgetConfig): String {
        val bridge = MobileSDKConstants.JS_BRIDGE_NAME
        val transform = config.eventDataTransform
            ?: return """
                const watchEvent = (event) => {
                    widget.on(event, function (data) {
                        if (typeof $bridge !== "undefined") {
                            $bridge.postMessage(JSON.stringify({
                                event,
                                data: data
                            }));
                        }
                    });
                };
            """.trimIndent()
        return """
            const mapEventData = $transform;
            const watchEvent = (event) => {
                widget.on(event, function (data) {
                    try {
                        if (typeof $bridge !== "undefined") {
                            $bridge.postMessage(JSON.stringify({
                                event,
                                data: mapEventData(event, data)
                            }));
                        }
                    } catch (e) {
                        console.error("Failed to forward " + event + " event", e);
                    }
                });
            };
        """.trimIndent()
    }
}
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
                #loader {
                    display: flex;
                    position: fixed;
                    width: 100%;
                    height: 100vh;
                    top: 0;
                    left: 0;
                    background: white;
                    align-items: center;
                    justify-content: center;
                    color: black;
                    font-size: 40px;
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

                const watchEvent = (event) => {
                    widget.on(event, function (data) {
                        if (typeof ${MobileSDKConstants.JS_BRIDGE_NAME} !== "undefined") {
                            ${MobileSDKConstants.JS_BRIDGE_NAME}.postMessage(JSON.stringify({
                                event,
                                data: data
                            }));
                        }
                    });
                };
                ${config.events.joinToString("\n") { "watchEvent(\"$it\");" }}

                widget.load();
                """.trimIndent()
            )
        }
    }
}
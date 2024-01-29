/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paydock.feature.threeDS.presentation.utils

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
 * Helper object for creating HTML content related to 3D Secure (3DS) processing.
 *
 * This object provides a method to generate HTML content for embedding a 3DS widget in a WebView.
 * The HTML includes necessary meta tags, styles, scripts, and event handling logic.
 */
internal object ThreeDSHTMLHelper {

    /**
     * Creates HTML content for embedding a 3DS widget in a WebView.
     *
     * @param token The token used for 3DS widget initialization.
     * @return The generated HTML content as a string.
     */
    @Suppress("LongMethod")
    fun create3DSHtml(token: String): String = buildString {
        appendHTML().html {
            lang = "en"
            head { includeInlineHead() }
            // Body of the HTML document
            body {
                // Div container for the 3DS widget
                div { id = "widget" }
                // Script tag for including the 3DS widget library
                script(src = "https://widget.paydock.com/sdk/latest/widget.umd.min.js") {}
                // Script block with JavaScript code for initializing the 3DS widget
                script { includeInlineScript(token) }
            }
        }
    }
}

/**
 * Includes meta tags, links, title, and style block in the HEAD section of the HTML document.
 */
private fun HEAD.includeInlineHead() {
    // Meta tags for character set and viewport settings
    meta(charset = "UTF-8")
    meta(
        name = "viewport",
        content = "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"
    )

    // Links to favicon and site manifest
    link(rel = "apple-touch-icon", href = "/apple-touch-icon.png")
    link(rel = "icon", type = "image/png", href = "/favicon-32x32.png")
    link(rel = "icon", type = "image/png", href = "/favicon-16x16.png")
    link(rel = "manifest", href = "/site.webmanifest")

    // Title for the HTML document
    title("3DS")

    // Style block for custom styles
    style { includeInlineStyles() }
}

/**
 * Includes inline styles in the STYLE block of the HTML document.
 */
private fun STYLE.includeInlineStyles() {
    unsafe {
        raw(
            """
            iframe {
                border: 0;
                width: 100%;
                height: 80vh;
            }
            #loader {
                display: flex;
                position: fixed;
                width: 100%;
                height: 80vh;
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
 * Includes inline JavaScript code in the SCRIPT block for initializing the 3DS widget.
 *
 * @param token The token used for 3DS widget initialization.
 */
private fun SCRIPT.includeInlineScript(token: String) {
    unsafe {
        raw(
            """
            var meta = document.createElement("meta");
            meta.name = "viewport";
            meta.content = "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no";
            var head = document.getElementsByTagName("head")[0];
            head.appendChild(meta);
            const token = "$token";
            var canvas3ds = new paydock.Canvas3ds("#widget", token);
            canvas3ds.setEnv("sandbox");
            const watchEvent = (event) => {
                canvas3ds.on(event, function (data) {
                    if (typeof PayDockMobileSDK !== "undefined") {
                        PayDockMobileSDK.postMessage(JSON.stringify({
                            event,
                            charge3dsId: data.charge_3ds_id,
                        }));
                    }
                });
            };
            watchEvent("chargeAuthSuccess");
            watchEvent("chargeAuthReject");
            watchEvent("chargeAuthChallenge");
            watchEvent("chargeAuthDecoupled");
            watchEvent("chargeAuthInfo");
            watchEvent("error");
            canvas3ds.load();
            """.trimIndent()
        )
    }
}

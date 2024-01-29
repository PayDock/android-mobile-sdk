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

package com.paydock.designsystems.components.web

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.paydock.designsystems.components.web.utils.SdkJSBridge

/**
 * Composable function to display a WebView with customizable settings and event handling.
 *
 * @param webUrl The URL to load in the WebView.
 * @param data Optional HTML data to load in the WebView instead of a URL.
 * @param showLoader Flag to determine whether to display a loading indicator.
 * @param domStorageEnabled Sets whether the DOM storage API is enabled. The default value is false. [WebSettings]
 * @param jsBridge The generic JavaScript interface for communication between WebView and Android code.
 * @param onShouldOverrideUrlLoading Custom logic for handling URL loading events.
 * @param onWebViewError Callback to handle WebView errors.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun <T : Any?> SdkWebView(
    webUrl: String,
    data: String? = null,
    showLoader: Boolean = true,
    allowScrolling: Boolean = true,
    domStorageEnabled: Boolean = false,
    jsBridge: SdkJSBridge<T>? = null,
    onShouldOverrideUrlLoading: ((request: WebResourceRequest?) -> Boolean?)? = null,
    onWebViewError: (Int, String) -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }
    var webView: WebView? by remember { mutableStateOf(null) }
    val scrollState = rememberScrollState()
    // Display a WebView in a Box composable
    val modifier = if (allowScrolling) {
        Modifier.fillMaxSize().verticalScroll(scrollState)
    } else {
        Modifier.fillMaxSize()
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // AndroidView to display the WebView
        AndroidView(
            factory = {
                WebView(it).apply {
                    // Configure WebView
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    // Configure WebViewClient to handle various events
                    webViewClient = object : WebViewClient() {

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            return onShouldOverrideUrlLoading?.let {
                                it(request)
                            } ?: super.shouldOverrideUrlLoading(view, request)
                        }

                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onRenderProcessGone(
                            view: WebView?,
                            detail: RenderProcessGoneDetail?
                        ): Boolean {
                            // Handle when the render process has gone
                            if (view == webView && detail?.didCrash() == true) {
                                return true
                            }
                            return super.onRenderProcessGone(view, detail)
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            // Handle error here
                            if (error != null) {
                                onWebViewError(error.errorCode, error.description.toString())
                            }
                        }
                    }

                    // Enable JavaScript and set cache mode
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = domStorageEnabled
                    settings.cacheMode = WebSettings.LOAD_NO_CACHE
                    // Add JavaScript interface for communication between WebView and Android code
                    jsBridge?.let { jsInterface ->
                        addJavascriptInterface(
                            jsInterface,
                            "PayDockMobileSDK"
                        )
                    }

                    webView = this
                }
            },
            // Update the WebView with HTML content when recomposed
            update = {
                if (data != null) {
                    webView?.loadDataWithBaseURL(
                        webUrl,
                        data,
                        "text/html",
                        "utf-8",
                        null
                    )
                } else {
                    webView?.loadUrl(webUrl)
                }
                webView = it
            }
        )

        // Display a loading indicator if the WebView is still loading content
        if (showLoader && isLoading) {
            CircularProgressIndicator()
        }
    }
}

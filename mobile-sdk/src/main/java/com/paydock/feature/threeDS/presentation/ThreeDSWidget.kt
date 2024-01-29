/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 4:15 PM
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

package com.paydock.feature.threeDS.presentation

import androidx.compose.runtime.Composable
import com.paydock.core.data.network.error.exceptions.WebViewException
import com.paydock.designsystems.components.web.SdkWebView
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.feature.threeDS.domain.model.ThreeDSResult
import com.paydock.feature.threeDS.presentation.utils.ThreeDSHTMLHelper
import com.paydock.feature.threeDS.presentation.utils.ThreeDSJSBridge

/**
 * Composable function to initiate 3D Secure (3DS) processing.
 *
 * @param token The 3DS token used for 3DS widget initialization.
 * @param completion Callback function to handle the result of 3DS processing.
 */
@Composable
fun ThreeDSWidget(
    token: String,
    completion: (Result<ThreeDSResult>) -> Unit
) {
    // Apply the SdkTheme for consistent styling
    SdkTheme {
        // Display the 3DS WebView within the bottom sheet
        val htmlString = ThreeDSHTMLHelper.create3DSHtml(token)
        SdkWebView(
            allowScrolling = false,
            webUrl = "https://paydock.com",
            data = htmlString,
            jsBridge = ThreeDSJSBridge {
                // Invoke the callback with the 3DS result
                completion(Result.success(it))
            }
        ) { status, message ->
            // Invoke the onWebViewError callback with the WebView ThreeDSException exception
            completion(Result.failure(WebViewException.ThreeDSException(status, message)))
        }
    }
}

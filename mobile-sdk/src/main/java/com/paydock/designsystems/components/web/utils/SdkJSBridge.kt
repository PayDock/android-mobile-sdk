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

package com.paydock.designsystems.components.web.utils

import android.webkit.JavascriptInterface

/**
 * Abstract class representing a JavaScript bridge for communication between WebView and Android code.
 * Child classes must implement the [postMessage] method to handle JavaScript messages.
 *
 * @param T The type of the result expected from JavaScript messages.
 * @property onResultCallback Callback function to be invoked with the result from JavaScript.
 */
internal abstract class SdkJSBridge<T>(protected val onResultCallback: (T) -> Unit) {

    /**
     * Abstract method to be implemented by child classes for handling JavaScript messages.
     *
     * @param eventJson The JSON string representing an event.
     */
    @JavascriptInterface
    abstract fun postMessage(eventJson: String)
}

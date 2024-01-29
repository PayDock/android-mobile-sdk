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

package com.paydock.core.data.network.error.exceptions

import java.io.IOException

/**
 * Represents the error that happened while communicating with WebView
 *
 * @param code - HTTP code of the response
 * @param displayableMessage - message that can displayed to the user
 */
sealed class WebViewException(
    val code: Int? = null,
    val displayableMessage: String,
) : IOException(displayableMessage) {

    /**
     * Represents the error that happened while communicating with WebView via 3DS Integration
     *
     * @param code - HTTP code of the response
     * @param displayableMessage - message that can displayed to the user
     */
    class ThreeDSException(code: Int? = null, displayableMessage: String) : WebViewException(code, displayableMessage)

    /**
     * Represents the error that happened while communicating with WebView via PayPal integration
     *
     * @param code - HTTP code of the response
     * @param displayableMessage - message that can displayed to the user
     */
    class PayPalException(code: Int? = null, displayableMessage: String) : WebViewException(code, displayableMessage)

    /**
     * Represents the error that happened while communicating with WebView via FlyPay integration
     *
     * @param code - HTTP code of the response
     * @param displayableMessage - message that can displayed to the user
     */
    class FlyPayException(code: Int? = null, displayableMessage: String) : WebViewException(code, displayableMessage)
    class UnknownException(displayableMessage: String) : WebViewException(displayableMessage = displayableMessage)
}
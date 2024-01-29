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

package com.paydock.core.utils.decoder

import android.net.Uri

/**
 * Implementation of the StringDecoder interface that uses Android's Uri class to decode strings.
 */
class UriDecoder : StringDecoder {

    /**
     * Decodes an encoded string using Android's Uri class.
     *
     * @param encodedString The string to be decoded.
     * @return The decoded string.
     */
    override fun decodeString(encodedString: String): String {
        // Using Android's Uri class to decode the string
        return Uri.decode(encodedString)
    }
}
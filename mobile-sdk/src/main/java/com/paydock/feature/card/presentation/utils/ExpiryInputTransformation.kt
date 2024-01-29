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

package com.paydock.feature.card.presentation.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.paydock.core.EXPIRY_CHUNK_SIZE
import com.paydock.core.MAX_EXPIRY_LENGTH

/**
 * A [VisualTransformation] that formats the input of an expiration field to "mm/yy" format.
 * The transformation is only applied visually; the actual value remains unchanged.
 */
class ExpiryInputTransformation : VisualTransformation {

    /**
     * Applies the expiration date formatting to the input [AnnotatedString].
     *
     * @param text The input [AnnotatedString] to be transformed.
     * @return A [TransformedText] with the expiration date formatting applied.
     */
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(MAX_EXPIRY_LENGTH)
        val transformedText = buildString {
            for (i in trimmed.indices) {
                append(trimmed[i])
                if (i == 1) append("/") // Adding slash after the second character
            }
        }

        val offsetTranslator = object : OffsetMapping {
            /**
             * Maps the original text offset to the transformed text offset.
             *
             * @param offset The original text offset.
             * @return The corresponding transformed text offset.
             */
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 1 -> offset // From 0 to 1, offset doesn't change
                    offset <= MAX_EXPIRY_LENGTH -> offset + 1 // From 2 to 4, offset takes into account the added slash
                    else -> MAX_EXPIRY_LENGTH + 1
                }
            }

            /**
             * Maps the transformed text offset back to the original text offset.
             *
             * @param offset The transformed text offset.
             * @return The corresponding original text offset.
             */
            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= EXPIRY_CHUNK_SIZE -> offset
                    offset <= MAX_EXPIRY_LENGTH + 1 -> offset - 1
                    else -> MAX_EXPIRY_LENGTH
                }
            }
        }

        return TransformedText(AnnotatedString(transformedText), offsetTranslator)
    }
}
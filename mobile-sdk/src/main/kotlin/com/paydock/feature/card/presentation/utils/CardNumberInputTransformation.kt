package com.paydock.feature.card.presentation.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.paydock.core.MobileSDKConstants

/**
 * A custom visual transformation for card numbers that formats the input into "xxxx xxxx xxxx xxxx" format.
 */
class CardNumberInputTransformation(
    private val maxNumber: Int,
    private val subSectionSize: Int = MobileSDKConstants.CardDetailsConfig.CARD_NUMBER_SECTION_SIZE
) : VisualTransformation {

    /**
     * Filters and formats the input text for card numbers.
     *
     * @param text The input text to be transformed.
     * @return The transformed text with the card number formatted as "xxxx xxxx xxxx xxxx".
     */
    override fun filter(text: AnnotatedString): TransformedText {
        // Remove all non-digit characters from the input text
        val trimmedText = text.text.replace(Regex("\\D"), "")
        val out = buildString {
            // Separate the input into 4-digit segments
            trimmedText.chunked(MobileSDKConstants.CardDetailsConfig.CARD_NUMBER_SECTION_SIZE) { chunk ->
                append(chunk)
                append(" ") // Add a space after each 4-digit segment
            }
        }

        /**
         * An offset mapping implementation to handle the transformed text.
         * It adjusts the offset based on the spaces added during formatting.
         */
        val offsetTranslator = object : OffsetMapping {
            /**
             * Translates the original offset to the transformed offset.
             *
             * @param offset The original offset.
             * @return The transformed offset with spaces added.
             */
            override fun originalToTransformed(offset: Int): Int {
                val spacesAdded =
                    (offset / subSectionSize).coerceAtMost((maxNumber - 1) / subSectionSize)
                return offset + spacesAdded
            }

            /**
             * Translates the transformed offset to the original offset.
             *
             * @param offset The transformed offset.
             * @return The original offset catering for spaces subtracted.
             */
            override fun transformedToOriginal(offset: Int): Int {
                // Here we just return the length to prevent user from changing random location text (causes crash)
                return text.length
            }
        }
        return TransformedText(AnnotatedString(out), offsetTranslator)
    }
}
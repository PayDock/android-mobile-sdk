package com.paydock.feature.card.presentation.utils.transformations

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * A custom visual transformation for card numbers that formats the input based on provided subsection sizes.
 *
 * @param subSectionSizes A list of integers representing the cumulative positions where spaces should be inserted.
 *                        For example, `listOf(4, 8, 12)` would format the number as "xxxx xxxx xxxx xxxx".
 *                        `listOf(4, 10)` would format it as "xxxx xxxxxx xxxx".
 *                        An empty list will result in no spaces being added.
 */
internal class CardNumberInputTransformation(
    subSectionSizes: List<Int> = emptyList() // Default to emptyList
) : VisualTransformation {

    private val subSectionSizes: List<Int> = subSectionSizes.ifEmpty { listOf(4, 8, 12, 16) }

    /**
     * Filters and formats the input text for card numbers based on the provided subsection sizes.
     *
     * @param text The input text to be transformed.
     * @return The transformed text with the card number formatted according to the subsection sizes.
     */
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmedText = text.text.replace(Regex("\\D"), "")
        val formattedNumber = formatCardNumber(trimmedText, subSectionSizes)

        val offsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return originalToTransformedOffset(offset, subSectionSizes, trimmedText.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                return transformedToOriginalOffset(offset, subSectionSizes, trimmedText.length)
            }
        }

        return TransformedText(AnnotatedString(formattedNumber), offsetTranslator)
    }

    /**
     * Formats a credit card number by inserting spaces at specified intervals.
     *
     * @param number The credit card number to format.
     * @param subSectionSizes A list of integers representing the cumulative positions where spaces should be inserted.
     *                        For example, `listOf(4, 8, 12)` would format the number as "xxxx xxxx xxxx".
     *                        `listOf(4, 10)` would format it as "xxxx xxxxxxxxxx".
     *                        An empty list will result in no spaces being added.
     * @return The formatted credit card number with spaces inserted at the specified intervals.
     */
    private fun formatCardNumber(number: String, subSectionSizes: List<Int>): String {
        if (subSectionSizes.isEmpty()) {
            return number
        }

        val formattedNumber = StringBuilder()
        var currentPosition = 0
        var currentSubSectionIndex = 0
        var nextSpacePosition = subSectionSizes.getOrElse(currentSubSectionIndex) {
            Int.MAX_VALUE
        } // Initialize with the first gap position or a large value

        while (currentPosition < number.length) {
            formattedNumber.append(number[currentPosition])
            currentPosition++

            if (currentPosition == nextSpacePosition) {
                formattedNumber.append(" ")
                currentSubSectionIndex++
                nextSpacePosition = subSectionSizes.getOrElse(currentSubSectionIndex) { Int.MAX_VALUE }
            }
        }

        return formattedNumber.toString()
    }

    /**
     * Translates the original offset to the transformed offset based on the subsection sizes.
     *
     * @param offset The original offset.
     * @param subSectionSizes The list of subsection sizes.
     * @param originalLength The length of the original (unformatted) text.
     * @return The transformed offset.
     */
    private fun originalToTransformedOffset(offset: Int, subSectionSizes: List<Int>, originalLength: Int): Int {
        if (subSectionSizes.isEmpty()) return offset
        if (offset > originalLength) {
            return formatCardNumber(
                buildString {
                    repeat(originalLength) { append("x") }
                },
                subSectionSizes
            ).length
        }

        var spacesAdded = 0
        for (subSectionSize in subSectionSizes) {
            if (offset > subSectionSize) {
                spacesAdded++
            }
        }
        return offset + spacesAdded
    }

    /**
     * Translates the transformed offset to the original offset based on the subsection sizes.
     *
     * @param offset The transformed offset.
     * @param subSectionSizes The list of subsection sizes.
     * @param originalLength The length of the original (unformatted) text.
     * @return The original offset.
     */
    private fun transformedToOriginalOffset(offset: Int, subSectionSizes: List<Int>, originalLength: Int): Int {
        if (subSectionSizes.isEmpty()) return offset

        var spaces = 0
        var originalOffset = offset
        for (subSectionSize in subSectionSizes) {
            if (originalOffset > subSectionSize + spaces) {
                spaces++
            }
        }
        originalOffset -= spaces
        return originalOffset.coerceAtLeast(0).coerceAtMost(originalLength)
    }
}
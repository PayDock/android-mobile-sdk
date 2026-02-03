package com.paydock.feature.card.data.detector

import com.paydock.feature.card.data.dto.BinDataResponse

/**
 * Result of card scheme detection.
 *
 * @property scheme The detected scheme name (e.g., "visa", "mastercard")
 * @property detectedAt The number of digits at which the scheme was detected (1, 2, 4, or 6)
 * @property length The length of the PAN that was checked
 */
internal data class DetectionResult(
    val scheme: String,
    val detectedAt: Int,
    val length: Int
)

/**
 * Detector for card schemes using BIN data with hierarchical prefix matching.
 *
 * Implements the detection algorithm:
 * 1. Check 2-digit prefixes (minimum length)
 * 2. Check 4-digit prefixes (override if more specific)
 * 3. Check 6-digit BINs (FINAL - 100% accuracy, override if more specific)
 */
internal class BinDataDetector(private val data: BinDataResponse) {

    /**
     * Detects the card scheme for a given PAN (Primary Account Number).
     *
     * @param pan The card number (PAN) to detect the scheme for
     * @return DetectionResult with the detected scheme, or null if no match found
     */
    fun detectScheme(pan: String): DetectionResult? {
        if (pan.isEmpty()) {
            return null
        }

        // Remove any non-digit characters
        val cleanedPan = pan.replace(Regex("\\D"), "")
        if (cleanedPan.length < 2) {
            return null
        }

        val length = cleanedPan.length
        var detectedScheme: String? = null
        var detectedAt: Int? = null

        // Step 1: Check 2-digit prefixes (minimum length)
        if (length >= 2) {
            val prefix2 = cleanedPan.substring(0, 2)
            val prefix1 = cleanedPan.substring(0, 1)

            // Check exact 2-digit match
            if (data.prefix2.containsKey(prefix2)) {
                val schemeCode = data.prefix2[prefix2]
                if (schemeCode != null) {
                    detectedScheme = data.schemes[schemeCode]
                    detectedAt = 2
                }
            }
            // Check 1-digit match
            else if (data.prefix2.containsKey(prefix1)) {
                val schemeCode = data.prefix2[prefix1]
                if (schemeCode != null) {
                    detectedScheme = data.schemes[schemeCode]
                    detectedAt = 1
                }
            }
            // Check 2-digit ranges
            else {
                val schemeCode = checkRanges(prefix2, data.ranges.range2)
                if (schemeCode != null) {
                    detectedScheme = data.schemes[schemeCode]
                    detectedAt = 2
                }
            }
        }

        // Step 2: Check 4-digit prefixes (override if more specific)
        if (length >= 4) {
            val prefix4 = cleanedPan.substring(0, 4)

            // Check exact 4-digit match
            if (data.prefix4.containsKey(prefix4)) {
                val schemeCode = data.prefix4[prefix4]
                if (schemeCode != null) {
                    detectedScheme = data.schemes[schemeCode]
                    detectedAt = 4
                }
            }
            // Check 4-digit ranges
            else {
                val schemeCode = checkRanges(prefix4, data.ranges.range4)
                if (schemeCode != null) {
                    detectedScheme = data.schemes[schemeCode]
                    detectedAt = 4
                }
            }
        }

        // Step 3: Check 6-digit BINs (FINAL - 100% accuracy, override if more specific)
        if (length >= 6) {
            val prefix6 = cleanedPan.substring(0, 6)

            // Check exact 6-digit match
            if (data.prefix6.containsKey(prefix6)) {
                val schemeCode = data.prefix6[prefix6]
                if (schemeCode != null) {
                    detectedScheme = data.schemes[schemeCode]
                    detectedAt = 6
                }
            }
            // Check 6-digit ranges (FINAL check)
            else {
                val schemeCode = checkRanges(prefix6, data.ranges.range6)
                if (schemeCode != null) {
                    detectedScheme = data.schemes[schemeCode]
                    detectedAt = 6
                }
            }
        }

        return if (detectedScheme != null && detectedAt != null) {
            DetectionResult(
                scheme = detectedScheme,
                detectedAt = detectedAt,
                length = length
            )
        } else {
            null
        }
    }

    /**
     * Checks if a prefix falls within any of the given ranges.
     *
     * @param prefix The prefix to check (as string)
     * @param ranges List of ranges, where each range is [start, end, schemeCode]
     * @return The scheme code if found in ranges, or null
     */
    private fun checkRanges(prefix: String, ranges: List<List<String>>): String? {
        if (ranges.isEmpty()) {
            return null
        }

        val prefixNum = prefix.toIntOrNull() ?: return null

        for (range in ranges) {
            if (range.size < 3) continue

            val startStr = range[0]
            val endStr = range[1]
            val schemeCode = range[2]

            val startNum = startStr.toIntOrNull() ?: continue
            val endNum = endStr.toIntOrNull() ?: continue

            if (prefixNum >= startNum && prefixNum <= endNum) {
                return schemeCode
            }
        }

        return null
    }
}

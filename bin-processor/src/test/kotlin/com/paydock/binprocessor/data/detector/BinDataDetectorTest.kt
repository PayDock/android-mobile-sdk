package com.paydock.binprocessor.data.detector

import com.paydock.binprocessor.data.dto.BinDataResponse
import com.paydock.binprocessor.data.dto.BinRanges
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [BinDataDetector].
 *
 * Tests verify the hierarchical card scheme detection algorithm:
 * - 2-digit prefix detection (minimum length)
 * - 4-digit prefix detection (override if more specific)
 * - 6-digit BIN detection (final/100% accuracy)
 * - Range-based detection for each prefix length
 */
internal class BinDataDetectorTest {

    private lateinit var binData: BinDataResponse
    private lateinit var detector: BinDataDetector

    @Before
    fun setUp() {
        binData = BinDataResponse(
            prefix2 = mapOf(
                "4" to "v", // Visa with single digit prefix
                "51" to "m", // Mastercard 51
                "52" to "m", // Mastercard 52
                "34" to "a", // Amex 34
                "37" to "a" // Amex 37
            ),
            prefix4 = mapOf(
                "6011" to "d", // Discover
                "6221" to "u" // UnionPay
            ),
            prefix6 = mapOf(
                "411111" to "v", // Visa specific BIN
                "222100" to "m", // Mastercard specific BIN
                "601100" to "d" // Discover specific BIN
            ),
            ranges = BinRanges(
                range2 = listOf(
                    listOf("53", "55", "m") // Mastercard 53-55 range
                ),
                range4 = listOf(
                    listOf("3528", "3589", "j"), // JCB range
                    listOf("2221", "2720", "m") // Mastercard 2 range
                ),
                range6 = listOf(
                    listOf("622126", "622925", "u") // UnionPay 6-digit range
                )
            ),
            schemes = mapOf(
                "v" to "visa",
                "m" to "mastercard",
                "a" to "amex",
                "d" to "discover",
                "j" to "japcb",
                "u" to "unionpay"
            ),
            version = 1
        )
        detector = BinDataDetector(binData)
    }

    // ===========================================
    // Empty and Invalid Input Tests
    // ===========================================

    @Test
    fun `detectScheme returns null for empty PAN`() {
        val result = detector.detectScheme("")
        assertNull(result)
    }

    @Test
    fun `detectScheme returns null for single digit PAN`() {
        val result = detector.detectScheme("4")
        assertNull(result)
    }

    @Test
    fun `detectScheme returns null for PAN with only non-digits`() {
        val result = detector.detectScheme("abc")
        assertNull(result)
    }

    @Test
    fun `detectScheme handles PAN with spaces and removes them`() {
        val result = detector.detectScheme("41 11 11")
        assertNotNull(result)
        assertEquals("visa", result?.scheme)
    }

    @Test
    fun `detectScheme handles PAN with dashes and removes them`() {
        val result = detector.detectScheme("4111-1111")
        assertNotNull(result)
        assertEquals("visa", result?.scheme)
    }

    // ===========================================
    // 2-Digit Prefix Detection Tests (Single digit in prefix2)
    // ===========================================

    @Test
    fun `detectScheme detects Visa with single digit 4 prefix in prefix2 map`() {
        val result = detector.detectScheme("40")
        assertNotNull(result)
        assertEquals("visa", result?.scheme)
        assertEquals(1, result?.detectedAt)
        assertEquals(2, result?.length)
    }

    @Test
    fun `detectScheme detects Visa with 4 prefix and longer PAN`() {
        val result = detector.detectScheme("4111111111111111")
        assertNotNull(result)
        assertEquals("visa", result?.scheme)
    }

    // ===========================================
    // 2-Digit Prefix Detection Tests (Exact 2-digit match)
    // ===========================================

    @Test
    fun `detectScheme detects Mastercard with 51 prefix`() {
        val result = detector.detectScheme("5100000000000000")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
        assertEquals(2, result?.detectedAt)
    }

    @Test
    fun `detectScheme detects Mastercard with 52 prefix`() {
        val result = detector.detectScheme("5200000000000000")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
        assertEquals(2, result?.detectedAt)
    }

    @Test
    fun `detectScheme detects Amex with 34 prefix`() {
        val result = detector.detectScheme("340000000000000")
        assertNotNull(result)
        assertEquals("amex", result?.scheme)
        assertEquals(2, result?.detectedAt)
    }

    @Test
    fun `detectScheme detects Amex with 37 prefix`() {
        val result = detector.detectScheme("370000000000000")
        assertNotNull(result)
        assertEquals("amex", result?.scheme)
        assertEquals(2, result?.detectedAt)
    }

    // ===========================================
    // 2-Digit Range Detection Tests
    // ===========================================

    @Test
    fun `detectScheme detects Mastercard with 53 range prefix`() {
        val result = detector.detectScheme("5300000000000000")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
        assertEquals(2, result?.detectedAt)
    }

    @Test
    fun `detectScheme detects Mastercard with 54 range prefix`() {
        val result = detector.detectScheme("5400000000000000")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
        assertEquals(2, result?.detectedAt)
    }

    @Test
    fun `detectScheme detects Mastercard with 55 range prefix`() {
        val result = detector.detectScheme("5500000000000000")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
        assertEquals(2, result?.detectedAt)
    }

    // ===========================================
    // 4-Digit Prefix Detection Tests
    // ===========================================

    @Test
    fun `detectScheme detects Discover with 6011 prefix`() {
        // Use a 4-digit PAN to test 4-digit detection only
        val result = detector.detectScheme("6011")
        assertNotNull(result)
        assertEquals("discover", result?.scheme)
        assertEquals(4, result?.detectedAt)
    }

    @Test
    fun `detectScheme detects UnionPay with 6221 prefix`() {
        // Use a 4-digit PAN to test 4-digit detection only
        val result = detector.detectScheme("6221")
        assertNotNull(result)
        assertEquals("unionpay", result?.scheme)
        assertEquals(4, result?.detectedAt)
    }

    // ===========================================
    // 4-Digit Range Detection Tests
    // ===========================================

    @Test
    fun `detectScheme detects JCB with 3528 range start`() {
        val result = detector.detectScheme("3528000000000000")
        assertNotNull(result)
        assertEquals("japcb", result?.scheme)
        assertEquals(4, result?.detectedAt)
    }

    @Test
    fun `detectScheme detects JCB with 3550 in range`() {
        val result = detector.detectScheme("3550000000000000")
        assertNotNull(result)
        assertEquals("japcb", result?.scheme)
        assertEquals(4, result?.detectedAt)
    }

    @Test
    fun `detectScheme detects JCB with 3589 range end`() {
        val result = detector.detectScheme("3589000000000000")
        assertNotNull(result)
        assertEquals("japcb", result?.scheme)
        assertEquals(4, result?.detectedAt)
    }

    @Test
    fun `detectScheme detects Mastercard with 2221 4-digit range start`() {
        // Use a 4-digit PAN to test 4-digit range detection only
        val result = detector.detectScheme("2221")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
        assertEquals(4, result?.detectedAt)
    }

    @Test
    fun `detectScheme detects Mastercard with 2500 in 4-digit range`() {
        // Use a 4-digit PAN to test 4-digit range detection only
        val result = detector.detectScheme("2500")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
        assertEquals(4, result?.detectedAt)
    }

    @Test
    fun `detectScheme detects Mastercard with 2720 4-digit range end`() {
        // Use a 4-digit PAN to test 4-digit range detection only
        val result = detector.detectScheme("2720")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
        assertEquals(4, result?.detectedAt)
    }

    // ===========================================
    // 6-Digit BIN Detection Tests
    // ===========================================

    @Test
    fun `detectScheme detects Visa with specific 6-digit BIN 411111`() {
        val result = detector.detectScheme("4111111111111111")
        assertNotNull(result)
        assertEquals("visa", result?.scheme)
        assertEquals(6, result?.detectedAt)
    }

    @Test
    fun `detectScheme detects Mastercard with specific 6-digit BIN 222100`() {
        val result = detector.detectScheme("2221001111111111")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
        assertEquals(6, result?.detectedAt)
    }

    @Test
    fun `detectScheme detects Discover with specific 6-digit BIN 601100`() {
        val result = detector.detectScheme("6011001111111111")
        assertNotNull(result)
        assertEquals("discover", result?.scheme)
        assertEquals(6, result?.detectedAt)
    }

    // ===========================================
    // 6-Digit Range Detection Tests
    // ===========================================

    @Test
    fun `detectScheme detects UnionPay with 622126 6-digit range start`() {
        val result = detector.detectScheme("6221261111111111")
        assertNotNull(result)
        assertEquals("unionpay", result?.scheme)
        assertEquals(6, result?.detectedAt)
    }

    @Test
    fun `detectScheme detects UnionPay with 622500 in 6-digit range`() {
        val result = detector.detectScheme("6225001111111111")
        assertNotNull(result)
        assertEquals("unionpay", result?.scheme)
        assertEquals(6, result?.detectedAt)
    }

    @Test
    fun `detectScheme detects UnionPay with 622925 6-digit range end`() {
        val result = detector.detectScheme("6229251111111111")
        assertNotNull(result)
        assertEquals("unionpay", result?.scheme)
        assertEquals(6, result?.detectedAt)
    }

    // ===========================================
    // Override Hierarchy Tests
    // ===========================================

    @Test
    fun `detectScheme 6-digit match overrides 4-digit match`() {
        // 6011 is Discover at 4-digit, but 601100 is also Discover at 6-digit
        val result = detector.detectScheme("6011001111111111")
        assertNotNull(result)
        assertEquals("discover", result?.scheme)
        assertEquals(6, result?.detectedAt) // Should be 6-digit detection
    }

    @Test
    fun `detectScheme 4-digit match overrides 2-digit match`() {
        // 22 does not match at 2-digit level, but 2221 matches at 4-digit as Mastercard
        // Use 5 digits to ensure we go through 4-digit detection but not 6-digit
        val result = detector.detectScheme("22210")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
        assertEquals(4, result?.detectedAt)
    }

    // ===========================================
    // Detection Result Fields Tests
    // ===========================================

    @Test
    fun `detectScheme returns correct length for various PAN lengths`() {
        assertEquals(2, detector.detectScheme("41")?.length)
        assertEquals(4, detector.detectScheme("4111")?.length)
        assertEquals(6, detector.detectScheme("411111")?.length)
        assertEquals(16, detector.detectScheme("4111111111111111")?.length)
    }

    // ===========================================
    // Edge Cases and Boundary Tests
    // ===========================================

    @Test
    fun `detectScheme returns null when no scheme matches`() {
        val result = detector.detectScheme("9900000000000000")
        assertNull(result)
    }

    @Test
    fun `detectScheme handles PAN that only matches at 2-digit level`() {
        // 34 is Amex, and we have no 4-digit or 6-digit overrides for it
        val result = detector.detectScheme("3412345678901234")
        assertNotNull(result)
        assertEquals("amex", result?.scheme)
        assertEquals(2, result?.detectedAt)
    }

    @Test
    fun `detectScheme handles exactly 2 digit PAN`() {
        val result = detector.detectScheme("51")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
        assertEquals(2, result?.length)
    }

    @Test
    fun `detectScheme handles exactly 4 digit PAN`() {
        val result = detector.detectScheme("6011")
        assertNotNull(result)
        assertEquals("discover", result?.scheme)
        assertEquals(4, result?.length)
    }

    @Test
    fun `detectScheme handles exactly 6 digit PAN`() {
        val result = detector.detectScheme("411111")
        assertNotNull(result)
        assertEquals("visa", result?.scheme)
        assertEquals(6, result?.length)
    }

    // ===========================================
    // Empty BinData Tests
    // ===========================================

    @Test
    fun `detectScheme returns null with empty BinData`() {
        val emptyData = BinDataResponse()
        val emptyDetector = BinDataDetector(emptyData)

        val result = emptyDetector.detectScheme("4111111111111111")
        assertNull(result)
    }

    @Test
    fun `detectScheme returns null when scheme code not in schemes map`() {
        val dataWithMissingScheme = BinDataResponse(
            prefix2 = mapOf("99" to "unknown_code"),
            schemes = mapOf("v" to "visa") // "unknown_code" not in schemes
        )
        val detector = BinDataDetector(dataWithMissingScheme)

        val result = detector.detectScheme("9900000000000000")
        assertNull(result)
    }

    // ===========================================
    // Range Edge Cases
    // ===========================================

    @Test
    fun `detectScheme handles invalid range format gracefully`() {
        val dataWithInvalidRange = BinDataResponse(
            ranges = BinRanges(
                range2 = listOf(
                    listOf("51", "55") // Missing scheme code
                )
            ),
            schemes = mapOf("m" to "mastercard")
        )
        val detector = BinDataDetector(dataWithInvalidRange)

        val result = detector.detectScheme("5300000000000000")
        assertNull(result)
    }

    @Test
    fun `detectScheme handles non-numeric range values gracefully`() {
        val dataWithNonNumericRange = BinDataResponse(
            ranges = BinRanges(
                range2 = listOf(
                    listOf("abc", "def", "m") // Non-numeric values
                )
            ),
            schemes = mapOf("m" to "mastercard")
        )
        val detector = BinDataDetector(dataWithNonNumericRange)

        val result = detector.detectScheme("5300000000000000")
        assertNull(result)
    }
}

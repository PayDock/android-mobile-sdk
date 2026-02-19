package com.paydock.feature.card.data.detector

import com.paydock.core.BaseUnitTest
import com.paydock.feature.card.data.dto.BinDataResponse
import com.paydock.feature.card.data.dto.BinRanges
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [BinDataDetector].
 *
 * Tests verify detection at 2nd, 4th, 6th, and 8th digit positions,
 * including both exact prefix matches and range matches.
 */
internal class BinDataDetectorTest : BaseUnitTest() {

    private val testSchemes = mapOf(
        "v" to "visa",
        "m" to "mastercard",
        "a" to "amex",
        "d" to "discover",
        "u" to "unionpay",
        "c" to "diners",
        "j" to "japcb"
    )

    private val testPrefix2 = mapOf(
        "4" to "v",
        "34" to "a",
        "37" to "a"
    )

    private val testPrefix4 = mapOf(
        "6011" to "d",
        "3528" to "j"
    )

    private val testPrefix6 = mapOf(
        "601301" to "m",
        "650008" to "c"
    )

    private val testPrefix8 = mapOf(
        "60138205" to "u",
        "63894700" to "v"
    )

    private val testRange2 = listOf(
        listOf("22", "27", "m"),
        listOf("51", "55", "m")
    )

    private val testRange4 = listOf(
        listOf("2221", "2720", "m"),
        listOf("3400", "3499", "a")
    )

    private val testRange6 = listOf(
        listOf("222100", "222169", "m"),
        listOf("340000", "345677", "a")
    )

    private val testRange8 = listOf(
        listOf("22289600", "22289669", "m"),
        listOf("60138200", "60138299", "u")
    )

    private fun createTestData() = BinDataResponse(
        prefix2 = testPrefix2,
        prefix4 = testPrefix4,
        prefix6 = testPrefix6,
        prefix8 = testPrefix8,
        ranges = BinRanges(
            range2 = testRange2,
            range4 = testRange4,
            range6 = testRange6,
            range8 = testRange8
        ),
        schemes = testSchemes,
        version = 1
    )

    // ===========================================
    // Basic Detection Tests
    // ===========================================

    @Test
    fun `detectScheme returns null for empty PAN`() {
        val detector = BinDataDetector(createTestData())
        assertNull(detector.detectScheme(""))
    }

    @Test
    fun `detectScheme returns null for single digit PAN`() {
        val detector = BinDataDetector(createTestData())
        assertNull(detector.detectScheme("4"))
    }

    @Test
    fun `detectScheme strips non-digit characters`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("4111-1111-1111-1111")
        assertNotNull(result)
        assertEquals("visa", result?.scheme)
    }

    // ===========================================
    // 2-Digit Detection Tests
    // ===========================================

    @Test
    fun `detectScheme at 2 digits - exact match single digit prefix`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("41")
        assertNotNull(result)
        assertEquals("visa", result?.scheme)
        assertEquals(1, result?.detectedAt)
        assertEquals(2, result?.length)
    }

    @Test
    fun `detectScheme at 2 digits - exact match two digit prefix`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("34")
        assertNotNull(result)
        assertEquals("amex", result?.scheme)
        assertEquals(2, result?.detectedAt)
        assertEquals(2, result?.length)
    }

    @Test
    fun `detectScheme at 2 digits - range match`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("22")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
        assertEquals(2, result?.detectedAt)
    }

    @Test
    fun `detectScheme at 2 digits - range boundary start`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("51")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
    }

    @Test
    fun `detectScheme at 2 digits - range boundary end`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("55")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
    }

    // ===========================================
    // 4-Digit Detection Tests
    // ===========================================

    @Test
    fun `detectScheme at 4 digits - exact match`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("6011")
        assertNotNull(result)
        assertEquals("discover", result?.scheme)
        assertEquals(4, result?.detectedAt)
        assertEquals(4, result?.length)
    }

    @Test
    fun `detectScheme at 4 digits - range match`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("2500")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
        assertEquals(4, result?.detectedAt)
    }

    @Test
    fun `detectScheme at 4 digits - overrides 2 digit detection`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("3456")
        assertNotNull(result)
        assertEquals("amex", result?.scheme)
        assertEquals(4, result?.detectedAt)
    }

    // ===========================================
    // 6-Digit Detection Tests
    // ===========================================

    @Test
    fun `detectScheme at 6 digits - exact match`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("601301")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
        assertEquals(6, result?.detectedAt)
        assertEquals(6, result?.length)
    }

    @Test
    fun `detectScheme at 6 digits - range match`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("222150")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
        assertEquals(6, result?.detectedAt)
    }

    @Test
    fun `detectScheme at 6 digits - overrides 4 digit detection`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("340500")
        assertNotNull(result)
        assertEquals("amex", result?.scheme)
        assertEquals(6, result?.detectedAt)
    }

    // ===========================================
    // 8-Digit Detection Tests
    // ===========================================

    @Test
    fun `detectScheme at 8 digits - exact match`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("60138205")
        assertNotNull(result)
        assertEquals("unionpay", result?.scheme)
        assertEquals(8, result?.detectedAt)
        assertEquals(8, result?.length)
    }

    @Test
    fun `detectScheme at 8 digits - range match`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("22289650")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
        assertEquals(8, result?.detectedAt)
    }

    @Test
    fun `detectScheme at 8 digits - range boundary start`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("22289600")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
        assertEquals(8, result?.detectedAt)
    }

    @Test
    fun `detectScheme at 8 digits - range boundary end`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("22289669")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
        assertEquals(8, result?.detectedAt)
    }

    @Test
    fun `detectScheme at 8 digits - overrides 6 digit detection`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("60138250")
        assertNotNull(result)
        assertEquals("unionpay", result?.scheme)
        assertEquals(8, result?.detectedAt)
    }

    // ===========================================
    // Detection Only at Even Digit Positions
    // ===========================================

    @Test
    fun `detectScheme with 3 digits returns 2-digit detection`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("411")
        assertNotNull(result)
        assertEquals("visa", result?.scheme)
        assertEquals(1, result?.detectedAt)
        assertEquals(3, result?.length)
    }

    @Test
    fun `detectScheme with 5 digits returns 4-digit detection`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("60111")
        assertNotNull(result)
        assertEquals("discover", result?.scheme)
        assertEquals(4, result?.detectedAt)
        assertEquals(5, result?.length)
    }

    @Test
    fun `detectScheme with 7 digits returns 6-digit detection`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("6013011")
        assertNotNull(result)
        assertEquals("mastercard", result?.scheme)
        assertEquals(6, result?.detectedAt)
        assertEquals(7, result?.length)
    }

    @Test
    fun `detectScheme with 9 digits returns 8-digit detection`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("601382051")
        assertNotNull(result)
        assertEquals("unionpay", result?.scheme)
        assertEquals(8, result?.detectedAt)
        assertEquals(9, result?.length)
    }

    // ===========================================
    // Full Card Number Tests
    // ===========================================

    @Test
    fun `detectScheme with full card number uses most specific match`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("6013820512345678")
        assertNotNull(result)
        assertEquals("unionpay", result?.scheme)
        assertEquals(8, result?.detectedAt)
        assertEquals(16, result?.length)
    }

    @Test
    fun `detectScheme with Visa card number`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("4111111111111111")
        assertNotNull(result)
        assertEquals("visa", result?.scheme)
    }

    // ===========================================
    // No Match Tests
    // ===========================================

    @Test
    fun `detectScheme returns null for unknown prefix`() {
        val detector = BinDataDetector(createTestData())
        val result = detector.detectScheme("99")
        assertNull(result)
    }

    // ===========================================
    // Scheme Update Tests (Simulating Typing)
    // ===========================================

    @Test
    fun `scheme detected correctly as digits are added - Amex`() {
        val detector = BinDataDetector(createTestData())

        val result2 = detector.detectScheme("34")
        assertEquals("amex", result2?.scheme)
        assertEquals(2, result2?.detectedAt)

        val result4 = detector.detectScheme("3456")
        assertEquals("amex", result4?.scheme)
        assertEquals(4, result4?.detectedAt)

        val result6 = detector.detectScheme("345600")
        assertEquals("amex", result6?.scheme)
        assertEquals(6, result6?.detectedAt)
    }

    @Test
    fun `scheme can change at higher digit positions`() {
        val detector = BinDataDetector(createTestData())

        val result2 = detector.detectScheme("60")
        assertNull(result2)

        val result4 = detector.detectScheme("6011")
        assertEquals("discover", result4?.scheme)
        assertEquals(4, result4?.detectedAt)

        val result6 = detector.detectScheme("601301")
        assertEquals("mastercard", result6?.scheme)
        assertEquals(6, result6?.detectedAt)
    }
}

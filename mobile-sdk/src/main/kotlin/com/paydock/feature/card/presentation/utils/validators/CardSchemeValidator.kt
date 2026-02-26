package com.paydock.feature.card.presentation.utils.validators

import com.paydock.binprocessor.data.detector.BinDataDetector
import com.paydock.binprocessor.data.dto.BinDataResponse
import com.paydock.core.MobileSDKConstants
import com.paydock.feature.card.domain.model.integration.enums.CardType
import com.paydock.feature.card.domain.model.ui.CardCode
import com.paydock.feature.card.domain.model.ui.CardScheme
import com.paydock.feature.card.domain.model.ui.enums.CodeType

/**
 * Result of card scheme detection for UI display (e.g. test/debug).
 *
 * @property schemeName The detected scheme name (e.g., "visa", "mastercard")
 * @property detectedAt The number of digits at which the scheme was detected (1, 2, 4, 6, or 8)
 * @property length The length of the PAN that was checked
 */
internal data class CardSchemeDetectionResult(
    val schemeName: String,
    val detectedAt: Int,
    val length: Int
)

/**
 * An object responsible for validating and detecting the card scheme based on a given card number.
 * It uses BIN data with hierarchical prefix matching (2-digit → 4-digit → 6-digit → 8-digit BIN checks).
 *
 * The `detectCardScheme` function is the main entry point to identify the card scheme for a given card number.
 */
internal object CardSchemeValidator {

    private val schemes = listOf(
        CardScheme(
            type = CardType.VISA,
            gaps = listOf(4, 8, 12, 16),
            lengths = listOf(16, 18, 19),
            code = CardCode(type = CodeType.CVV, size = MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH)
        ),

        CardScheme(
            type = CardType.MASTERCARD,
            gaps = listOf(4, 8, 12, 16),
            lengths = listOf(16),
            code = CardCode(type = CodeType.CVC, size = MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH)
        ),

        CardScheme(
            type = CardType.AMEX,
            gaps = listOf(4, 10),
            lengths = listOf(15),
            code = CardCode(type = CodeType.CID, size = MobileSDKConstants.CardDetailsConfig.CID_LENGTH)
        ),

        CardScheme(
            type = CardType.DINERS,
            gaps = listOf(4, 10),
            lengths = listOf(14),
            code = CardCode(type = CodeType.CVV, size = MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH)
        ),

        CardScheme(
            type = CardType.DISCOVER,
            gaps = listOf(4, 8, 12, 16),
            lengths = listOf(16, 19),
            code = CardCode(type = CodeType.CID, size = MobileSDKConstants.CardDetailsConfig.CID3_LENGTH)
        ),

        CardScheme(
            type = CardType.JAPCB,
            gaps = listOf(4, 8, 12, 16),
            lengths = listOf(16),
            code = CardCode(type = CodeType.CVV, size = MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH)
        ),

        CardScheme(
            type = CardType.UNIONPAY,
            gaps = listOf(4, 8, 12, 16),
            lengths = listOf(16, 17, 18, 19),
            code = CardCode(type = CodeType.CVN, size = MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH)
        )
    )

    /**
     * Detects the card scheme for a given card number using BIN data.
     *
     * This function uses hierarchical prefix matching (2-digit → 4-digit → 6-digit → 8-digit BIN checks)
     * to identify the card scheme.
     *
     * @param binData The [BinDataResponse] containing BIN data for detection.
     * @param number The card number to validate and detect the scheme for.
     * @return The detected [CardScheme] corresponding to the card number, or `null` if no match is found.
     */
    fun detectCardScheme(binData: BinDataResponse?, number: String): CardScheme? {
        if (binData == null) return null

        val detector = BinDataDetector(binData)
        val result = detector.detectScheme(number) ?: return null

        // Map detected scheme name to CardType enum
        val cardType = mapSchemeNameToCardType(result.scheme) ?: return null

        // Return the corresponding CardScheme
        return schemes.find { it.type == cardType }
    }

    /**
     * Detects the card scheme and returns detailed result for UI (e.g. test display).
     *
     * @param binData The [BinDataResponse] containing BIN data for detection.
     * @param number The card number to validate and detect the scheme for.
     * @return [CardSchemeDetectionResult] with scheme name, detectedAt, and length, or `null` if no match.
     */
    fun detectSchemeWithResult(binData: BinDataResponse?, number: String): CardSchemeDetectionResult? {
        if (binData == null) return null

        val detector = BinDataDetector(binData)
        val result = detector.detectScheme(number) ?: return null

        return CardSchemeDetectionResult(
            schemeName = result.scheme,
            detectedAt = result.detectedAt,
            length = result.length
        )
    }

    /**
     * Maps a scheme name string to a CardType enum.
     *
     * @param schemeName The scheme name (e.g., "visa", "mastercard", "amex")
     * @return The corresponding CardType, or null if not recognized
     */
    private fun mapSchemeNameToCardType(schemeName: String): CardType? {
        return when (schemeName.lowercase()) {
            "visa" -> CardType.VISA
            "mastercard" -> CardType.MASTERCARD
            "amex" -> CardType.AMEX
            "diners" -> CardType.DINERS
            "discover" -> CardType.DISCOVER
            "japcb" -> CardType.JAPCB
            "unionpay" -> CardType.UNIONPAY
            else -> null
        }
    }
}
package com.paydock.feature.card.presentation.utils.validators

import com.paydock.binprocessor.data.dto.BinDataResponse
import com.paydock.core.BaseUnitTest
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.card.domain.model.integration.enums.CardType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class CardSchemeValidatorTest : BaseUnitTest() {

    companion object {
        private val VISA_LENGTHS = listOf(16, 18, 19)
        private val MASTERCARD_LENGTHS = listOf(16)
        private val AMEX_LENGTHS = listOf(15)
        private val DINERS_LENGTHS = listOf(14)
        private val DISCOVER_LENGTHS = listOf(16, 19)
        private val JAPCB_LENGTHS = listOf(16)
        private val UNIONPAY_LENGTHS = listOf(16, 17, 18, 19)

        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any?>> {
            return listOf(
                arrayOf("4024007105083702", CardType.VISA, VISA_LENGTHS),
                arrayOf("4208004923372193", CardType.VISA, VISA_LENGTHS),
                arrayOf("4208165487748971884", CardType.VISA, VISA_LENGTHS),
                arrayOf("4917345526504959", CardType.VISA, VISA_LENGTHS), // Visa Electron
                arrayOf("2356994999357329", CardType.MASTERCARD, MASTERCARD_LENGTHS),
                arrayOf("5570239494137094", CardType.MASTERCARD, MASTERCARD_LENGTHS),
                arrayOf("5499295755031070", CardType.MASTERCARD, MASTERCARD_LENGTHS),
                arrayOf("348090808209082", CardType.AMEX, AMEX_LENGTHS),
                arrayOf("371400194034655", CardType.AMEX, AMEX_LENGTHS),
                arrayOf("375527411179515", CardType.AMEX, AMEX_LENGTHS),
                arrayOf("30312568541349", CardType.DINERS, DINERS_LENGTHS), // Carte Blanche
                arrayOf("30484878591106", CardType.DINERS, DINERS_LENGTHS), // Carte Blanche
                arrayOf("36240062066419", CardType.DINERS, DINERS_LENGTHS), // International
                arrayOf("3530111333300000", CardType.JAPCB, JAPCB_LENGTHS), // BIN: 35 (2-digit match)
                arrayOf("3569018805679188", CardType.JAPCB, JAPCB_LENGTHS), // BIN: 356901 (6-digit match)
                arrayOf("3088123456789012", CardType.JAPCB, JAPCB_LENGTHS), // BIN: 3088 (4-digit match)
                arrayOf("6011709089999627", CardType.DISCOVER, DISCOVER_LENGTHS),
                arrayOf("6011820354558384", CardType.DISCOVER, DISCOVER_LENGTHS),
                arrayOf("6011266475952146623", CardType.DISCOVER, DISCOVER_LENGTHS),
                arrayOf("6282123456789012", CardType.UNIONPAY, UNIONPAY_LENGTHS), // 16 digits
                arrayOf("6285987654321098", CardType.UNIONPAY, UNIONPAY_LENGTHS), // 16 digits
                arrayOf("8105123456789012", CardType.UNIONPAY, UNIONPAY_LENGTHS), // 16 digits
                arrayOf("62298712345678901", CardType.UNIONPAY, UNIONPAY_LENGTHS), // 17 digits - BIN 622987
                arrayOf("628212345678901234", CardType.UNIONPAY, UNIONPAY_LENGTHS), // 18 digits
                arrayOf("6282123456789012345", CardType.UNIONPAY, UNIONPAY_LENGTHS) // 19 digits
            )
        }
    }

    @Parameterized.Parameter
    lateinit var cardNumber: String

    @Parameterized.Parameter(1)
    lateinit var expectedIssuer: CardType

    @Suppress("UNCHECKED_CAST")
    @Parameterized.Parameter(2)
    lateinit var expectedLengths: List<Int>

    @Test
    fun testValidateCardNumber() {
        val binData =
            readResourceFile("card/success_bin_data_response.json")
                .convertToDataClass<BinDataResponse>()

        assertEquals(
            expectedIssuer,
            CardSchemeValidator.detectCardScheme(binData, cardNumber)?.type
        )
    }

    /**
     * Asserts that every detected scheme exposes the correct length constraints.
     * This guards against regressions where scheme detection returns the right type
     * but length constraints are wrong or missing, which would allow the input UI to
     * accept more (or fewer) digits than valid for the scheme.
     */
    @Test
    fun detectCardScheme_returnsSchemeWithCorrectLengthConstraints() {
        val binData =
            readResourceFile("card/success_bin_data_response.json")
                .convertToDataClass<BinDataResponse>()
        val scheme = CardSchemeValidator.detectCardScheme(binData, cardNumber)
        assertNotNull(scheme)
        assertEquals(expectedIssuer, scheme?.type)
        assertEquals(
            "Scheme ${scheme?.type} must have correct lengths; input capping and validation depend on this",
            expectedLengths,
            scheme?.lengths
        )
    }
}
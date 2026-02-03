package com.paydock.feature.card.presentation.utils.validators

import com.paydock.core.BaseUnitTest
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.card.data.dto.BinDataResponse
import com.paydock.feature.card.domain.model.integration.enums.CardType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class CardSchemeValidatorTest : BaseUnitTest() {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any?>> {
            return listOf(
                arrayOf("4024007105083702", CardType.VISA),
                arrayOf("4208004923372193", CardType.VISA),
                arrayOf("4208165487748971884", CardType.VISA),
                arrayOf("4917345526504959", CardType.VISA), // Visa Electron
                arrayOf("2356994999357329", CardType.MASTERCARD),
                arrayOf("5570239494137094", CardType.MASTERCARD),
                arrayOf("5499295755031070", CardType.MASTERCARD),
                arrayOf("348090808209082", CardType.AMEX),
                arrayOf("371400194034655", CardType.AMEX),
                arrayOf("375527411179515", CardType.AMEX),
                arrayOf("30312568541349", CardType.DINERS), // Carte Blanche
                arrayOf("30484878591106", CardType.DINERS), // Carte Blanche
                arrayOf("36240062066419", CardType.DINERS), // International
                arrayOf("3530111333300000", CardType.JAPCB), // BIN: 35 (2-digit match)
                arrayOf("3569018805679188", CardType.JAPCB), // BIN: 356901 (6-digit match)
                arrayOf("3088123456789012", CardType.JAPCB), // BIN: 3088 (4-digit match)
                arrayOf("6011709089999627", CardType.DISCOVER),
                arrayOf("6011820354558384", CardType.DISCOVER),
                arrayOf("6011266475952146623", CardType.DISCOVER),
                arrayOf("6282123456789012", CardType.UNIONPAY), // 16 digits
                arrayOf("6285987654321098", CardType.UNIONPAY), // 16 digits
                arrayOf("8105123456789012", CardType.UNIONPAY), // 16 digits
                arrayOf("62298712345678901", CardType.UNIONPAY), // 17 digits - BIN 622987
                arrayOf("628212345678901234", CardType.UNIONPAY), // 18 digits
                arrayOf("6282123456789012345", CardType.UNIONPAY), // 19 digits
                arrayOf("5010123456789012", CardType.EFTPOS), // BIN: 5010
                arrayOf("5018123456789012", CardType.EFTPOS), // BIN: 5018
                arrayOf("5021123456789012", CardType.EFTPOS) // BIN: 5021
            )
        }
    }

    @Parameterized.Parameter
    lateinit var cardNumber: String

    @Parameterized.Parameter(1)
    lateinit var expectedIssuer: CardType

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
}
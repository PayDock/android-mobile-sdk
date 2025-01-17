package com.paydock.feature.card.presentation.utils.validators

import com.paydock.feature.card.domain.model.integration.enums.CardScheme
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class CardSchemeValidatorTest {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any?>> {
            return listOf(
                arrayOf("4024007105083702", CardScheme.VISA),
                arrayOf("4539654923372193", CardScheme.VISA),
                arrayOf("4539275487748971884", CardScheme.VISA),
                arrayOf("4917345526504959", CardScheme.VISA), // Visa Electron
                arrayOf("4917000869902803", CardScheme.VISA), // Visa Electron
                arrayOf("4175008066695309", CardScheme.VISA), // Visa Electron
                arrayOf("2720994999357329", CardScheme.MASTERCARD),
                arrayOf("2221009494137094", CardScheme.MASTERCARD),
                arrayOf("5499295755031070", CardScheme.MASTERCARD),
                arrayOf("372015808209082", CardScheme.AMEX),
                arrayOf("342439194034655", CardScheme.AMEX),
                arrayOf("347333411179515", CardScheme.AMEX),
                arrayOf("30312568541349", CardScheme.DINERS), // Carte Blanche
                arrayOf("30484878591106", CardScheme.DINERS), // Carte Blanche
                arrayOf("30330208562251", CardScheme.DINERS), // Carte Blanche
                arrayOf("36230262066419", CardScheme.DINERS), // International
                arrayOf("36213673014525", CardScheme.DINERS), // International
                arrayOf("36415529976035", CardScheme.DINERS), // International
                arrayOf("3534429687149385", CardScheme.JAPCB),
                arrayOf("3531808805679188", CardScheme.JAPCB),
                arrayOf("6011709089999627", CardScheme.DISCOVER),
                arrayOf("6011820354558384", CardScheme.DISCOVER),
                arrayOf("6011266475952146623", CardScheme.DISCOVER),
                arrayOf("6334101999990013", CardScheme.SOLO), // 16 digits
                arrayOf("67675678901234568698", CardScheme.SOLO), // 19 digits
                arrayOf("5893254569871234", CardScheme.AUSBC), // Bank of Queensland
                arrayOf("6304987654321098", CardScheme.AUSBC), // NAB
                arrayOf("6771891234567890", CardScheme.AUSBC), // Bendigo Bank)
            )
        }
    }

    @Parameterized.Parameter
    lateinit var cardNumber: String

    @Parameterized.Parameter(1)
    lateinit var expectedIssuer: CardScheme

    @Test
    fun testDetectCardIssuer() {
        assertEquals(expectedIssuer, CardSchemeValidator.detectCardScheme(cardNumber))
    }
}
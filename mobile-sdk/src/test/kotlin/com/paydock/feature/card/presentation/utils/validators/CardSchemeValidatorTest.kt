package com.paydock.feature.card.presentation.utils.validators

import com.paydock.core.BaseUnitTest
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.card.data.dto.CardSchemasResponse
import com.paydock.feature.card.data.mapper.asEntity
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
                arrayOf("4917000869902803", CardType.VISA), // Visa Electron
                arrayOf("4175008066695309", CardType.VISA), // Visa Electron
                arrayOf("2356994999357329", CardType.MASTERCARD),
                arrayOf("5570239494137094", CardType.MASTERCARD),
                arrayOf("5499295755031070", CardType.MASTERCARD),
                arrayOf("348090808209082", CardType.AMEX),
                arrayOf("371400194034655", CardType.AMEX),
                arrayOf("375527411179515", CardType.AMEX),
                arrayOf("30312568541349", CardType.DINERS), // Carte Blanche
                arrayOf("30484878591106", CardType.DINERS), // Carte Blanche
                arrayOf("30330208562251", CardType.DINERS), // Carte Blanche
                arrayOf("36240062066419", CardType.DINERS), // International
                arrayOf("36213673014525", CardType.DINERS), // International
                arrayOf("36415529976035", CardType.DINERS), // International
                arrayOf("2131519687149385", CardType.JAPCB),
                arrayOf("3569018805679188", CardType.JAPCB),
                arrayOf("6011709089999627", CardType.DISCOVER),
                arrayOf("6011820354558384", CardType.DISCOVER),
                arrayOf("6011266475952146623", CardType.DISCOVER),
                arrayOf("6334781999990013", CardType.SOLO), // 16 digits
                arrayOf("67675678901234568698", CardType.SOLO), // 19 digits
                arrayOf("5610254569871234", CardType.AUSBC),
                arrayOf("5602237654321098", CardType.AUSBC)
            )
        }
    }

    @Parameterized.Parameter
    lateinit var cardNumber: String

    @Parameterized.Parameter(1)
    lateinit var expectedIssuer: CardType

    @Test
    fun testValidateCardNumber() {
        val cardSchemasCollection =
            readResourceFile("card/success_get_card_schemas_response.json")
                .convertToDataClass<CardSchemasResponse>().asEntity()

        assertEquals(
            expectedIssuer,
            CardSchemeValidator.detectCardScheme(cardSchemasCollection, cardNumber)?.type
        )
    }
}
package com.paydock.feature.card.presentation.utils.validators

import com.paydock.api.management.data.dto.CardSchemasResponse
import com.paydock.api.management.data.mapper.asEntity
import com.paydock.core.BaseUnitTest
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.card.domain.model.integration.enums.CardScheme
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
                arrayOf("4024007105083702", CardScheme.VISA),
                arrayOf("4208004923372193", CardScheme.VISA),
                arrayOf("4208165487748971884", CardScheme.VISA),
                arrayOf("4917345526504959", CardScheme.VISA), // Visa Electron
                arrayOf("4917000869902803", CardScheme.VISA), // Visa Electron
                arrayOf("4175008066695309", CardScheme.VISA), // Visa Electron
                arrayOf("2356994999357329", CardScheme.MASTERCARD),
                arrayOf("5570239494137094", CardScheme.MASTERCARD),
                arrayOf("5499295755031070", CardScheme.MASTERCARD),
                arrayOf("348090808209082", CardScheme.AMEX),
                arrayOf("371400194034655", CardScheme.AMEX),
                arrayOf("375527411179515", CardScheme.AMEX),
                arrayOf("30312568541349", CardScheme.DINERS), // Carte Blanche
                arrayOf("30484878591106", CardScheme.DINERS), // Carte Blanche
                arrayOf("30330208562251", CardScheme.DINERS), // Carte Blanche
                arrayOf("36240062066419", CardScheme.DINERS), // International
                arrayOf("36213673014525", CardScheme.DINERS), // International
                arrayOf("36415529976035", CardScheme.DINERS), // International
                arrayOf("2131519687149385", CardScheme.JAPCB),
                arrayOf("3569018805679188", CardScheme.JAPCB),
                arrayOf("6011709089999627", CardScheme.DISCOVER),
                arrayOf("6011820354558384", CardScheme.DISCOVER),
                arrayOf("6011266475952146623", CardScheme.DISCOVER),
                arrayOf("6334781999990013", CardScheme.SOLO), // 16 digits
                arrayOf("67675678901234568698", CardScheme.SOLO), // 19 digits
                arrayOf("5610254569871234", CardScheme.AUSBC),
                arrayOf("5602237654321098", CardScheme.AUSBC)
            )
        }
    }

    @Parameterized.Parameter
    lateinit var cardNumber: String

    @Parameterized.Parameter(1)
    lateinit var expectedIssuer: CardScheme

    @Test
    fun testValidateCardNumber() {
        val cardSchemasCollection =
            readResourceFile("management/success_get_card_schemas_response.json")
                .convertToDataClass<CardSchemasResponse>().asEntity()

        assertEquals(
            expectedIssuer,
            CardSchemeValidator.detectCardScheme(cardSchemasCollection, cardNumber)
        )
    }
}
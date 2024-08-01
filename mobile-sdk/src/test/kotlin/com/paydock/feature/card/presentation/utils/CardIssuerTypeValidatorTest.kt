package com.paydock.feature.card.presentation.utils

import com.paydock.feature.card.presentation.model.CardIssuerType
import org.junit.Assert.assertEquals
import org.junit.Test

class CardIssuerTypeValidatorTest {

    @Suppress("LongMethod")
    @Test
    fun testDetectCardIssuer() {
        assertEquals(CardIssuerType.VISA, CardIssuerValidator.detectCardIssuer("4024007105083702"))
        assertEquals(CardIssuerType.VISA, CardIssuerValidator.detectCardIssuer("4539654923372193"))
        assertEquals(
            CardIssuerType.VISA,
            CardIssuerValidator.detectCardIssuer("4539275487748971884")
        )
        // Visa Electron
        assertEquals(CardIssuerType.VISA, CardIssuerValidator.detectCardIssuer("4917345526504959"))
        assertEquals(CardIssuerType.VISA, CardIssuerValidator.detectCardIssuer("4917000869902803"))
        assertEquals(CardIssuerType.VISA, CardIssuerValidator.detectCardIssuer("4175008066695309"))

        assertEquals(
            CardIssuerType.MASTERCARD,
            CardIssuerValidator.detectCardIssuer("2720994999357329")
        )
        assertEquals(
            CardIssuerType.MASTERCARD,
            CardIssuerValidator.detectCardIssuer("2221009494137094")
        )
        assertEquals(
            CardIssuerType.MASTERCARD,
            CardIssuerValidator.detectCardIssuer("5499295755031070")
        )

        assertEquals(
            CardIssuerType.AMERICAN_EXPRESS,
            CardIssuerValidator.detectCardIssuer("372015808209082")
        )
        assertEquals(
            CardIssuerType.AMERICAN_EXPRESS,
            CardIssuerValidator.detectCardIssuer("342439194034655")
        )
        assertEquals(
            CardIssuerType.AMERICAN_EXPRESS,
            CardIssuerValidator.detectCardIssuer("347333411179515")
        )

        // Diners Club - Carte Blanche
        assertEquals(
            CardIssuerType.DINERS_CLUB,
            CardIssuerValidator.detectCardIssuer("30312568541349")
        )
        assertEquals(
            CardIssuerType.DINERS_CLUB,
            CardIssuerValidator.detectCardIssuer("30484878591106")
        )
        assertEquals(
            CardIssuerType.DINERS_CLUB,
            CardIssuerValidator.detectCardIssuer("30330208562251")
        )
        // Diners Club - International
        assertEquals(
            CardIssuerType.DINERS_CLUB,
            CardIssuerValidator.detectCardIssuer("36230262066419")
        )
        assertEquals(
            CardIssuerType.DINERS_CLUB,
            CardIssuerValidator.detectCardIssuer("36213673014525")
        )
        assertEquals(
            CardIssuerType.DINERS_CLUB,
            CardIssuerValidator.detectCardIssuer("36415529976035")
        )

        assertEquals(CardIssuerType.JCB, CardIssuerValidator.detectCardIssuer("3534429687149385"))
        assertEquals(CardIssuerType.JCB, CardIssuerValidator.detectCardIssuer("3531808805679188"))
        assertEquals(
            CardIssuerType.JCB,
            CardIssuerValidator.detectCardIssuer("3544249360294788693")
        )

        assertEquals(
            CardIssuerType.MAESTRO,
            CardIssuerValidator.detectCardIssuer("5018186568344747")
        )
        assertEquals(
            CardIssuerType.MAESTRO,
            CardIssuerValidator.detectCardIssuer("5020280143144270")
        )
        assertEquals(
            CardIssuerType.MAESTRO,
            CardIssuerValidator.detectCardIssuer("5020373043087119")
        )

        assertEquals(
            CardIssuerType.DISCOVER,
            CardIssuerValidator.detectCardIssuer("6011709089999627")
        )
        assertEquals(
            CardIssuerType.DISCOVER,
            CardIssuerValidator.detectCardIssuer("6011820354558384")
        )
        assertEquals(
            CardIssuerType.DISCOVER,
            CardIssuerValidator.detectCardIssuer("6011266475952146623")
        )

        // China Union Pay
        assertEquals(
            CardIssuerType.UNION_PAY,
            CardIssuerValidator.detectCardIssuer("6250941006528599")
        )
        assertEquals(
            CardIssuerType.UNION_PAY,
            CardIssuerValidator.detectCardIssuer("6247628133655264")
        )

        assertEquals(
            CardIssuerType.INTER_PAY,
            CardIssuerValidator.detectCardIssuer("6362970000457013")
        )

        assertEquals(
            CardIssuerType.INSTA_PAYMENT,
            CardIssuerValidator.detectCardIssuer("6387249684531166")
        )
        assertEquals(
            CardIssuerType.INSTA_PAYMENT,
            CardIssuerValidator.detectCardIssuer("6381894258161100")
        )
        assertEquals(
            CardIssuerType.INSTA_PAYMENT,
            CardIssuerValidator.detectCardIssuer("6388737095552275")
        )

        assertEquals(CardIssuerType.UATP, CardIssuerValidator.detectCardIssuer("1100000000000013"))
        assertEquals(CardIssuerType.UATP, CardIssuerValidator.detectCardIssuer("135410014004955"))

        assertEquals(CardIssuerType.OTHER, CardIssuerValidator.detectCardIssuer("9234567890123456"))
    }
}
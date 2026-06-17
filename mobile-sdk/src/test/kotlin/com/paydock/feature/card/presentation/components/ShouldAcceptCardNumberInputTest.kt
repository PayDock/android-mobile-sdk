package com.paydock.feature.card.presentation.components

import com.paydock.core.MobileSDKConstants
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [shouldAcceptCardNumberInput] — the card-number entry cap that prevents typing
 * more digits than the detected scheme allows.
 */
internal class ShouldAcceptCardNumberInputTest {

    private val absoluteMax = MobileSDKConstants.CardDetailsConfig.MAX_CREDIT_CARD_LENGTH // 19

    @Test
    fun `blocks a typed 17th digit on a 16-digit scheme`() {
        // Mastercard (max 16): typing one more digit onto a full 16-digit number is rejected.
        assertFalse(
            shouldAcceptCardNumberInput(
                newDigits = "55555555555544447",
                currentLength = 16,
                schemeMaxLength = 16
            )
        )
    }

    @Test
    fun `allows a typed digit up to the scheme max`() {
        // Going from 15 to 16 digits on a 16-digit scheme is accepted.
        assertTrue(
            shouldAcceptCardNumberInput(
                newDigits = "5555555555554444",
                currentLength = 15,
                schemeMaxLength = 16
            )
        )
    }

    @Test
    fun `always allows deletions even when over the scheme max`() {
        // Deleting a digit (length decreases) is always accepted, regardless of the cap.
        assertTrue(
            shouldAcceptCardNumberInput(
                newDigits = "5555555555554444", // 16
                currentLength = 17,
                schemeMaxLength = 16
            )
        )
    }

    @Test
    fun `allows a bulk paste up to the absolute max when it switches scheme`() {
        // A bulk change (not a single-digit increment) may switch scheme (e.g. Amex 15 -> Visa 16),
        // so it is allowed up to the absolute max even though the previous scheme's cap was smaller.
        assertTrue(
            shouldAcceptCardNumberInput(
                newDigits = "4111111111111111", // 16, pasted in one go
                currentLength = 0,
                schemeMaxLength = 16
            )
        )
    }

    @Test
    fun `caps typed digits at the absolute max when no scheme is detected`() {
        // No scheme yet: incremental entry is capped at the absolute maximum (19).
        assertTrue(
            shouldAcceptCardNumberInput(
                newDigits = "1".repeat(absoluteMax),
                currentLength = absoluteMax - 1,
                schemeMaxLength = null
            )
        )
        assertFalse(
            shouldAcceptCardNumberInput(
                newDigits = "1".repeat(absoluteMax + 1),
                currentLength = absoluteMax,
                schemeMaxLength = null
            )
        )
    }

    @Test
    fun `allows a typed 19th digit on a 19-digit scheme`() {
        // A scheme that genuinely allows 19 digits is not capped early.
        assertTrue(
            shouldAcceptCardNumberInput(
                newDigits = "1".repeat(19),
                currentLength = 18,
                schemeMaxLength = 19
            )
        )
    }
}

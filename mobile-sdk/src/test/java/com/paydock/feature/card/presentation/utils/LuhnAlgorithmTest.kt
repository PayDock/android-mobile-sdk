/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paydock.feature.card.presentation.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class LuhnAlgorithmTest {

    @Test
    fun testValidCardNumber() {
        val validCardNumber = "4532015112830366"
        val result = CreditCardInputValidator.isLuhnValid(validCardNumber)
        assertEquals(true, result)
    }

    @Test
    fun testInvalidCardNumber() {
        val invalidCardNumber = "4532015112830367"
        val result = CreditCardInputValidator.isLuhnValid(invalidCardNumber)
        assertEquals(false, result)
    }

    @Test
    fun testValidCardNumberWithSpaces() {
        val cardNumberWithSpaces = "4 5320 1511 2830 366"
        val result = CreditCardInputValidator.isLuhnValid(cardNumberWithSpaces)
        assertEquals(true, result)
    }

    @Test
    fun testInvalidCardNumberWithNonDigits() {
        val cardNumberWithNonDigits = "453201511283036a"
        val result = CreditCardInputValidator.isLuhnValid(cardNumberWithNonDigits)
        assertEquals(false, result)
    }

    @Test
    fun testEmptyString() {
        val emptyString = ""
        val result = CreditCardInputValidator.isLuhnValid(emptyString)
        assertEquals(false, result)
    }

    @Test
    fun testShortCardNumber() {
        val shortCardNumber = "123"
        val result = CreditCardInputValidator.isLuhnValid(shortCardNumber)
        assertEquals(false, result)
    }

    @Test
    fun testCardHolderName() {
        val cardHolderName = "John Doe"
        val result = CreditCardInputValidator.isLuhnValid(cardHolderName)
        assertEquals(false, result)
    }
}